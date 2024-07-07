package tuiBrain.HandleInputs.Handlers

import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.HandleInputs.Handlers.msgBoxes.yesNoCreateTopic
import tuiBrain.{AppData, CurPos, MenuSelector, PopMode}
import tuiBrain.HandleInputs.{InputHandlers, O}
import tuiBrain.UI.Global.{TopicBolder, TopicBrake}
import tuiBrain.UI.SearchableList
import tuiBrain.UI.SearchableList.StatefulList

object TopicSelecting {
  case object SelectTopicEditJournal extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, jrnlEditTopic).handle
      (r._1, r._2.getOrElse(this))
    }
  }

  private def jrnlEditTopic(app : AppData, key : Key) : (AppData, Option[InputHandlers]) = selection(app, key, EditJournal, getSelected, 
    (c : Char) => c.isLetter || c == '_' || c.isDigit, escPress)

  private def filterList(app : AppData, str : String) : AppData = app.copy(input = str, filteredTopics = StatefulList(items = app.topics.filter(_.contains(str))))

  private def getSelected(app: AppData, res : InputHandlers): (AppData, Option[InputHandlers]) = app.filteredTopics.getSelected match {
    case None => if app.input.nonEmpty && (res == EditJournal)
    then (app.copy(pop = PopMode.YesNo), Some(yesNoCreateTopic))
    else (app.copy(pop = PopMode.NoPop, input = ""), Some(res))
    case Some(s) if res == EditJournal => val ttt = TopicBrake + s + TopicBolder + TopicBrake
      val line = app.getCurPosText.splitAt(app.curPos.x)
      app.addTopic(s)
      app.setCurText(line._1 + ttt + line._2, app.getCurPosType);
      (app.copy(pop = PopMode.NoPop,
        input = "",
        curPos = CurPos(line._1.length + ttt.length, app.curPos.y)), Some(res))

    case _ => (app, None)
  }

  private def escPress(app : AppData, res : InputHandlers) = (app.copy(pop = PopMode.NoPop, input = ""), Some(res))

  def selection(app : AppData, key : Key, result : InputHandlers, 
                gs : (AppData, InputHandlers) => (AppData, Option[InputHandlers]), 
                allowedChars : Char => Boolean,
                esc : (AppData, InputHandlers) => (AppData, Option[InputHandlers]))
  : (AppData, Option[InputHandlers]) =  key.keyEvent().code() match {
      case _: KeyCode.Up => app.filteredTopics.previous(); (app, Some(SelectTopicEditJournal))
      case _: KeyCode.Down => app.filteredTopics.next(); (app, Some(SelectTopicEditJournal))
      case _: KeyCode.Esc => esc(app, result)

      case c : KeyCode.Char if allowedChars(c.c()) => (filterList(app, app.input + c.c), Some(SelectTopicEditJournal))

      case _: KeyCode.Backspace => (filterList(app, if app.input.nonEmpty then app.input.dropRight(1)  else ""), Some(SelectTopicEditJournal))

      case _: KeyCode.Enter => gs(app, result)

      case _ => (app, None)
    }
}
