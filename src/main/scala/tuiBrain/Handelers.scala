package tuiBrain
import tui.*
import tui.crossterm.CrosstermJni
import tuiBrain.HandleInputs.Handlers.QuitProgram
import tuiBrain.HandleInputs.InputHandlers
import tuiBrain.UI.TerminalUI
import tuiBrain.UI.Global.*
import scala.annotation.tailrec

object Handelers {

  @tailrec
  def run_app2(terminal: Terminal, app: AppData, jni: CrosstermJni, handler : InputHandlers): Unit = {

    terminal.draw(f => UI.TerminalUI.ui2(f, app))

    val res : (AppData, InputHandlers) = jni.read() match {
      case key: tui.crossterm.Event.Key =>
        handler.handleInput(app, key)
      case _ => (app, handler)
    }
    if res._2 != QuitProgram then run_app2(terminal, res._1, jni, res._2)
  }

}
