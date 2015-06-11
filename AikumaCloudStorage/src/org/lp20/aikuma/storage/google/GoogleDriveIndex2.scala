package org.lp20.aikuma.storage.google

import java.util.{Map => JMap}
import org.lp20.aikuma.net.Http
import org.lp20.aikuma.storage.Utils

class GoogleDriveIndex2(rootTitle: String, tm: TokenManager, baseUrl: String, idToken: String)
extends GoogleDriveIndex(rootTitle, tm) {

  override def index(identifier: String, metadata: JMap[String,String]) =
    call("POST", identifier, metadata)

  override def update(identifier: String, metadata: JMap[String,String]) =
    call("PUT", identifier, metadata)

  private def call(method: String, identifier: String, metadata: JMap[String,String]): Boolean = {
    if (identifier == null || metadata == null)
      return false

    Http(s"$baseUrl/$identifier")
      .method(method)
      .header("X-Aikuma-Auth-Token", idToken)
      .body(Utils.map2query(metadata))
      .code match {
        case 202 => true
        case _ => false
      }
  }
}

