package org.lp20.aikuma.server.gcm

import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.util.StringUtils
import org.jivesoftware.smack.provider.PacketExtensionProvider
import org.xmlpull.v1.XmlPullParser
import GcmPacketExtension._

class GcmPacketExtension(json: String)
  extends DefaultPacketExtension(GCM_ELEMENT_NAME, GCM_NAMESPACE) {

  def getJson: String = json

  override def toXML: String = {
    String.format("<%s xmlns=\"%s\">%s</%s>",
      GCM_ELEMENT_NAME, GCM_NAMESPACE,
      StringUtils.escapeForXML(json),
      GCM_ELEMENT_NAME)
  }

  def toPacket: Packet = {
    val message = new Message
    message.addExtension(this)
    message
  }
}

object GcmPacketExtension {
  val GCM_ELEMENT_NAME = "gcm"
  val GCM_NAMESPACE = "google:mobile:data"
}

object GcmPacketExtensionProvider extends PacketExtensionProvider {
  @throws[Exception]
  override def parseExtension(parser: XmlPullParser): PacketExtension = {
    val json = parser.nextText
    new GcmPacketExtension(json)
  }
}

