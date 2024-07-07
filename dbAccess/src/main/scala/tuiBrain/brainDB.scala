package tuiBrain

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import tuiBrain.GlobalDB.*
import tuiBrain.GlobalDB.given
import cats.effect.*
import cats.*
import org.sqlite.SQLiteConfig.JournalMode
import cats.implicits.*

import java.text.SimpleDateFormat
import java.util.Date
import doobie.*
import doobie.util.fragment
import cats.effect.unsafe.implicits.global

object brainDB {
  private def defTables(using transactor: Resource[IO, HikariTransactor[IO]]) : List[String] = {
    val topics : Fragment = tableINE("topics").nl.inter("ID").pk.ai.nx
      .text("topicName").nl.fin

    val journal: Fragment = tableINE("journal").nl.inter("ID").notNull.pk.ai.nx
      .real("roworder").notNull.nx
      .dater("dateadded").notNull.nx
      .dater("dateupdated").notNull.nx
      .text("entrytype").notNull.nx
      .inter("msglevel").nx
      .text("messages").notNull.nx
      .text("topics").notNull.nl
      .fin

    val k = List(topics.getSql, journal.getSql)
    runUpdate(k).unsafeRunSync()

    val sql = select("topicName", "topics")
    val lst = query[String](sql, taker = Int.MaxValue).unsafeRunSync()

    runUpdate(lst.map(tblName => tableINE(tblName).nl.inter("ID").pk.ai.nx.inter("journalID").nx.real("roworder").nl.fin.getSql)).unsafeRunSync()
    lst
  }

  def createDefaultTables(): List[String] = defTables

  def createTopicTable(tbl : String) : Unit = {
    runUpdate(tableINE(tbl).nl.inter("ID").pk.ai.nx.inter("journalID").nx.real("roworder").nl.fin).unsafeRunSync()
    runUpdate(insert("topics", "topicName","\'" + tbl + "\'")).unsafeRunSync()
  }

  private def journalGetter(col : String, whereVal : String)(using transactor: Resource[IO, HikariTransactor[IO]]) : List[JournalEntry] = {
    val sel = select(s"${JournalEntry.ID}, ${JournalEntry.order}, ${JournalEntry.added}, ${JournalEntry.updated}, ${JournalEntry.entrytype}, ${JournalEntry.msglevel}, ${JournalEntry.messages}, ${JournalEntry.topics}", "journal")
      .whereT(col, whereVal).order(JournalEntry.order)
    query[JournalEntry](sel).unsafeRunSync()
  }

  private def likeGetter(col : String, whereVal : String)(using transactor: Resource[IO, HikariTransactor[IO]]) : List[JournalEntry] = {
    val sel = select(s"${JournalEntry.ID}, ${JournalEntry.order}, ${JournalEntry.added}, ${JournalEntry.updated}, ${JournalEntry.entrytype}, ${JournalEntry.msglevel}, ${JournalEntry.messages}, ${JournalEntry.topics}", "journal")
      .like(col, whereVal).order(JournalEntry.order)
    query[JournalEntry](sel).unsafeRunSync()
  }

  def getQAs(topics : Array[String], number : Int = -1) : List[(EditStatus, JournalEntry)] = {
    if topics.isEmpty then return List()
    val tp = topics.map(z => s"topics Like \'%$z%\'").mkString(" OR ")

    val liker = if number > 0 then Fragment.const(s"WHERE entrytype = \'QA\' AND ($tp) ORDER BY RANDOM() LIMIT $number")
    else Fragment.const(s"WHERE entrytype = \'QA\' AND ($tp) ORDER BY RANDOM()")

    val sel = select(s"${JournalEntry.ID}, ${JournalEntry.order}, ${JournalEntry.added}, ${JournalEntry.updated}, " +
      s"${JournalEntry.entrytype}, ${JournalEntry.msglevel}, ${JournalEntry.messages}, ${JournalEntry.topics}", "journal") ++ liker
    query[JournalEntry](sel).unsafeRunSync().map((DoNothing, _))
  }

  private def checkLegalDate(dt : String) : Boolean = {
    val k = dt.split("-")
    if k.length != 3 then return false
    val year = k(0).toIntOption.getOrElse(-1)
    val month = k(1).toIntOption.getOrElse(-1)
    val day = k(2).toIntOption.getOrElse(-1)

    if (year < 1900) || (year > 2500) then false
    else if (month < 1) || (month > 12) then false
    else if (day < 1) || (day > 31) then false
    else true
  }
  def getFromJournalByDate(dt : String) : List[(EditStatus, JournalEntry)] = if checkLegalDate(dt) then journalGetter(JournalEntry.added, dt).map((DoNothing, _))
                                                                else List()

  def getFromJournalByTopic(tp : String) : List[(EditStatus, JournalEntry)] = likeGetter(JournalEntry.topics, tp).map((DoNothing, _))

  def updateRows(rows : Array[(EditStatus, JournalEntry)]) : Unit = {
    val latch = Deferred[IO, Unit].unsafeRunSync()
    val k = rows.map(_._2.makeUpdateFragment)
    val gg = k.map(wer => latch.get *> runUpdate(wer)).toList
    val ttt = for {
      fibers <- gg.traverse(_.start)
      rty <- latch.complete(()) *> fibers.traverse(_.join)
    } yield rty
    val res = ttt.unsafeRunSync()
    val up = rows.map(v => (v._1.asInstanceOf[tuiBrain.Update], v._2.ID, v._2.roworder))

    val l2 = Deferred[IO, Unit].unsafeRunSync()
    val del = up.map(v => (v._1.del, v._2)).collect { case (Some(x), id) => (x, id) }.flatMap{ row =>
      row._1.map{ topic => delete(topic).where("journalID", row._2.toString) } }.map {wer => l2.get *> runUpdate(wer)}.toList

    (for{
      fibers <- del.traverse(_.start)
      rty <- l2.complete(()) *> fibers.traverse(_.join)
    }yield rty).unsafeRunSync()

    val l3 = Deferred[IO, Unit].unsafeRunSync()
    val add = up.map(v => (v._1.add, v._2, v._3)).collect { case (Some(x), id, ord) => (x, id, ord) }.flatMap { row =>
      row._1.map { topic => insert(topic, "journalID, roworder", s"${row._2}") }
    }.map { wer => l3.get *> runUpdate(wer) }.toList

    (for {
      fibers <- add.traverse(_.start)
      rty <- l3.complete(()) *> fibers.traverse(_.join)
    } yield rty).unsafeRunSync()
  }

  def deleteRows(rows : Array[(EditStatus, JournalEntry)]) : Unit = {
    val latch = Deferred[IO, Unit].unsafeRunSync()
    val k = rows.map(_._2.makeDeleteFragment)
    val gg = k.map(wer => latch.get *> runUpdate(wer)).toList
    val ttt = for {
      fibers <- gg.traverse(_.start)
      rty <- latch.complete(()) *> fibers.traverse(_.join)
    } yield rty
    val res = ttt.unsafeRunSync()

    val up = rows.map(v => (v._1.asInstanceOf[tuiBrain.Delete], v._2.ID))
    val l2 = Deferred[IO, Unit].unsafeRunSync()
    val del = up.map(v => (v._1.del, v._2)).collect { case (Some(x), id) => (x, id) }.flatMap{ row =>
      row._1.map{ topic => delete(topic).where("journalID", row._2.toString) } }.map {wer => l2.get *> runUpdate(wer)}.toList

    (for{
      fibers <- del.traverse(_.start)
      rty <- l2.complete(()) *> fibers.traverse(_.join)
    }yield rty).unsafeRunSync()
  }
  
  def insertRows(rows : Array[(EditStatus, JournalEntry)]) : Unit = {
    val latch = Deferred[IO, Unit].unsafeRunSync()
    val k = rows.map(_._2.makeInsertFragment)
    val gg = k.map(wer => latch.get *> runUpdate(wer)).toList
    val ttt = for {
      fibers <- gg.traverse(_.start)
      rty <- latch.complete(()) *> fibers.traverse(_.join)
    } yield rty

    val res = ttt.unsafeRunSync()
    val l2 = Deferred[IO, Unit].unsafeRunSync()
    val id = query[(Int, Double, String)](select("ID, roworder, topics", "journal").desc("ID").limit(rows.length)).unsafeRunSync()
    val oo = id.flatMap { row =>
      row._3.split(",").map{ tp =>
        if tp == "" then None
        else Some(insert(tp, "journalID, roworder", s"${row._1}, ${row._2}"))
      }
    }
    val g2 = oo.collect{ case Some(x) => x }.map(wer => l2.get *> runUpdate(wer))
    val t2 = for {
      fibers <- g2.traverse(_.start)
      rty <- l2.complete(()) *> fibers.traverse(_.join)
    } yield rty

    t2.unsafeRunSync()
  }

  private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
  given Get[Date] = Get[String].tmap(dateFormat.parse)
  given Put[Date] = Put[String].tcontramap(dateFormat.format)


}
