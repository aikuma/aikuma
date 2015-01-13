import org.mapdb._
import scala.collection.JavaConversions._

object MapDbTest {
  def main(args: Array[String]) {
    val mapdbFileName = args(0)
    val db = DBMaker.newFileDB(new java.io.File(mapdbFileName)).make()
    val map = db.getHashMap("log")
    for (entry <- map.entrySet) {
      println(entry.getKey)
    }
    println("ok")
  }
}
