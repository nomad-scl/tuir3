package tuiBrain.UI

import tui.*
import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tui.widgets.{BlockWidget, ListWidget, ParagraphWidget, TableWidget}
//import tuiBrain.Handelers.getSelected
import tuiBrain.UI.Global.{TopicBolder, TopicBrake}
import tuiBrain.{AppData, CurPos, DeleteMe, ExitStatus, InputMode, MenuSelector, PopMode}

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

//  def handleInput(app : AppData, key : Key, listofItems : StatefulList[String], getSelectedItem : AppData => AppData): AppData = {
  def handleInput(app : AppData, key : Key, getSelectedItem : AppData => AppData): AppData = {
    key.keyEvent().code() match {
//      case c : KeyCode.Char if app.menu.subMenu == "2" =>
////        SearchableList.listOfString = SearchableList.StatefulList(items = app.topics.filter(_.contains(app.input + c.c)))
////        app.copy(input = app.input + c.c)
//        app.copy(input = app.input + c.c, filteredTopics = StatefulList(items = app.topics.filter(_.contains(app.input + c.c))))
//
//    case _: KeyCode.Backspace if app.menu.subMenu == "2" =>
////      val k = if app.input.nonEmpty then app.copy(input = app.input.dropRight(1)) 
////      else app.copy(input = "")
////      SearchableList.listOfString = SearchableList.StatefulList(items = app.topics.filter(_.contains(k.input)))
////      k
//      val k = if app.input.nonEmpty then app.input.dropRight(1)  else ""
//      app.copy(input = k, filteredTopics = StatefulList(items = app.topics.filter(_.contains(k))))

//      case _: KeyCode.Up => app.filteredTopics.previous(); app
//      case _: KeyCode.Down => app.filteredTopics.next(); app

//      case _: KeyCode.Enter if app.input_mode == InputMode.Editing => getSelectedItem(app)
      case _: KeyCode.Enter if app.menu.subMenu == "2" => getSelectedItem(app)
      case _: KeyCode.Enter if app.menu.subMenu == "1" =>
        val kk = getSelectedItem(app)
        DeleteMe.tempFilteredQAs =
          DeleteMe.tempQAs.filter(z => z._1.contains(TopicBrake + kk.input + TopicBolder + TopicBrake) || z._2.contains(TopicBrake + kk.input + TopicBolder + TopicBrake));
        TableMaker.tableOfString = TableMaker.TableData(state = TableWidget.State(), items = DeleteMe.tempFilteredQAs.map(Array(_, _)))
        kk

//      case _: KeyCode.Esc => app.copy(pop = PopMode.NoPop, input = "")

    }
  }
}
