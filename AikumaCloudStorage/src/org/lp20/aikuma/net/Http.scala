package org.lp20.aikuma.net

import util.control.NonFatal
import java.net._
import java.io._
import org.lp20.aikuma.storage._

class Http(url: URL) {
  type X <: Http

  val con = try {
    Option(url.openConnection.asInstanceOf[HttpURLConnection])
  } catch {
    case e: IOException => None
  }

  def method(m: String) = {
    con.map(_.setRequestMethod(m))
    asInstanceOf[X]
  }

  def header(k: String, v: String) = {
    con.map(_.setRequestProperty(k, v))
    asInstanceOf[X]
  }

  def chunked(n: Int) = {
    con.map(_.setChunkedStreamingMode(n))
    asInstanceOf[X]
  }

  def body(b: String) = {
    writer.foreach(_.write(b.getBytes))
    asInstanceOf[X]
  }

  def body(b: InputStream) = {
    writer.foreach(Utils.copyStream(b, _))
    asInstanceOf[X]
  }

  def code: Int = {
    try {
      con.map(_.getResponseCode) getOrElse 1000
    } catch {
      case e: IOException => 1100
    }
  }

  def message: String = {
    try {
      con.map(_.getResponseMessage) getOrElse "no connection"
    } catch {
      case e: IOException => "io exception: " + e.getMessage
    }
  }

  def read: String = {
    try {
      con.map(Utils readStream _.getInputStream) getOrElse null
    } catch {
      case e: IOException => null
    }
  }

  def inputStream: InputStream = {
    con.map(_.getInputStream) getOrElse null
  }

  private def writer: Option[OutputStream] = {
    con.map(x => {
      if (!x.getDoOutput) x.setDoOutput(true)
      x.getOutputStream
    })
  }
}

object Http {
  def apply(url: URL) = new Http(url)
  def apply(url: String) = new Http(new URL(url))
}

