package tuiBrain

import doobie.{Fragment, Get, Put}
import tuiBrain.GlobalDB._

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

val formatter = SimpleDateFormat("yyyy-M-dd")

case class JournalEntry(ID : Int, roworder : Double, added : Date, updated : Date,
                        entryTp : String, msglevel : Int, message : String, topics : String){
  def makeInsertFragment : Fragment = {
    val cols : String = s"${JournalEntry.order}, ${JournalEntry.added}, ${JournalEntry.updated}, ${JournalEntry.entrytype}, ${JournalEntry.msglevel}, ${JournalEntry.messages}, ${JournalEntry.topics}"
//    val cols : String = s"${JournalEntry.order}, ${JournalEntry.added}, ${JournalEntry.updated}, ${JournalEntry.entrytype}, ${JournalEntry.notifydate}, ${JournalEntry.msglevel}, ${JournalEntry.messages}, ${JournalEntry.topics}"
//    val not = if notifyDate.isEmpty then "NULL, " else s"\'${formatter.format(notifyDate.get)}\', "
//    val values : String = s"$roworder, \'${formatter.format(added)}\', \'${formatter.format(updated)}\', \'$entryTp\',$not $msglevel, \'$message\', \'$topics\'"
    val values : String = s"$roworder, \'${formatter.format(added)}\', \'${formatter.format(updated)}\', \'$entryTp\', $msglevel, \'$message\', \'$topics\'"
    insert("journal", cols, values)
  }

  def makeUpdateFragment: Fragment = {
    val cols = Array(JournalEntry.order, JournalEntry.added, JournalEntry.updated, JournalEntry.entrytype, JournalEntry.msglevel, JournalEntry.messages, JournalEntry.topics)
//    val cols = Array(JournalEntry.order, JournalEntry.added, JournalEntry.updated, JournalEntry.entrytype, JournalEntry.notifydate, JournalEntry.msglevel, JournalEntry.messages, JournalEntry.topics)
//    val not = if notifyDate.isEmpty then "NULL" else s"\'${formatter.format(notifyDate.get)}\', "
//    val values = Array(roworder, s"\'${formatter.format(added)}\'", s"\'${formatter.format(updated)}\'", s"\'$entryTp\'", not, msglevel, s"\'$message\'", s"\'$topics\'")
    val values = Array(roworder, s"\'${formatter.format(added)}\'", s"\'${formatter.format(updated)}\'", s"\'$entryTp\'", msglevel, s"\'$message\'", s"\'$topics\'")

    update("journal", cols.zip(values).map(z => s"${z._1} = ${z._2}").mkString(",")).where(JournalEntry.ID, this.ID.toString)
  }
  def makeDeleteFragment: Fragment = delete("journal").where(JournalEntry.ID, this.ID.toString)
}

//val emptyEntry = (Insert, JournalEntry(-1, 0,Date(), Date(), "Text", None, 0, "", ""))
val emptyEntry = (Insert, JournalEntry(-1, 0,Date(), Date(), "Text", 0, "", ""))

object JournalEntry {
  val ID = "ID"
  val order = "roworder"
  val added = "dateadded"
  val updated = "dateupdated"
  val entrytype = "entrytype"
//  val notifydate = "notifydate"
  val msglevel = "msglevel"
  val messages = "messages"
  val topics = "topics"
}

object messageTypes {
  val text : String = "Text"
  val qa : String = "QA"
  val event : String = "Event"
  val task : String = "Task"
}