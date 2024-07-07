package tuiBrain.HandleInputs.Handlers

import tui.crossterm.Event.Key
import tuiBrain.{AppData, CurPos, Delete, DoNothing, Insert, MenuSelector, PopMode, Update, emptyEntry}
import tuiBrain.HandleInputs.{InputHandlers, O}
import tui.crossterm.KeyCode
import tuiBrain.HandleInputs.Handlers.QAMenu.SelectTopics4QA
import tuiBrain.InputMode.Normal
import tuiBrain.UI.Global.{TopicBolder, TopicBrake}
import tuiBrain.UI.SearchableList
import tuiBrain.brainDB.{createTopicTable, deleteRows, getFromJournalByDate, insertRows, updateRows}

object msgBoxes {
  case object okMsg2JrnlEdit extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, ok2EdJrnl).handle
      (r._1, r._2.getOrElse(this))
    }
    private def ok2EdJrnl(app: AppData, key: Key): (AppData, Option[InputHandlers]) = ok1(app, key, EditJournal)
  }

  case object okMsg2QA extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, ok2QA).handle
      (r._1, r._2.getOrElse(this))
    }

    private def ok2QA(app: AppData, key: Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
      case c: KeyCode.Char if c.c() == 'O' =>
        app.menu.msgBox = ""
        (app, Some(SelectTopics4QA))
      case _ => (app, Some(okMsg2QA))
    }
  }


  private def ok1(app: AppData, key: Key, res: InputHandlers): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
    case c: KeyCode.Char if c.c() == 'O' =>
      app.menu.msgBox = ""
      (app, Some(res))
    case _ => (app, Some(okMsg2JrnlEdit))
  }


  case object yesNoCreateTopic extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, yn).handle
      (r._1, r._2.getOrElse(this))
    }

    private def yn(app: AppData, key: Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
      case c: KeyCode.Char if c.c() == 'Y' =>
        val gg = app.input
        createTopicTable(gg)
        val tmp = TopicBrake + gg + TopicBolder + TopicBrake
        val line = app.getCurPosText.splitAt(app.curPos.x)
        app.setCurText(line._1 + tmp + line._2, app.getCurPosType)
        app.addTopic(gg)

        (app.copy(pop = PopMode.NoPop,
          input = "",
          curPos = CurPos(line._1.length + tmp.length, app.curPos.y),
          topics = app.topics.appended(gg)
        ), Some(EditJournal))

      case c: KeyCode.Char if c.c() == 'N' =>
        (app.copy(pop = PopMode.NoPop, input = "", filteredTopics = SearchableList.StatefulList(items = app.topics)), Some(EditJournal))

      case _ => (app, Some(yesNoCreateTopic))
    }
  }

  case object saveChangesEsc extends InputHandlers {
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, yn).handle
      (r._1, r._2.getOrElse(this))
    }

    private def yn(app: AppData, key: Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
      case c: KeyCode.Char if c.c() == 'Y' =>

        val ups = app.journal.groupBy(_._1.getClass)
        ups.keys.foreach {
          case s if s == classOf[Insert.type] => insertRows(ups(s))
          case s if s == classOf[Update] => updateRows(ups(s))
          case s if s == classOf[Delete] => deleteRows(ups(classOf[Delete]))
          case _ => ()
        }

        val kk = if "^\\d{4}-(0[1-9]|1[0-2]|[0-9])-(0[1-9]|[12][0-9]|3[01])$".r.matches(app.load) then getFromJournalByDate(app.load)
            else List()
        (app.copy(pop = PopMode.NoPop,
          input = "",
          curPos = CurPos(0,0),
          input_mode = Normal,
          journal = if kk.nonEmpty then kk.toArray else Array(emptyEntry),
          menu = MenuSelector.Journaling
        ), Some(JournalNormalMode))

      case c: KeyCode.Char if c.c() == 'N' =>
        val kk = if "^\\d{4}-(0[1-9]|1[0-2]|[0-9])-(0[1-9]|[12][0-9]|3[01])$".r.matches(app.load) then getFromJournalByDate(app.load)
        else List(emptyEntry)

        (app.copy(pop = PopMode.NoPop,
          input = "",
          curPos = CurPos(0,0),
          input_mode = Normal,
          journal = if kk.nonEmpty then kk.toArray else Array(emptyEntry),
          menu = MenuSelector.Journaling
        ), Some(JournalNormalMode))

      case _ => (app, Some(saveChangesEsc))
    }
  }
}


