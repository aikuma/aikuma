package org.lp20.aikuma.storage.google

import java.util.Enumeration

import org.lp20.aikuma.storage.UserIndex

/**
 * @param tm TokenManager object to access Google API.
 * @param gd GoogleDriveStorage object. Not used directly, but required to
 *    make sure that GoogleDriveFolderCache has been initialized with the
 *    user's Aikuma folder.
 */
class GoogleUserIndex(tm: TokenManager, gd: GoogleDriveStorage) extends UserIndex {
  val indexPath = "/index/user"
  val gapi = new Api(tm)

  private def indexFid = {
    GoogleDriveFolderCache.getInstance.getFid(indexPath)
  }

  override def searchUser(term: String): Enumeration[String] = {
    val query = s"parentId=${indexFid} and fullText contains '$term'"
    new Enumeration[String] {
      val search = gapi.search(query)
      def hasMoreElements: Boolean = search.hasMoreElements
      def nextElement: String = {
        val obj = search.nextElement
        val title = obj.get("title").asInstanceOf[String]
        title
      }
    }
  }
}

