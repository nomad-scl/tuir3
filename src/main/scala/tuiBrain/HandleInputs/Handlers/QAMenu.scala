package tuiBrain.HandleInputs.Handlers

import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.HandleInputs.Handlers.msgBoxes.{okMsg2JrnlEdit, okMsg2QA}
import tuiBrain.{AppData, CurPos, DeleteMe, MenuSelector, PopMode, UI, emptyEntry, loadDB}
import tuiBrain.HandleInputs.{InputHandlers, O}
import tuiBrain.InputMode.Normal
import tuiBrain.UI.Global.QABoundary
import tuiBrain.UI.SearchableList
import tuiBrain.brainDB.{getFromJournalByDate, getQAs}

import scala.util.Random

object QAMenu {
  case object SelectTopics4QA extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, qaManage).handle
      (r._1, r._2.getOrElse(this))
    }
  }

  private def qaManage(app : AppData, key : Key): (AppData, Option[InputHandlers]) = {
    app.menu.subMenu match{
      case "2" => subMenu2(app, key)
      case "3" => subMenu3(app, key)

      case _ => (app, Some(SelectTopics4QA))
    }
  }

  private def subMenu2(app: AppData, key: Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
    case _: KeyCode.Esc => app.menu.subMenu = "2";
      val tmp = getFromJournalByDate(UI.Global.nowDate);
      (app.copy(pop = PopMode.NoPop, menu = MenuSelector.Journaling, input_mode = Normal, journal = if tmp.isEmpty then Array(emptyEntry) else tmp.toArray),
        Some(JournalNormalMode))

    case _: KeyCode.Up if app.pop == PopMode.Topic =>
      app.filteredTopics.previous()
      (app, Some(SelectTopics4QA))
    case _: KeyCode.Down if app.pop == PopMode.Topic =>
      app.filteredTopics.next()
      (app, Some(SelectTopics4QA))

    case _: KeyCode.Backspace if app.pop == PopMode.Topic =>
      val k = if app.input.nonEmpty then app.copy(input = app.input.dropRight(1)) else app.copy(input = "")
      (app.copy(filteredTopics = SearchableList.StatefulList(items = app.topics.filter(_.contains(k.input)))), Some(SelectTopics4QA))

    case _: KeyCode.Enter if app.pop == PopMode.Topic => app.filteredTopics.getSelected match {
      case Some(s) if (app.menu == MenuSelector.QA) && (app.menu.subMenu == "2") =>
//        (app.copy(pop = PopMode.YesNo, input = s, tempBuffer = app.tempBuffer :+ (0, "")), Some(SelectTopics4QA))
        (app.copy(pop = PopMode.YesNo, input = s), Some(SelectTopics4QA))
      case _ => (app, Some(SelectTopics4QA))
    }
    case c: KeyCode.Char if app.pop == PopMode.Topic =>
      (app.copy(input = app.input + c.c, filteredTopics = SearchableList.StatefulList(items = app.topics.filter(_.contains(app.input + c.c))))
        , Some(SelectTopics4QA))

    case c: KeyCode.Char if (c.c == 'Y') && (app.pop == PopMode.YesNo) =>
      app.tempBuffer(app.tempBuffer.length - 1) = (0, app.input)
      (app.copy(pop = PopMode.Topic, tempBuffer = app.tempBuffer :+ (0, ""), input = ""), Some(SelectTopics4QA))

    case c: KeyCode.Char if (c.c == 'N') && (app.pop == PopMode.YesNo) =>
      (app.copy(pop = PopMode.Topic, input = ""), Some(SelectTopics4QA))

    case c: KeyCode.Char if (c.c == 'G') && (app.pop == PopMode.YesNo) =>
      app.menu.subMenu = "3"
      app.tempBuffer(app.tempBuffer.length - 1) = (0, app.input)
//      val subs = app.tempBuffer.map(_._2).distinct.map(TopicBrake + _ + TopicBolder + TopicBrake)
//      DeleteMe.tempFilteredQAs = DeleteMe.tempQAs.filter((m1, m2) => subs.exists((m1 + m2).contains(_)))
      (app.copy(pop = PopMode.YesNo, input = ""), Some(SelectTopics4QA))
    case _ => (app, Some(SelectTopics4QA))
  }

  private def subMenu3(app: AppData, key: Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
    case _: KeyCode.Esc => app.menu.subMenu = "2"; (app.copy(pop = PopMode.Topic), Some(SelectTopics4QA))

    case c: KeyCode.Char if (c.c == 'N') && (app.pop == PopMode.Card) && PopMode.Card.showAnswer =>
      PopMode.Card.showAnswer = false
      val in2 = app.input.split(" ")
      val in3 = in2(2).toInt + 1
      if in3 <= in2(4).toInt then
        (app.copy(curPos = CurPos(0, app.curPos.y + 1), input = s"Card { ${in3.toString} of ${in2(4)} }"), Some(SelectTopics4QA))
      else {
        app.menu.subMenu = "2"
        app.menu.msgBox = "You have finished this question round"

        (app.copy(curPos = CurPos(0, 0), input = "", tempBuffer = Array((0, "")), pop = PopMode.Topic, menu = MenuSelector.QA), Some(okMsg2QA))
      }

    case c: KeyCode.Char if (c.c == 'S') && (app.pop == PopMode.Card) && (!PopMode.Card.showAnswer) =>
      PopMode.Card.showAnswer = true
      (app, Some(SelectTopics4QA))

    case c: KeyCode.Char if (c.c == 'A') && (app.pop == PopMode.YesNo) =>
//      val mm = DeleteMe.tempFilteredQAs.map((m1, m2) => (0, m1 + QABoundary + m2))
      val mm = getQAs(app.tempBuffer.map(_._2)).toArray
      if mm.nonEmpty then
//        (app.copy(tempBuffer = mm, curPos = CurPos(0, 0), pop = PopMode.Card, input = "Card { 1 of " + DeleteMe.tempFilteredQAs.length.toString + " }")
        (app.copy(journal = mm, curPos = CurPos(0, 0), pop = PopMode.Card, input = "Card { 1 of " + mm.length + " }")
          , Some(SelectTopics4QA))
      else {
        app.menu.subMenu = "2"
        app.menu.msgBox = "There are no questions with these selected topics"
        (app.copy(tempBuffer = Array((0, "")), input = "", pop = PopMode.Topic), Some(okMsg2QA))
      }

    case c: KeyCode.Char if c.c.isDigit && (app.pop == PopMode.YesNo) => (app.copy(input = app.input + c.c), Some(SelectTopics4QA))

    case _: KeyCode.Enter if app.pop == PopMode.YesNo =>
//      val mm = DeleteMe.tempFilteredQAs.map((m1, m2) => (0, m1 + QABoundary + m2)).take(1)
      val n2 = if app.input.toInt != 0 then app.input.toInt else 1
      val mm = getQAs(app.tempBuffer.map(_._2), n2).toArray

      val n = if n2 > mm.length then mm.length else n2
      if mm.nonEmpty then
//        (app.copy(tempBuffer = (1 to n).map(_ => mm(Random.nextInt(mm.length))).toArray, curPos = CurPos(0, 0), pop = PopMode.Card,
        (app.copy(journal = mm, curPos = CurPos(0, 0), pop = PopMode.Card, input = "Card { 1 of " + n.toString + " }"), Some(SelectTopics4QA))
      else {
        app.menu.subMenu = "2"
        app.menu.msgBox = "There are no questions with these selected topics"
        (app.copy(tempBuffer = Array((0, "")), input = "", pop = PopMode.Topic), Some(okMsg2QA))
      }

    case _ => (app, Some(SelectTopics4QA))
  }

}
