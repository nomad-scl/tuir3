package tuiBrain.HandleInputs.Handlers

import tui.crossterm.Event.Key
import tuiBrain.AppData
import tuiBrain.HandleInputs.O
import tuiBrain.HandleInputs.InputHandlers
import tuiBrain.HandleInputs._
import tuiBrain.HandleInputs.Bocks.NavigationAndCtrl._
import tuiBrain.HandleInputs.Bocks.TextEditing._

case object JournalNormalMode extends InputHandlers {
  override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) =
    val r = O(app, key, journalNormalMenu).handle
    (r._1, r._2.getOrElse(this))
}

case object EditJournal extends InputHandlers {
  override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = {
    val r = O(app, key, journalEditCtrl).orHandle(journalEditCursorNavigation).orHandle(journalEditTextModify).handle
    (r._1, r._2.getOrElse(this))
  }
}

case object QuitProgram extends InputHandlers :
  override def handleInput(app: AppData, key: Key): (AppData, InputHandlers) = (app, QuitProgram)


