package tuiBrain

import tui.*
import tuiBrain.UI.SearchableList
import tuiBrain.UI.SearchableList.StatefulList
import tuiBrain.brainDB.getFromJournalByDate
import tuiBrain.HandleInputs.Handlers.JournalNormalMode

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val currentDate : String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-dd"))

case class CurPos(x : Int, y : Int)

@main
def main(): Unit = {
  System.setProperty("slf4j.internal.verbosity", "ERROR")
  val res = loadDB()
  withTerminal { (jni, terminal) =>
    // create app and run it
    val app = AppData(topics = res._1, journal = res._2, filteredTopics = StatefulList(items = res._1))
    Handelers.run_app2(terminal, app, jni, JournalNormalMode)
  }
}

def loadDB() : (Array[String], Array[(EditStatus, JournalEntry)]) = {
  val topics = tuiBrain.brainDB.createDefaultTables().toArray
  val res = getFromJournalByDate(UI.Global.nowDate)
  (topics, if res.isEmpty then Array(emptyEntry) else res.toArray)
}