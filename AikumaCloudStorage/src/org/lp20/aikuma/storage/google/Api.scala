package org.lp20.aikuma.storage.google

import java.util.logging.Logger
import java.net._
import org.json.simple._
import org.lp20.aikuma.storage._
import org.lp20.aikuma.net._


class Api(tm: TokenManager) {
  val log = Logger.getLogger(this.getClass.getName)
  val ghttp = new GHttpFactory(tm)

  def copyFile(fileId: String, meta: JSONObject): JSONObject = {
    val http = ghttp(s"https://www.googleapis.com/drive/v2/files/$fileId/copy")
      .method("POST")
      .header("Content-Type", "application/json")
      .body(meta.toString)

    http.code match {
      case HttpURLConnection.HTTP_OK =>
        JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }

  def deleteFile(fileId: String): Boolean = {
    val url = s"https://www.googleapis.com/drive/v2/files/$fileId"
    ghttp(url).method("DELETE").code == HttpURLConnection.HTTP_OK
  }

  def deletePerm(fileId: String, permissionId: String): Boolean = {
    val url = s"https://www.googleapis.com/drive/v2/files/$fileId/permissions/$permissionId"
    ghttp(url).method("DELETE").code == 204
  }

  def download(url: String): java.io.InputStream = {
    try {
      val http = ghttp(url).method("GET")
      http.code match {
        case HttpURLConnection.HTTP_OK => http.inputStream
        case _ => null
      }
    } catch {
      case e: Exception => {e.printStackTrace; null}
    }
  } 

  def emptyTrash: Boolean = {
    val url = "https://www.googleapis.com/drive/v2/files/trash"
    ghttp(url).method("DELETE").code == 204
  }

  def exist(q: String): Boolean = search(q).hasMoreElements

  def getInfo(fileId: String): JSONObject = {
    val url = s"https://www.googleapis.com/drive/v2/files/$fileId"
    val http = ghttp(url).method("GET")
    http.code match {
      case 200 => JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }

  def insertFile(data: Data, meta: JSONObject): JSONObject = {
    val bd = "bdbdbdbdbdbdbdbdbdbd"
    val http = ghttp("https://www.googleapis.com/upload/drive/v2/files?uploadType=multipart")
      .method("POST")
      .header("Content-Type", s"""multipart/related; boundary="$bd"""")
      .chunked(8192)
      .body(s"--$bd\r\n")
      .body("Content-Type: application/json\r\n")
      .body("\r\n")
      .body(meta.toString)
      .body("\r\n")
      .body(s"--$bd\r\n")
      .body(s"Content-Type: ${data.getMimeType}\r\n")
      .body("\r\n")
      .body(data.getInputStream)
      .body("\r\n")
      .body(s"--$bd--\r\n")

    http.code match {
      case HttpURLConnection.HTTP_OK =>
        JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }

  def list(query: String, pageToken: String): JSONObject = {
    val url = try {
      val ub = new Utils.UrlBuilder("https://www.googleapis.com/drive/v2/files/")
      if (pageToken != null && !pageToken.isEmpty)
        ub.addQuery("pageToken", pageToken)
      else if (query != null && !query.isEmpty)
        ub.addQuery("q", query)
      ub.toUrl()
    } catch {
      case e: Exception => {
        log.fine("exception: " + e.getMessage)
        null
      }
    }

    if (url == null) return null

    val http = ghttp(url).method("GET")
    http.code match {
      case HttpURLConnection.HTTP_OK => {
        val obj = JSONValue.parse(http.read).asInstanceOf[JSONObject]
        obj.get("kind") match {
          case "drive#fileList" => obj
          case other: String => {
            log.fine("expected driv#fileList but received: $other")
            null
          }
        }
      }
      case code => null
    }
  }

  def makeFile(meta: JSONObject): JSONObject = {
    val http = ghttp("https://www.googleapis.com/drive/v2/files")
      .method("POST")
      .header("Content-Type", "application/json")
      .body(meta.toString)

    http.code match {
      case HttpURLConnection.HTTP_OK =>
        JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }

  def permId: String = {
    val http = ghttp("https://www.googleapis.com/drive/v2/about?fields=permissionId")
      .method("GET")

    http.code match {
      case HttpURLConnection.HTTP_OK =>
        JSONValue.parse(http.read).asInstanceOf[JSONObject]
          .get("permissionId").asInstanceOf[String]
      case _ => null
    }
  }

  def search(query: String): Search = {
    new Search(query, "drive#fileList") {
      override def getMore(query: String, pageToken: String): JSONObject = {
        log.fine(s"pageToken: $pageToken query: $query")
        list(query, pageToken)
      }
    }
  }

  def shareWith(fileId: String, email: String): JSONObject = {
    val meta = (new JSONObject).asInstanceOf[java.util.Map[String,String]]
    meta.put("type", "user")
    meta.put("value", email)
    meta.put("role", "reader")
    
    val http = ghttp(s"https://www.googleapis.com/drive/v2/files/$fileId/permissions")
      .method("POST")
      .header("Content-Type", "application/json")
      .body(meta.toString)

    http.code match {
      case HttpURLConnection.HTTP_OK =>
        JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }

  def trashFile(fileId: String): JSONObject = {
    val url = s"https://www.googleapis.com/drive/v2/files/$fileId/trash"
    val http = ghttp(url).method("POST")
    http.code match {
      case HttpURLConnection.HTTP_OK =>
        JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }

  def updateMetadata(fileId: String, meta: JSONObject): JSONObject = {
    val http = ghttp("https://www.googleapis.com/drive/v2/files/" + fileId)
      .method("PUT")
      .header("Content-Type", "application/json")
      .body(meta.toString)

    http.code match {
      case HttpURLConnection.HTTP_OK => JSONValue.parse(http.read).asInstanceOf[JSONObject]
      case _ => null
    }
  }
}
