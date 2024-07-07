package tuiBrain
import tui.*
import tui.crossterm.{CrosstermJni, KeyCode, KeyModifiers}
import tuiBrain.ExitStatus.{Quit, Run}
import tuiBrain.HandleInputs.Handlers.{JournalNormalMode, QuitProgram}
import tuiBrain.HandleInputs.InputHandlers
import tuiBrain.UI.{PopUp, SearchableList, TerminalUI, subMenus}
import tuiBrain.UI.Global.*
import tuiBrain.UI.SearchableList.{handleInput, listOfString}
import tuiBrain.brainDB._
import java.util.Date
import scala.annotation.tailrec

object Handelers {

  @tailrec
  def run_app2(terminal: Terminal, app: AppData, jni: CrosstermJni, handler : InputHandlers): Unit = {

    terminal.draw(f => UI.TerminalUI.ui2(f, app))

    val res : (AppData, InputHandlers) = jni.read() match {
      case key: tui.crossterm.Event.Key =>
        handler.handleInput(app, key)
//        app.menu.msgBox match {
//          case "" => app.menu match {
//            case MenuSelector.Journaling => journal(app, key)
//            case MenuSelector.QA => qaInput(app, key)
//          }
//          case str => viewMessage(app, key)
//        }
      case _ => (app, handler)
    }
//    println(res._2)
    if res._2 != QuitProgram then run_app2(terminal, res._1, jni, res._2)
  }

//  private def viewMessage(app : AppData, key : tui.crossterm.Event.Key) : (AppData, ExitStatus) = key.keyEvent().code() match {
//    case c : KeyCode.Char if c.c == 'O' =>
//      app.menu.msgBox = ""
//      (app, ExitStatus.Run)
//    case _ => (app, ExitStatus.Run)
//  }
//
//  private def qaInput(app : AppData, key : tui.crossterm.Event.Key) : (AppData, ExitStatus) = {
//    if app.pop == PopMode.Topic then key.keyEvent().code() match {
//        case _: KeyCode.Up if app.pop == PopMode.Topic => (d8(app), ExitStatus.Run)
//        case _: KeyCode.Down if app.pop == PopMode.Topic => (d9(app), ExitStatus.Run)
//        case _ => (app, ExitStatus.Run)
//    } else app.menu.subMenu match{
//      case "0" => subMenus.subMenu0(app, key)
//      case "2" => (subMenus.subMenu2(app, key), ExitStatus.Run)
//      case "3" => (subMenus.subMenu3(app, key), ExitStatus.Run)
//      case "4" => (subMenus.subMenu4(app, key), ExitStatus.Run)
//
//      case _ => (app, ExitStatus.Run)
//
//    }
//  }

//  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  private def journal(app : AppData, key : tui.crossterm.Event.Key) : (AppData, ExitStatus) = app.input_mode match {
//    case InputMode.Normal => normalMode2(key, app)
//
////    case InputMode.Editing => (editingMode2(key, app), ExitStatus.Run)
//  }
//  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  private def normalMode2(key: tui.crossterm.Event.Key, app: AppData): (AppData, ExitStatus) = key.keyEvent().code() match {
////    case c: KeyCode.Char if c.c == 'e' => (app.copy(input_mode = InputMode.Editing), ExitStatus.Run)
////    case c: KeyCode.Char if c.c == 'q' => (app, ExitStatus.Quit)
//
////    case c: KeyCode.Char if c.c == 'K' => (app.copy(menu = MenuSelector.QA, messages = Array((0, ""))), ExitStatus.Run)
////    case c: KeyCode.Char if c.c == 'K' => (app.copy(menu = MenuSelector.QA), ExitStatus.Run)
//
//    case _ => (app, ExitStatus.Run)
//  }

//  private def insertNewLine(app : AppData) : AppData = {
//    val mmm = new Array[(EditStatus, JournalEntry)](app.journal.length + 1)
//    Array.copy(app.journal, 0, mmm, 0, app.curPos.y + 1)
//    mmm(app.curPos.y + 1) = (Insert, JournalEntry(-1,app.getNewOrder, Date(), Date(), "", None, 0, "", ""))
//    Array.copy(app.journal, app.curPos.y + 1 , mmm, app.curPos.y + 2, app.journal.length - app.curPos.y - 1)
//    app.copy(input = "", journal = mmm, curPos = CurPos(0, app.curPos.y + 1))
//  }
//  private def popTopicList(app : AppData) : AppData =
//    app.copy(pop = PopMode.Topic, input = "", filteredTopics = SearchableList.StatefulList(items = app.topics))
//  private def addChar2MessageAtPos(app : AppData, c: KeyCode.Char) : AppData = {
//    val cr = app.getCurPosText.splitAt(app.gX)
//    val k = app.pop match{
//      case PopMode.NoPop =>
//        app.setCurText(cr._1 + c.c() + cr._2, app.getCurPosType)
//        app.copy(curPos = app.curPos.copy(x = app.curPos.x + 1))
//      case _ => app
//    }
//    k
//  }
//  private def increaseLevel(app : AppData) : AppData = {
////    app.messages(app.curPos.y) = (app.messages(app.curPos.y)._1 + 1, app.messages(app.curPos.y)._2)
//    app.increaseCurrentLevel()
//    app
//  }
//
//  private def removeChar(app : AppData) : AppData = {
//    val cr = app.getCurPosText.splitAt(app.curPos.x)
//
//    val k = if cr._1.endsWith(TopicBrake) then {
//      val pos = cr._1.lastIndexOf(TopicBrake, cr._1.lastIndexOf(TopicBrake) - 1)
//      val cnt = app.getCurPosText.sliding(TopicBolder.length).count(_ == TopicBolder)
//      if (cnt == 1) && (app.getCurPosText.contains(QABoundary) || app.getCurPosText.contains(eventBoundary) || (app.getCurPosText(0) == '['))
//      then {
//        app.menu.msgBox = "You cannot delete the last topic from a question, event or task>"
//        app
//      }else {
//        app.setCurText(cr._1.substring(0, pos) + cr._2, app.getCurPosType)
//        app.removeTopic(cr._1.split(TopicBrake).last.split(TopicBolder)(0))
//        app.copy(curPos = app.curPos.copy(x = pos))
//      }
//    } else if cr._1.endsWith(eventBoundary) then {
//      val pos = cr._1.lastIndexOf(eventBoundary, cr._1.lastIndexOf(eventBoundary) - 1)
//      app.setCurText(cr._1.substring(0, pos) + cr._2, messageTypes.text)
//      app.copy(curPos = app.curPos.copy(x = pos))
//    }else if cr._1.endsWith(QABoundary) then {
//      app.setCurText(cr._1.dropRight(QABoundary.length) + cr._2, messageTypes.text)
//      app.copy(curPos = app.curPos.copy(x = cr._1.length - QABoundary.length))
//    }
//    else if cr._1.replaceFirst("\\[.]", "").nonEmpty then
//      app.setCurText(cr._1.dropRight(1) + cr._2, app.getCurPosType)
//      app.copy(curPos = app.curPos.copy(app.curPos.x - 1))
//    else {
//      val msgs = if app.journal sameElements Array(emptyEntry)
//      then Array[(EditStatus, JournalEntry)](emptyEntry)
//      else if cr._2.isEmpty then app.journal.zipWithIndex.filter(_._2 != app.curPos.y).map(_._1)
//      else app.journal
//
//      val np = if app.curPos.y == 0 then 0 else app.curPos.y - 1
//      val k2 = if msgs sameElements Array(emptyEntry) then app.copy(curPos = CurPos(0, 0)) else
//        app.copy(input = "", journal = msgs, curPos = CurPos(msgs(np)._2.message.length, np))
//      k2
//    }
//    k
//  }
  //d8 could be removed since it is used by qainput
//  private def d8(app : AppData) : AppData = {
//    SearchableList.listOfString.previous()
//    app
//  }
//  //d9 could be removed since it is used by qainput
//  private def d9(app : AppData) : AppData = {
//    SearchableList.listOfString.next()
//    app
//  }
//  private def moveDown(app : AppData) : AppData = {
//    val np = if (app.curPos.y + 1) >= app.journal.length then 0 else app.curPos.y + 1
//    app.copy(curPos = CurPos(app.journal(np)._2.message.length, np))
//  }
//  private def moveUp(app : AppData) : AppData = {
//    if app.journal.nonEmpty then
//      val np = if (app.curPos.y - 1) < 0 then app.journal.length - 1 else app.curPos.y - 1
//      app.copy(curPos = CurPos(app.journal(np)._2.message.length, np))
//    else app.copy(curPos = CurPos(0, 0))
//  }
//  private def moveRight(app : AppData) : AppData = {
//    if app.journal.isEmpty || ((app.journal.length == 1) && (app.journal(0)._2.message == "")) then app.copy(curPos = CurPos(0,0)) else {
//      val cr = app.getCurPosText.splitAt(app.curPos.x)
//      val np = if cr._2.startsWith(TopicBrake) then cr._2.indexOf(TopicBrake, cr._2.indexOf(TopicBrake) + 1) + TopicBrake.length + cr._1.length
//      else if cr._2.startsWith(QABoundary) then QABoundary.length + cr._1.length
//      else if cr._2.startsWith(eventBoundary) then cr._2.indexOf(eventBoundary, cr._2.indexOf(eventBoundary) + 1) + eventBoundary.length + cr._1.length
//      else if "^\\[.]".r.findFirstIn(cr._1).isDefined && (app.curPos.x > (app.getCurPosText.length - 1)) then 3
//      else if app.curPos.x > (app.getCurPosText.length - 1) then 0
//      else app.curPos.x + 1
//      app.copy(curPos = CurPos(np, app.curPos.y))
//    }
//  }
//  private def moveLeft(app : AppData) : AppData = {
//    if app.journal.isEmpty || ((app.journal.length == 1) && app.journal(0)._2.message == "") then app.copy(curPos = CurPos(0,0))
//    else if app.getCurPosText.nonEmpty then {
//      val cr = app.getCurPosText.splitAt(app.curPos.x)
//      val np = if cr._1.endsWith(TopicBrake) then cr._1.lastIndexOf(TopicBrake, cr._1.lastIndexOf(TopicBrake) - 1)
//      else if cr._1.endsWith(QABoundary) then cr._1.lastIndexOf(QABoundary)
//      else if cr._1.endsWith(eventBoundary) then cr._1.lastIndexOf(eventBoundary, cr._1.lastIndexOf(eventBoundary) - 1)
//      else if "^\\[.]".r.findFirstIn(cr._1).isDefined && (app.curPos.x <= 3) then app.getCurPosText.length
//      else if app.curPos.x <= 0 then app.getCurPosText.length
//      else app.curPos.x - 1
//      app.copy(curPos = CurPos(np, app.curPos.y))
//    }
//    else app.copy(curPos = CurPos(0, app.curPos.y))
//  }
//  private def decreaseLevel(app : AppData) : AppData = {
//    app.decreaseCurrentLevel()
////    app.messages(app.curPos.y) = (if app.messages(app.curPos.y)._1 > 0 then app.messages(app.curPos.y)._1 - 1 else 0, app.messages(app.curPos.y)._2)
//    app
//  }
//  def getSelected(app: AppData): AppData = SearchableList.listOfString.getSelected match {
//    case None => if app.input.nonEmpty && (app.menu == MenuSelector.Journaling)
//    then app.copy(pop = PopMode.YesNo, input = s"Do you want to create a new topic \"${app.input}\"?")
//                  else app
//    case Some(s) if app.menu == MenuSelector.Journaling => val ttt = TopicBrake + s + TopicBolder + TopicBrake
//      val line = app.getCurPosText.splitAt(app.curPos.x)
//      app.setCurText(line._1 + ttt + line._2, app.getCurPosType)
//      app.copy(pop = PopMode.NoPop,
//        input = "",
//        curPos = CurPos(line._1.length + ttt.length, app.curPos.y))
//
//    case Some(s) if (app.menu == MenuSelector.QA) && (app.menu.subMenu == "2") => app.copy(pop = PopMode.YesNo, input = s)
//    case Some(s) if (app.menu == MenuSelector.QA) && (app.menu.subMenu == "1") => app.copy(pop = PopMode.NoPop, input = s)
//    case _ => app
//  }

//  private def editingMode2(key: tui.crossterm.Event.Key, app: AppData): AppData =
//    if app.pop == PopMode.Event then PopUp.popEvent(app, key)
//    else if app.pop == PopMode.Topic then handleInput(app, key, listOfString, getSelected)
//    else if app.pop == PopMode.Topic then handleInput(app, key, getSelected)
//    else key.keyEvent().code() match {
//
////      case c: KeyCode.Char if (c.c == 'k') && key.isControl && (app.pop == PopMode.NoPop) =>
////        val k = app.getCurPosText.take(3) match {
////          case "[\u2713]" => ("[X]", 3)
////          case "[ ]" => ("[\u2713]", 3)
////          case "[X]" => ("", 0)
////          case s : String => ("[ ]", 3)
////        }
////        if !app.getCurPosText.contains(TopicBrake) then app.menu.msgBox = "A task must have a topic"
////        else if app.getCurPosText.contains(QABoundary) then app.menu.msgBox = "A question cannot become a task"
////        else if app.getCurPosText.contains(eventBoundary) then app.menu.msgBox = "An event cannot become a task"
////        else app.setTask(k._1)
////        app.copy(curPos = app.curPos.copy(x = app.curPos.x + k._2))
//
////      case c: KeyCode.Char if (c.c == 't') && key.isControl && (app.pop == PopMode.NoPop) => popTopicList(app)
//
////      case c: KeyCode.Char if (c.c == 'a') && key.isControl && (app.pop == PopMode.NoPop) && (!app.getCurPosText.contains(QABoundary)) =>
////        if app.getCurPosText.contains(eventBoundary) then app.menu.msgBox = "An event cannot become a question"
////        else if app.getCurPosText.contains(TopicBrake) then app.menu.msgBox = "A question must have a topic"
////        else if app.getCurPosText(0) == '[' then app.menu.msgBox = "A task cannot become a question"
////        else{
////          val spl = app.getCurPosText.splitAt(app.curPos.x)
////          app.setCurText(spl._1 + QABoundary + spl._2, messageTypes.qa)
////        }
////        app.copy(curPos = app.curPos.copy(x = app.getCurPosText.length))
//
////      case c: KeyCode.Char if (c.c == 'e') && key.isControl && (app.pop == PopMode.NoPop) =>
////        if app.getCurPosText.contains(QABoundary) then {
////          app.menu.msgBox = "A question cannot be an event"
////          app
////        }else if app.getCurPosText.contains(eventBoundary) then {
////          app.menu.msgBox = "An event already exists"
////          app
////        }else if app.getCurPosText(0) == '[' then {
////          app.menu.msgBox = "A task cannot be an event"
////          app
////        }
////        else if !app.getCurPosText.contains(TopicBrake) then {
////          app.menu.msgBox = "An event must have a topic"
////          app
////        }
////        else {
////          PopMode.Event.action = 0
////          app.copy(pop = PopMode.Event, input = "")
////        }
//
//
////      case c: KeyCode.Char => addChar2MessageAtPos(app, c)
////
////      case _: KeyCode.Enter if app.pop == PopMode.NoPop => insertNewLine(app)
////      case _: KeyCode.Tab if app.pop == PopMode.NoPop => increaseLevel(app)
////      case _: KeyCode.BackTab if app.pop == PopMode.NoPop => decreaseLevel(app)
////      case _: KeyCode.Backspace if app.pop == PopMode.NoPop => removeChar(app)
////      case _: KeyCode.Esc if app.pop == PopMode.NoPop =>
////        //TODO: ask for save confirmation
////        app.copy(input_mode = InputMode.Normal)
////      case _: KeyCode.Up if app.pop == PopMode.NoPop => moveUp(app)
////      case _: KeyCode.Down if app.pop == PopMode.NoPop => moveDown(app)
////      case _: KeyCode.Right if app.pop == PopMode.NoPop => moveRight(app)
////      case _: KeyCode.Left if app.pop == PopMode.NoPop => moveLeft(app)
//
//      case _ => app
//    }

  //////////////////////////////////////////////////////////////////////////////////
//  extension (key: tui.crossterm.Event.Key)
//    def isControl : Boolean = key.keyEvent().modifiers().bits() == KeyModifiers.CONTROL

}
