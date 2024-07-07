package tuiBrain.UI

import tui.*
import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.UI.Global.{QABoundary, TopicBolder, TopicBrake}
import tuiBrain.{AppData, CurPos, PopMode}

import scala.util.Random

object PopUp {
  def makePopUp(f: Rect, percX : Int, percY : Int): Rect =  centered_rect(percX, percY, f)

  private def centered_rect(percent_x: Int, percent_y: Int, r: Rect): Rect = {
    val popup_layout = Layout(
      direction = Direction.Vertical,
      constraints = Array(
        Constraint.Percentage((100 - percent_y) / 2),
        Constraint.Percentage(percent_y),
        Constraint.Percentage((100 - percent_y) / 2)
      )
    ).split(r)

    Layout(
      direction = Direction.Horizontal,
      constraints = Array(
        Constraint.Percentage((100 - percent_x) / 2),
        Constraint.Percentage(percent_x),
        Constraint.Percentage((100 - percent_x) / 2)
      )
    ).split(popup_layout(1))(1)
  }

  def popYesNo(app : AppData, key : Key) : AppData = { key.keyEvent().code() match {
    case c : KeyCode.Char if (c.c == 'A') && (app.menu.subMenu == "1") =>
      app.menu.subMenu = "4"
      val kr = TableMaker.tableOfString.state.selected.get
      app.copy(pop = PopMode.Editor, tempBuffer = Array((kr, TableMaker.tableOfString.items(kr)(1)), (kr, TableMaker.tableOfString.items(kr)(1))))

    case c : KeyCode.Char if (c.c == 'U') && (app.menu.subMenu == "1") =>
      app.menu.subMenu = "4"
      val kr = TableMaker.tableOfString.state.selected.get
      app.copy(pop = PopMode.Editor, tempBuffer = Array((kr, TableMaker.tableOfString.items(kr)(0)), (kr, TableMaker.tableOfString.items(kr)(0))))

    case c : KeyCode.Char if (c.c == 'Y') && (app.menu.subMenu == "2") =>
      app.tempBuffer(app.tempBuffer.length - 1) = (0, app.input)
      app.copy(pop = PopMode.Topic, tempBuffer = app.tempBuffer :+ (0, ""), input = "")

    case c : KeyCode.Char if (c.c == 'G') && (app.menu.subMenu == "2") =>
      app.menu.subMenu = "3"
      app.tempBuffer(app.tempBuffer.length - 1) = (0, app.input)
      val subs = app.tempBuffer.map(_._2).distinct.map(TopicBrake + _ + TopicBolder + TopicBrake)
      app.copy(pop = PopMode.YesNo, input = "")

    case c : KeyCode.Char if (c.c == 'N') && (app.menu.subMenu == "2") => app.copy(pop = PopMode.Topic, input = "")

    case c : KeyCode.Char if c.c.isDigit && (app.menu.subMenu == "3") => app.copy(input = app.input + c.c)
    
    case c: KeyCode.Char if (c.c.toLower == 'y') || (c.c.toLower == 'n') => if c.c.toLower == 'n' then {
      app.copy(pop = PopMode.Topic, input = "", filteredTopics = SearchableList.StatefulList(items = app.topics))
    } else {
      val gg = app.input
      val tmp = TopicBrake + gg + TopicBolder + TopicBrake
      val line = app.getCurPosText.splitAt(app.curPos.x)
      app.setCurText(line._1 + tmp + line._2, app.getCurPosType)
      app.copy(pop = PopMode.NoPop,
        input = "",
        curPos = CurPos(line._1.length + tmp.length, app.curPos.y),
        topics = app.topics.appended(gg)
      )
    }
  }
  }
}
