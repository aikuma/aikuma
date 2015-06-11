package org.lp20.aikuma.storage.google

import collection.JavaConversions._
import collection.mutable.ArrayBuffer
import java.util.logging.Logger
import java.util.{Map => JMap, List}
import org.json.simple._
import org.lp20.aikuma.storage._
import org.lp20.aikuma.net._
import SimpleJsonConversions._

/**
 * @param rootTitle Title of the root folder.
 * @param tm A TokenManager object.
 */
@throws[DataStore.StorageException]("if provided root folder it not found")
class GoogleDriveIndex(rootTitle: String, tm: TokenManager) extends Index {
  
  private val log = Logger.getLogger(this.getClass.getName)
  private val api = new Api(tm)
  private val ghttp = new GHttpFactory(tm)
  private val rootId = findAikumaRoot match {
      case Some(v) => v
      case None => throw new DataStore.StorageException
  }

  private def findAikumaRoot: Option[String] = {
    val res = api search s"title='$rootTitle' and 'root' in parents and trashed = false"
    if (res.hasMoreElements) {
      // TODO: It should throw an error when there are more than one root.
      Some(res.nextElement.gets("id"))
    } else None
  }

  private implicit def functionToSearchResultProcessor(
    f: JMap[String,String] => Boolean
  ): Index.SearchResultProcessor = {
    new Index.SearchResultProcessor {
      override def process(result: JMap[String,String]): Boolean = f(result)
    }
  }

  private def mergeTags(tags1: String, tags2: String): String = {
    s"$tags1 $tags2".split("\\s+").toSet.mkString(" ")
  }

  private def mapToTags(map: JMap[String,String]): String = {
    val sr = new java.io.StringWriter()
    for ((k,v) <- map) {
      val s = s"__${k.replaceAll("\\s+", "_")}__${v.replaceAll("\\s+", "_")}\n"
      sr.write(s)
    }
    sr.toString
  }

  private def tagsToMap(s: String): JMap[String,String] = {
    def str2pair(t: String) = t.split("__", 2) match {
      case Array(a,b) => (a,b)
      case Array(a) => (a,"")
    }
    if (s == null) Map[String,String]()
    else s.trim.split("\\s+").map(str2pair).toMap: Map[String,String]
  }

  private def escape(s: String): String = {
    s.replaceAll("'", "\\'")
  }

  class SearchObj(search: Search) extends Iterator[JSONObject] {
    def next = search.nextElement
    def hasNext = search.hasMoreElements
    def first = if (hasNext) Some(next) else None
  }

  private implicit def search2searchObj(search: Search) = new SearchObj(search)

  override def getItemMetadata(identifier: String): JMap[String,String] = {
    val q = s"title = '$identifier' and '$rootId' in parents and trashed = false"
    api.search(q).first.map( item => tagsToMap(item.gets("description")) ).orNull
  }

  override def search(constraints: JMap[String,String]): List[String] = {
    val tags = escape(mapToTags(constraints))
    val q = s"fullText contains '$tags' and '$rootId' in parents and trashed = false"
    api.search(q).map(_.gets("title")).toList
  }

  override def search(constraints: JMap[String,String], processor: Index.SearchResultProcessor) {
    val tags = mapToTags(constraints)
    val q = s"fullText contains '${escape(tags)}' and '$rootId' in parents and trashed = false"
    search(q, processor)
  }

  override def search(query: String, processor: Index.SearchResultProcessor) {
    val conj = if (query.trim == "") "" else "and"
    val q = s"$query $conj '$rootId' in parents and trashed = false"
    try {
      api.search(q).map { item =>
        val meta = tagsToMap(item.gets("description")) +
        ("identifier" -> item.gets("title"))
        processor.process(meta)
      } .takeWhile(identity).size
    } catch {
      case e: Search#Error => log.fine("query error")
    }
  }

  override def index(identifier: String, metadata: JMap[String,String]): Boolean = {
    val q = s"title = '$identifier' and '$rootId' in parents and trashed = false"
    api.search(q).first.map { item =>
      val meta = new JSONObject(Map("description" -> mapToTags(metadata)))
      api.updateMetadata(item.gets("id"), meta) != null
    } getOrElse false
  }

  override def update(identifier: String, metadata: JMap[String,String]): Boolean = {
    val q = s"title = '$identifier' and '$rootId' in parents and trashed = false"
    api.search(q).first.map { item =>
      val tags = mergeTags(item.gets("description"), mapToTags(metadata))
      val meta = new JSONObject(Map("description" -> tags))
      api.updateMetadata(item.gets("id"), meta) != null
    } getOrElse false
  }
}

