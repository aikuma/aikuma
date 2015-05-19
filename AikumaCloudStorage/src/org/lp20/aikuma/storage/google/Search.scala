package org.lp20.aikuma.storage.google

import java.util._
import java.util.logging.{Logger,Level}
import org.json.simple._

abstract class Search(query: String, kind: String) {
  private val log = Logger.getLogger("Search")

  private var mNextPageToken: String = null
  private var mNumItems: Int = 0
  private var mIdx: Int = 0
  private var mArr: JSONArray = null
  private var mErr: Boolean = false

  private def nextIdx: Int = {
    val x = mIdx
    mIdx += 1
    x
  }

  processListObj(getMore(query, mNextPageToken))

  class Error extends Exception

  protected def getMore(query: String, pageToken: String): JSONObject

  def hasMoreElements: Boolean = {
    // if there is error, let them call nextElement and handle error
    mErr || mNextPageToken != null || mNumItems > mIdx
  }

  @throws[NoSuchElementException]("if there's no more item to return")
  @throws[Error]("if error occurred while processing query result")
  def nextElement: JSONObject = {
    if (mErr)
      throw new Error
    if (mNumItems <= mIdx && mNextPageToken != null)
      processListObj(getMore(query, mNextPageToken))
    if (mNumItems > mIdx)
      mArr.get(nextIdx).asInstanceOf[JSONObject]
    else
      throw new NoSuchElementException
  }

  def processListObj(obj: JSONObject) {
    if (obj == null) {
      log.fine("received null")
      mNextPageToken = null
      mNumItems = 0
      mIdx = 0
      mErr = true
    } else if (!kind.equals(obj.get("kind"))) {
      log.log(Level.FINE, "wrong kind received: %s", obj.get("kind"))
      mErr = true
    } else {
      mArr = obj.get("items").asInstanceOf[JSONArray]
      mIdx = 0
      mNumItems = mArr.size
      mNextPageToken = obj.get("nextPageToken").asInstanceOf[String]
      log.fine(s"items: $mNumItems next token: $mNextPageToken")
    }
  }
}
