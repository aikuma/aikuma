package org.lp20.aikuma.storage.google

import java.net.URL
import org.lp20.aikuma.net.Http

class GHttp(url:URL) extends Http(url) {
  type X = GHttp
  def accessToken(v: String) = {
    header("Authorization", s"Bearer $v")
  }
}

class GHttpFactory(tm: TokenManager) {
  def apply(url: URL) = (new GHttp(url)).accessToken(tm.accessToken)
  def apply(url: String): GHttp = apply(new URL(url))
}

