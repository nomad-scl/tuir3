package tuiBrain.HandleInputs.Bocks

import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.HandleInputs.Bocks.helpers.{checkDate, checkWeek, wk2Ltr}
import tuiBrain.{AppData, MenuSelector, PopMode, messageTypes}
import tuiBrain.HandleInputs.Handlers.EditJournal
import tuiBrain.HandleInputs.Handlers.msgBoxes.okMsg2JrnlEdit
import tuiBrain.HandleInputs.{InputHandlers, O}
import tuiBrain.UI.Global.{eventBoundary, eventFinder}

object popUps {
  case object editJrnlEventChs extends InputHandlers {
    private def chooseEvent(app: AppData, key: Key) = key.keyEvent().code() match {
      case c: KeyCode.Char if c.c.toLower == 'w' =>
        PopMode.Event.action = 1
        (app.copy(input = "w"), Some(editJrnlEventWeek))
      case c: KeyCode.Char if c.c.toLower == 'c' =>
        PopMode.Event.action = 2
        (app.copy(input = "c"), Some(editJrnlEventYear))
      case _: KeyCode.Esc => (app.copy(pop = PopMode.NoPop, input = ""), Some(EditJournal))
      case _ => (app, Some(editJrnlEventChs))
    }
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, chooseEvent).handle
      (r._1, r._2.getOrElse(this))
    }
  }

  private case object editJrnlEventWeek extends InputHandlers{
    private def weekDays(app: AppData, key: Key) = key.keyEvent().code() match {
      case c: KeyCode.Char if checkWeek(c.c.toUpper) != "" =>
        val dd = checkWeek(c.c.toUpper)
        if app.input.contains(dd) then (app.copy(input = app.input.replace(" : " + dd, "")), Some(editJrnlEventWeek))
        else (app.copy(input = app.input + " : " + dd), Some(editJrnlEventWeek))

      case _: KeyCode.Enter =>
        val k = app.input.splitAt(1)._2.split(" : ").drop(1).map(wk2Ltr).mkString("")
        val cr = app.getCurPosText.splitAt(app.curPos.x)
        app.setCurText(cr._1 + eventBoundary + app.input(0) + k + eventFinder + eventBoundary + cr._2, messageTypes.event)
        PopMode.Event.action = 0
        (app.copy(input = "", pop = PopMode.NoPop), Some(EditJournal))

      case _: KeyCode.Esc => (app.copy(pop = PopMode.NoPop, input = ""), Some(EditJournal))
      case _ => (app, Some(editJrnlEventWeek))
    }
    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, weekDays).handle
      (r._1, r._2.getOrElse(this))
    }
  }

  private case object editJrnlEventYear extends InputHandlers {
    private def calendarIn(app: AppData, key: Key) = key.keyEvent().code() match {
      case c: KeyCode.Char if c.c == '*' =>
        val tt = if !app.input.contains("\\") then "c*\\" else app.input.substring(0, app.input.lastIndexOf("\\")) + "\\*\\"
        val num = checkDate(tt.drop(1))
        PopMode.Event.action +=1
        if num.length == 3 then {
          if num.contains(None) then {
            app.menu.msgBox = "Entered date is incorrect"
            PopMode.Event.action = 0
            (app.copy(input = "", pop = PopMode.NoPop), Some(okMsg2JrnlEdit))
          } else{
            val k = "c" + num.map(_.get).mkString("\\")
            val cr = app.getCurPosText.splitAt(app.curPos.x)
            app.setCurText(cr._1 + eventBoundary + k + eventFinder + eventBoundary + cr._2, messageTypes.event)
            (app.copy(input = "", pop = PopMode.NoPop), Some(EditJournal))
          }
        }
        else (app.copy(input = tt), Some(editJrnlEventYear))

      case c: KeyCode.Char if c.c.isDigit => (app.copy(input = app.input + c.c), Some(editJrnlEventYear))

      case _: KeyCode.Backspace => if (app.input.length > 1) && (app.input.last != '\\') then (app.copy(input = app.input.dropRight(1)), Some(editJrnlEventYear))
        else (app, Some(editJrnlEventYear))

      case _: KeyCode.Enter if app.input(0) == 'c' =>
        val num = checkDate(app.input.drop(1))
        PopMode.Event.action +=1
        if num.length != 3 then (app.copy(input = app.input + "\\"), Some(editJrnlEventYear))
        else if num.contains(None) then {
          app.menu.msgBox = "Entered date is incorrect"
          PopMode.Event.action = 0
          (app.copy(input = "", pop = PopMode.NoPop), Some(EditJournal))
        } else {
          val k = "c" + num.map(_.get).mkString("\\")
          val cr = app.getCurPosText.splitAt(app.curPos.x)
          app.setCurText(cr._1 + eventBoundary + k + eventFinder + eventBoundary + cr._2, messageTypes.event)
          PopMode.Event.action = 0
          (app.copy(input = "", pop = PopMode.NoPop), Some(EditJournal))
        }

      case _: KeyCode.Esc => (app.copy(pop = PopMode.NoPop, input = ""), Some(EditJournal))
      case _ => (app, Some(editJrnlEventYear))
    }

    override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
      val r = O(app, key, calendarIn).handle
      (r._1, r._2.getOrElse(this))
    }
  }

}
