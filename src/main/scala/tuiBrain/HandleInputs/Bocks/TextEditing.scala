package tuiBrain.HandleInputs.Bocks

import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.HandleInputs.Handlers.EditJournal
import tuiBrain.HandleInputs.Bocks.helpers.*
import tuiBrain.HandleInputs.Handlers.msgBoxes.{okMsg2JrnlEdit, saveChangesEsc}
import tuiBrain.{AppData, InputMode, PopMode}
import tuiBrain.HandleInputs.InputHandlers

object TextEditing {
  def journalEditTextModify(app : AppData, key : Key): (AppData, Option[InputHandlers]) = key.keyEvent().code() match {
    case c: KeyCode.Char => (addChar2MessageAtPos(app, c), Some(EditJournal))
    case _: KeyCode.Enter => (insertNewLine(app), Some(EditJournal))
    case _: KeyCode.Tab => (increaseLevel(app), Some(EditJournal))
    case _: KeyCode.BackTab => (decreaseLevel(app), Some(EditJournal))
    case _: KeyCode.Backspace =>
      val k = removeChar(app)
      if k.menu.msgBox != "" then (app, Some(okMsg2JrnlEdit))
      else (k, Some(EditJournal))
    case _: KeyCode.Esc =>
      (app.copy(input_mode = InputMode.Normal, pop = PopMode.YesNo, input = "Do you want to save your changes?"), Some(saveChangesEsc)) //save dialog messagebox
    case _ => (app, None)
  }
}
