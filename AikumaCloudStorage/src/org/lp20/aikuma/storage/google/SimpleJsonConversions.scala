package org.lp20.aikuma.storage.google

import org.json.simple.JSONObject

object SimpleJsonConversions {

  class JSONObjectWrapper(obj: JSONObject) {
    def gets(path: String*): String = {
      var o: Any = obj
      for (s <- path) o = o.asInstanceOf[JSONObject].get(s)
      o.asInstanceOf[String]
    }
  }

  implicit def wrapJSONObject(obj: JSONObject) = new JSONObjectWrapper(obj)
}

