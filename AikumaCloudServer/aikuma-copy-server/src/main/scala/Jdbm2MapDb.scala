import jdbm.RecordManager
import jdbm.RecordManagerFactory
import org.mapdb._
import org.lp20.aikuma.servers.ProcessLog
import org.lp20.aikuma.copyserver.{ProcessLog => NewPrLog}
import scala.io.Source
import java.io.File

/**
 * Convert JDBM2-based process log to MapDB.
 */
object Jdbm2MapDb {
  def main(args: Array[String]) {
    val jdbmFileName = args(0)
    val jdbmKeysFileName = args(1)
    val mapdbFileName = args(2)

    val jdbm = RecordManagerFactory.createRecordManager(jdbmFileName)
    val db =
      DBMaker.newFileDB(new File(mapdbFileName)).
      closeOnJvmShutdown().
      make()
    val fetch = getProcessLog(jdbm)_
    val copy = copyToMapDb(db.getHashMap("log"))_

    for {
      id <- readKeys(jdbmKeysFileName)
      obj = fetch(id)
      if obj != null
    } copy(id, obj)

    db.commit
  }

  def readKeys(filename: String): Iterator[String] = {
    val it = Source.fromFile(filename).getLines
    it.next()    // discard header
    for (line <- it) yield line.split(",")(0)
  }

  def getProcessLog(rm:RecordManager)(name:String): ProcessLog = {
    rm.getNamedObject(name) match {
      case n if n != 0 => rm.fetch(n).asInstanceOf[ProcessLog]
      case _ => null
    }
  }

  def copyToMapDb(map:java.util.Map[String,NewPrLog])
                 (key:String, log:ProcessLog) {
    val pl = new NewPrLog
    pl.setUri(log.getUri)
    if (log.isDated) pl.setDated
    println("adding " + key)
    map.put(key, pl)
  }
}

