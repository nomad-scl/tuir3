package tuiBrain.HandleInputs.Handlers

import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.{AppData, MenuSelector, PopMode, UI, emptyEntry}
import tuiBrain.UI.Global.*
import tuiBrain.HandleInputs.*
import tuiBrain.HandleInputs.Handlers.TopicSelecting.SelectTopicEditJournal
import tuiBrain.InputMode.Normal
import tuiBrain.UI.SearchableList.StatefulList
import tuiBrain.brainDB.{getFromJournalByDate, getFromJournalByTopic}

object TopicView {
  case object SelectTopic2View extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, jrnlEditTopic).handle
      (r._1, r._2.getOrElse(this))
    }
  }

  private def jrnlEditTopic(app : AppData, key : Key) : (AppData, Option[InputHandlers]) = {
    zzzzz(app, key, JournalNormalMode,
      (c: Char) => c.isLetter || c == '_' || c.isDigit || c == '\\')
  }

  private def getSelected(app: AppData, res: InputHandlers): (AppData, Option[InputHandlers]) = {
    app.filteredTopics.getSelected match {
      case None =>
        val tmp = if "^\\d{4}-(0[1-9]|1[0-2]|[0-9])-(0[1-9]|[12][0-9]|3[01])$".r.matches(app.input) then getFromJournalByDate(app.input)
        else List()
        (app.copy(pop = PopMode.NoPop, input = "", load = app.input, journal = if tmp.isEmpty then Array(emptyEntry) else tmp.toArray), Some(res))
      case Some(s) =>
        val tmp = getFromJournalByTopic(s)
        (app.copy(pop = PopMode.NoPop, input_mode = Normal, input = "", load = s, journal = if tmp.isEmpty then Array(emptyEntry) else tmp.toArray), Some(res))
    }
  }

  private def escPress(app: AppData, res: InputHandlers) = {
    val tmp = getFromJournalByDate(UI.Global.nowDate);
    (app.copy(pop = PopMode.NoPop, menu = MenuSelector.Journaling, input_mode = Normal, journal = if tmp.isEmpty then Array(emptyEntry) else tmp.toArray),
      Some(JournalNormalMode))
  }

  private def filterList(app : AppData, str : String) : AppData = app.copy(input = str, filteredTopics = StatefulList(items = app.topics.filter(_.contains(str))))

  def zzzzz(app: AppData, key: Key, result: InputHandlers, allowedChars: Char => Boolean)
  : (AppData, Option[InputHandlers]) = {
    key.keyEvent().code() match {
      case _: KeyCode.Up => app.filteredTopics.previous(); (app, Some(SelectTopic2View))
      case _: KeyCode.Down => app.filteredTopics.next(); (app, Some(SelectTopic2View))
      case _: KeyCode.Esc => escPress(app, result)

      case c: KeyCode.Char if allowedChars(c.c()) => (filterList(app, app.input + c.c), Some(SelectTopic2View))

      case _: KeyCode.Backspace => (filterList(app, if app.input.nonEmpty then app.input.dropRight(1) else ""), Some(SelectTopic2View))

      case _: KeyCode.Enter => getSelected(app, result)

      case _ => (app, None)
    }
  }
}
