package tuiBrain.HandleInputs.Bocks

import tui.crossterm.KeyCode
import tuiBrain.{AppData, CurPos, Delete, EditStatus, Insert, JournalEntry, emptyEntry, messageTypes}
import tuiBrain.UI.Global.*

import java.util.Date
import scala.annotation.tailrec

object helpers {
  def allowed(text : String, alwd : String) : String = {
    if !text.contains(TopicBrake) then "this entry must have a topic first"
    else if text.contains(QABoundary) && (alwd != QABoundary) then "this entry is already a question"
    else if text.contains(eventBoundary) && (alwd != eventBoundary) then "this entry is already an event"
    else if (text(0) == '[') && (alwd != "[") then "this entry is already a task"
    else ""
  }

  @tailrec
  def moveDown(app: AppData): AppData = {
    val np = if (app.curPos.y + 1) >= app.journal.length then 0 else app.curPos.y + 1
    if app.journal.forall(_._1 == Delete) then app.copy(curPos = CurPos(0, 0))
    else if app.journal(np)._1 == Delete then moveDown(app)
    else app.copy(curPos = CurPos(app.journal(np)._2.message.length, np))
  }

  @tailrec
  def moveUp(app: AppData): AppData = {
    if app.journal.nonEmpty then
      val np = if (app.curPos.y - 1) < 0 then app.journal.length - 1 else app.curPos.y - 1
      if app.journal.forall(_._1 == Delete) then app.copy(curPos = CurPos(0, 0))
      else if app.journal(np)._1 == Delete then moveUp(app)
      else app.copy(curPos = CurPos(app.journal(np)._2.message.length, np))
    else app.copy(curPos = CurPos(0, 0))
  }

  def moveRight(app: AppData): AppData = {
    if app.journal.isEmpty || ((app.journal.length == 1) && (app.journal(0)._2.message == "")) then app.copy(curPos = CurPos(0, 0)) else {
      val cr = app.getCurPosText.splitAt(app.curPos.x)
      val np = if cr._2.startsWith(TopicBrake) then cr._2.indexOf(TopicBrake, cr._2.indexOf(TopicBrake) + 1) + TopicBrake.length + cr._1.length
      else if cr._2.startsWith(QABoundary) then QABoundary.length + cr._1.length
      else if cr._2.startsWith(eventBoundary) then cr._2.indexOf(eventBoundary, cr._2.indexOf(eventBoundary) + 1) + eventBoundary.length + cr._1.length
      else if "^\\[.]".r.findFirstIn(cr._1).isDefined && (app.curPos.x > (app.getCurPosText.length - 1)) then 3
      else if app.curPos.x > (app.getCurPosText.length - 1) then 0
      else app.curPos.x + 1
      app.copy(curPos = CurPos(np, app.curPos.y))
    }
  }

  def moveLeft(app: AppData): AppData = {
    if app.journal.isEmpty || ((app.journal.length == 1) && app.journal(0)._2.message == "") then app.copy(curPos = CurPos(0, 0))
    else if app.getCurPosText.nonEmpty then {
      val cr = app.getCurPosText.splitAt(app.curPos.x)
      val np = if cr._1.endsWith(TopicBrake) then cr._1.lastIndexOf(TopicBrake, cr._1.lastIndexOf(TopicBrake) - 1)
      else if cr._1.endsWith(QABoundary) then cr._1.lastIndexOf(QABoundary)
      else if cr._1.endsWith(eventBoundary) then cr._1.lastIndexOf(eventBoundary, cr._1.lastIndexOf(eventBoundary) - 1)
      else if "^\\[.]".r.findFirstIn(cr._1).isDefined && (app.curPos.x <= 3) then app.getCurPosText.length
      else if app.curPos.x <= 0 then app.getCurPosText.length
      else app.curPos.x - 1
      app.copy(curPos = CurPos(np, app.curPos.y))
    }
    else app.copy(curPos = CurPos(0, app.curPos.y))
  }

  def addChar2MessageAtPos(app: AppData, c: KeyCode.Char): AppData = {
    val cr = app.getCurPosText.splitAt(app.gX)
    app.setCurText(cr._1 + c.c() + cr._2, app.getCurPosType)
    app.copy(curPos = app.curPos.copy(x = app.curPos.x + 1))
  }

  def increaseLevel(app: AppData): AppData = {
    app.increaseCurrentLevel()
    app
  }

  def removeChar(app: AppData): AppData = {
    val cr = app.getCurPosText.splitAt(app.curPos.x)

    val k = if cr._1.endsWith(TopicBrake) then {
      val pos = cr._1.lastIndexOf(TopicBrake, cr._1.lastIndexOf(TopicBrake) - 1)
      val cnt = app.getCurPosText.sliding(TopicBolder.length).count(_ == TopicBolder)
      if (cnt == 1) && (app.getCurPosText.contains(QABoundary) || app.getCurPosText.contains(eventBoundary) || (app.getCurPosText(0) == '['))
      then {
        app.menu.msgBox = "You cannot delete the last topic from a question, event or task>"
        app
      } else {
        app.setCurText(cr._1.substring(0, pos) + cr._2, app.getCurPosType)
        app.removeTopic(cr._1.split(TopicBrake).last.split(TopicBolder)(0))
        app.copy(curPos = app.curPos.copy(x = pos))
      }
    } else if cr._1.endsWith(eventBoundary) then {
      val pos = cr._1.lastIndexOf(eventBoundary, cr._1.lastIndexOf(eventBoundary) - 1)
      app.setCurText(cr._1.substring(0, pos) + cr._2, messageTypes.text)
      app.copy(curPos = app.curPos.copy(x = pos))
    } else if cr._1.endsWith(QABoundary) then {
      app.setCurText(cr._1.dropRight(QABoundary.length) + cr._2, messageTypes.text)
      app.copy(curPos = app.curPos.copy(x = cr._1.length - QABoundary.length))
    }
    else if cr._1.replaceFirst("\\[.]", "").nonEmpty then
      app.setCurText(cr._1.dropRight(1) + cr._2, app.getCurPosType)
      app.copy(curPos = app.curPos.copy(app.curPos.x - 1))
    else {
      val msgs = if app.isEmpty then app.journal
      else if cr._2.isEmpty then app.deleteCurrent()
      else app.journal

      val np = if app.curPos.y == 0 then 0 else app.curPos.y - 1
      val k2 = if app.isEmpty then app.copy(curPos = CurPos(0, 0)) else
        app.copy(input = "", journal = msgs, curPos = CurPos(msgs(np)._2.message.length, np))
      k2
    }
    k
  }
  
  def decreaseLevel(app: AppData): AppData = {
    app.decreaseCurrentLevel()
    app
  }
  
  def insertNewLine(app : AppData) : AppData = {
    val mmm = new Array[(EditStatus, JournalEntry)](app.journal.length + 1)
    Array.copy(app.journal, 0, mmm, 0, app.curPos.y + 1)
    mmm(app.curPos.y + 1) = (Insert, JournalEntry(-1,app.getNewOrder, Date(), Date(), "Text", 0, "", ""))
    Array.copy(app.journal, app.curPos.y + 1 , mmm, app.curPos.y + 2, app.journal.length - app.curPos.y - 1)
    app.copy(input = "", journal = mmm, curPos = CurPos(0, app.curPos.y + 1))
  }

  def checkWeek(c: Char): String = c match {
    case 'S' => "Sunday"
    case 'M' => "Monday"
    case 'T' => "Tuesday"
    case 'W' => "Wednesday"
    case 'H' => "Thursday"
    case 'F' => "Friday"
    case 'A' => "Saturday"
    case _ => ""
  }

  def wk2Ltr(str: String): String = str.toUpperCase match {
    case "SATURDAY" => "A"
    case "THURSDAY" => "H"
    case wk => wk(0).toString
  }

  def checkDate(dt: String): Array[Option[String]] = dt.split('\\').map {
    case "*" => Some(-5)
    case i => i.toIntOption
  }.zipWithIndex.map {
    case (None, i) => None
    case (Some(s), 0) if (s >= 2020) && (s <= 2500) => Some(s.toString)
    case (Some(s), 0) if s == -5 => Some("*")
    case (Some(s), 1) if (s >= 1) && (s <= 12) => Some(s.toString)
    case (Some(s), 1) if s == -5 => Some("*")
    case (Some(s), 2) if (s >= 1) && (s <= 31) => Some(s.toString)
    case (Some(s), 2) if s == -5 => Some("*")
    case _ => None
  }
}
