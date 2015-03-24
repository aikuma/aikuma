package org.lp20.aikuma.server.gcm

import collection.JavaConversions._
import util.Try

import javax.net.ssl._
import java.util.logging.{Logger,Level}
import java.util.{Map => JMap, HashMap}

import org.jivesoftware.smack._
import org.jivesoftware.smack.tcp._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._
import org.jivesoftware.smack.provider.ProviderManager
import org.json.simple._

abstract class GcmServer(
  host: String,
  port: Int,
  senderId: String,
  apiSecret: String
) {
  val logger = Logger.getLogger("GcmServer")

  type M = JMap[String,String]
  type MM = JMap[String,M]

  protected val con = connect

  private def connect: XMPPTCPConnection = {
    ProviderManager.addExtensionProvider(
      GcmPacketExtension.GCM_ELEMENT_NAME,
      GcmPacketExtension.GCM_NAMESPACE,
      GcmPacketExtensionProvider)

    val config = new ConnectionConfiguration(host, port)
    config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled)
    config.setReconnectionAllowed(true)
    config.setRosterLoadedAtLogin(false)
    config.setSendPresence(false)
    config.setSocketFactory(SSLSocketFactory.getDefault)

    val con = new XMPPTCPConnection(config)
    con.addConnectionListener(ConListener)
    con.addPacketListener(PktListener, new PacketTypeFilter(classOf[Message]))
    con.addPacketInterceptor(PktInterceptor, new PacketTypeFilter(classOf[Message]))
    con.connect
    con.login(senderId, apiSecret)

    con
  }

  protected def send(json: String): Unit = {
    val request = new GcmPacketExtension(json).toPacket
    con.sendPacket(request)
  }

  def sendMessage(email: String, data: java.util.Map[String,String]): Unit

  def registerUser(email: String, regId: String): Unit

  object PktListener extends PacketListener {
    def processPacket(packet: Packet) {
      println(s"got packet from ${packet.getFrom}")
      val gcmPacket = packet.asInstanceOf[Message]
        .getExtension(GcmPacketExtension.GCM_NAMESPACE)
        .asInstanceOf[GcmPacketExtension]
      val json = gcmPacket.getJson
      logger.info(json)

      try {
        val obj = JSONValue.parseWithException(json).asInstanceOf[M]
        obj.getOrElse("message_type", null) match {
          case null => {
            val m: M = Map(
              "message_type" -> "ack",
              "message_id" -> obj("message_id"),
              "to" -> obj("from")
            )
            send( JSONValue.toJSONString(m) )
            for {
              data <- Try( obj.asInstanceOf[MM]("data") )
              user <- Try( data("email") )
            } registerUser(user, obj("from"))
          }
          case "ack" => {
            val msg = s"ack from ${obj("from")} message id: ${obj("message_id")}"
            logger.info(msg)
          }
          case "nack" => {
            val msg = s"nack from ${obj("from")} message id: ${obj("message_id")}"
            logger.info(msg)
          }
        }
      } catch {
        case e: org.json.simple.parser.ParseException =>
          logger.log(Level.SEVERE, "Error parsing json: " + json, e)
        case e: Exception =>
          logger.log(Level.SEVERE, "Failed to process packet", e)
      }
    }
  }

  object PktInterceptor extends PacketInterceptor {
    def interceptPacket(packet: Packet) {
      println("intercepted packet")
    }
  }

  object ConListener extends ConnectionListener {
    def connected(con: XMPPConnection) {
      logger.info("gcm connected")
    }
    def authenticated(con: XMPPConnection) {
      logger.info("gcm authenticated")
    }
    def reconnectionSuccessful {
      println("reconnecting")
    }
    def reconnectionFailed(e: Exception) {
      println("reconnection failed:  " + e.getMessage)
    }
    def reconnectingIn(n: Int) {
      println(s"reconnecting in $n seconds")
    }
    def connectionClosedOnError(e: Exception) {
      println("connection closed on error: " + e.getMessage)
    }
    def connectionClosed {
      println("connection closed")
    }
  }

}
