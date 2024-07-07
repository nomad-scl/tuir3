package tuiBrain.UI

import tui.*
import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tui.widgets.{BlockWidget, ListWidget, ParagraphWidget, TableWidget}
import tuiBrain.UI.Global.{TopicBolder, TopicBrake}
import tuiBrain.{AppData, CurPos, ExitStatus, InputMode, MenuSelector, PopMode}

object SearchableList {
  case class StatefulList[T](
                              state: ListWidget.State = ListWidget.State(),
                              items: Array[T]
                            ) {
    def next(): Unit = {
      val i = state.selected match {
        case Some(i) => if (i >= items.length - 1) 0 else i + 1
        case None => 0
      }
      state.select(Some(i))
    }

    def previous(): Unit = {
      val i = state.selected match {
        case Some(i) =>
          if (i == 0) {
            items.length - 1
          } else {
            i - 1
          }
        case None => 0
      }
      state.select(Some(i))
    }

    def unselect(): Unit =
      state.select(None)

    def getSelected: Option[T] = {
      state.selected match {
        case Some(i) => Some(items(i))
        case None => None
      }
    }
  }

  var listOfString : StatefulList[String] = StatefulList(items = Array(""))

  def makeList[T](title : String, itemer : StatefulList[T], fn : T => String): ListWidget = {
    val items0 = itemer.items
      .map { str =>
        val lines = Array.newBuilder[Spans]
        lines += Spans.nostyle(fn(str))
        ListWidget.Item(Text(lines.result()), Style(fg = Some(Color.Black), bg = Some(Color.White)))
      }

    val items = ListWidget(
      items = items0,
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle(title)))),
      highlightStyle = Style(bg = Some(Color.LightGreen), addModifier = Modifier.BOLD),
      highlightSymbol = Some(">> ")
    )

    items
  }
  def makeTextBlock(txt : String = "", title : String = "Search Topic", align : Alignment = Alignment.Left): ParagraphWidget = {
    val input = ParagraphWidget(
      text = Text.nostyle(txt),
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle(title)))),
      wrap = Some(ParagraphWidget.Wrap(true)),
      alignment = align
    )
    input
  }

  def handleInput(app : AppData, key : Key, getSelectedItem : AppData => AppData): AppData = {
    key.keyEvent().code() match {
      case _: KeyCode.Enter if app.menu.subMenu == "2" => getSelectedItem(app)
      case _: KeyCode.Enter if app.menu.subMenu == "1" =>
        val kk = getSelectedItem(app)
        kk
    }
  }
}
