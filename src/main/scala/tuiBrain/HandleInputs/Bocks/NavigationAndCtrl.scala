package tuiBrain.HandleInputs.Bocks

import tui.crossterm.Event.Key
import tuiBrain.{AppData, MenuSelector, PopMode, messageTypes}
import tui.crossterm.{KeyCode, KeyModifiers}
import tuiBrain.HandleInputs.InputHandlers
import tuiBrain.HandleInputs.Handlers.*
import tuiBrain.HandleInputs.Bocks.helpers.*
import tuiBrain.HandleInputs.Bocks.popUps.editJrnlEventChs
import tuiBrain.HandleInputs.Handlers.QAMenu.SelectTopics4QA
import tuiBrain.HandleInputs.Handlers.TopicSelecting.SelectTopicEditJournal
import tuiBrain.HandleInputs.Handlers.TopicView.SelectTopic2View
import tuiBrain.HandleInputs.Handlers.msgBoxes.okMsg2JrnlEdit
import tuiBrain.InputMode.{Editing, Normal}
import tuiBrain.UI.Global.*
import tuiBrain.UI.SearchableList

object NavigationAndCtrl {
  extension (key: tui.crossterm.Event.Key)
    private def isControl: Boolean = key.keyEvent().modifiers().bits() == KeyModifiers.CONTROL

  def journalNormalMenu(app : AppData, key : Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
      case c: KeyCode.Char if c.c == 'e' => (app.copy(input_mode = Editing), Some(EditJournal))
      case c: KeyCode.Char if c.c == 'q' => (app, Some(QuitProgram))
      case c: KeyCode.Char if c.c == 'T' => 
        (app.copy(pop = PopMode.Topic, input_mode = Normal, input="", menu = MenuSelector.Journaling
          , filteredTopics = SearchableList.StatefulList(items = app.topics)), Some(SelectTopic2View))

      case c: KeyCode.Char if c.c == 'K' =>
        MenuSelector.QA.subMenu = "2"
        (app.copy(menu = MenuSelector.QA, pop = PopMode.Topic), Some(SelectTopics4QA))

      case _ => (app, None)
  }

  def journalEditCtrl(app : AppData, key : Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
    case c: KeyCode.Char if (c.c == 'k') && key.isControl =>
      app.menu.msgBox = helpers.allowed(app.getCurPosText, "[")
      if app.menu.msgBox != "" then (app, Some(okMsg2JrnlEdit))
      else {
        val k = app.getCurPosText.take(3) match {
          case "[\u2713]" => ("[X]", 0)
          case "[ ]" => ("[\u2713]", 0)
          case "[X]" => ("", -3)
          case _ => ("[ ]", 3)
        }
        app.setTask(k._1)
        (app.copy(curPos = app.curPos.copy(x = app.curPos.x + k._2)), Some(EditJournal))
      }

    case c: KeyCode.Char if (c.c == 't') && key.isControl =>
      (app.copy(pop = PopMode.Topic, input = "", filteredTopics = SearchableList.StatefulList(items = app.topics)), Some(SelectTopicEditJournal))

    case c: KeyCode.Char if (c.c == 'a') && key.isControl && (!app.getCurPosText.contains(QABoundary)) =>
      app.menu.msgBox = helpers.allowed(app.getCurPosText, QABoundary)
      if app.menu.msgBox != "" then (app, Some(okMsg2JrnlEdit))
      else{
        val spl = app.getCurPosText.splitAt(app.curPos.x)
        app.setCurText(spl._1 + QABoundary + spl._2, messageTypes.qa)
        app.setQA()
        (app.copy(curPos = app.curPos.copy(x = app.getCurPosText.length)), Some(EditJournal))
      }

    case c: KeyCode.Char if (c.c == 'e') && key.isControl =>
      app.menu.msgBox = helpers.allowed(app.getCurPosText, QABoundary)
      if app.menu.msgBox != "" then (app, Some(okMsg2JrnlEdit))
      else{
        PopMode.Event.action = 0
        (app.copy(pop = PopMode.Event, input = ""), Some(editJrnlEventChs))
      }

    case _ => (app, None)
  }

  def journalEditCursorNavigation(app : AppData, key : Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
    case _: KeyCode.Up if app.pop == PopMode.NoPop => (moveUp(app), Some(EditJournal))
    case _: KeyCode.Down if app.pop == PopMode.NoPop => (moveDown(app), Some(EditJournal))
    case _: KeyCode.Right if app.pop == PopMode.NoPop => (moveRight(app), Some(EditJournal))
    case _: KeyCode.Left if app.pop == PopMode.NoPop => (moveLeft(app), Some(EditJournal))
    case _ => (app, None)
  }
}
