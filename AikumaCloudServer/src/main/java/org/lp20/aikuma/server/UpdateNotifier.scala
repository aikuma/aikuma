package org.lp20.aikuma.server

import collection.JavaConversions.enumerationAsScalaIterator
import collection.JavaConversions.mapAsJavaMap
import java.util.logging.{Logger,Level}
import org.json.simple._
import org.lp20.aikuma.storage._
import org.lp20.aikuma.storage.google._
import org.lp20.aikuma.storage.google.{TokenManager => TM}
import org.lp20.aikuma.server.gcm.GcmServer

class UpdateNotifier(tm: TokenManager, gd: GoogleDriveStorage, gcm: GcmServer) {
  val log = Logger.getLogger("UpdateNotifier")
  val types = Set("respeaking")

  val storageTm = new TM {
    override def accessToken: String = tm.getAccessToken
  }
  val mUserIdx = new GoogleUserIndex(storageTm, gd)

  def processNewFile(identifier: String) {
    parseIdentifier(identifier) match {
      case Some(p) =>
        if (types.contains(p.fileType))
          for (user <- findFollowers(p.itemId))
            notify(user, identifier)
      case None =>
        log.warning("invalid identifier: " + identifier)
    }
  }

  def findFollowers(itemId: String): Iterator[String] = {
    mUserIdx.searchUser(s"follow-item:$itemId")
  }

  def notify(user: String, identifier: String): Unit = {
    log.fine(s"notifing $user of $identifier")
    gcm.sendMessage(user, Map("user" -> user, "identifier" -> identifier))
  }

  def parseIdentifier(s: String): Option[ParsedId] = {
    val filename = new java.io.File(s).getName.replaceAll("\\.[^.]*$", "")
    filename.split("-") match {
      case Array(itemId, userId, typeName) =>
        Some(new ParsedId(itemId, userId, typeName))
      case Array(itemId, userId, typeName, sn) =>
        Some(new ParsedId(itemId, userId, typeName, Some(sn)))
      case _ =>
        None
    }
  }

  private[UpdateNotifier] case class ParsedId(
    itemId: String,
    userId: String,
    fileType: String,
    extraId: Option[String] = None)
}


