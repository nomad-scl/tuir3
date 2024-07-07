package tuiBrain.UI

import tui.{Borders, Color, Constraint, Modifier, Rect, Span, Spans, Style, Text}
import tui.widgets.{BlockWidget, TableWidget}
import tuiBrain.UI.Global.{TopicBolder, TopicBrake, topicModifier, topicer, topicerLines}

object TableMaker {
  val items: Array[Array[String]] = Array(
    Array("Row11", "Row12", "Row13"),
    Array("Row21", "Row22", "Row23"),
    Array("Row31", "Row32", "Row33"),
    Array("Row41", "Row42", "Row43"),
    Array("Row51", "Row52", "Row53"),
    Array("Row61", "Row62\nTest", "Row63"),
    Array("Row71", "Row72", "Row73"),
    Array("Row81", "Row82", "Row83"),
    Array("Row91", "Row92", "Row93"),
    Array("Row101", "Row102", "Row103"),
    Array("Row111", "Row112", "Row113"),
    Array("Row121", "Row122", "Row123"),
    Array("Row131", "Row132", "Row133"),
    Array("Row141", "Row142", "Row143"),
    Array("Row151", "Row152", "Row153"),
    Array("Row161", "Row162", "Row163"),
    Array("Row171", "Row172", "Row173"),
    Array("Row181", "Row182", "Row183"),
    Array("Row191", "Row192", "Row193")
  )

  case class TableData(
                  state: TableWidget.State,
                  items: Array[Array[String]]
                ) {
    def next(): Unit = {
      val i = state.selected match {
        case Some(i) =>
          if (i >= items.length - 1) {
            0
          } else {
            i + 1
          }

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
  }

  var tableOfString : TableData = TableData(state = TableWidget.State(), items = items)

  def makeTable(data: TableData, hdr : Array[String], rc : Rect): TableWidget = {

    val selected_style = Style(addModifier = Modifier.REVERSED)
    val normal_style = Style(bg = Some(Color.Gray), addModifier = Modifier.BOLD)
    val header_cells = hdr.flatMap(Array(_, " ")).dropRight(1).map(h => TableWidget.Cell(Text.nostyle(h), style = Style(fg = Some(Color.Red))))
    val header = TableWidget.Row(cells = header_cells, style = normal_style, bottomMargin = 1)

    val cons = Array((rc.width * 0.35).toInt, 2, (rc.width * 0.65).toInt)
    val rows = data.items.map { item =>
      val r1 = item.flatMap(Array(_, " ")).dropRight(1)
      val r2 = r1.zipWithIndex.map( (z, i) => topicerLines(topicer(z), cons(i), Array.empty))
      val height = r2.map(_.length).maxOption.getOrElse(0) + 1

      val r3 = r2.zipWithIndex.map((z, i) => {
        if i % 2 == 0 then z
        else Array.fill(height)(z(0))
      })
      val cells = r3.map(c => {
        TableWidget.Cell(Text(c))
      })
      TableWidget.Row(cells, height = height, bottomMargin = 1)
    }
    val t = TableWidget(
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List of QAs")))),
      widths = Array(Constraint.Percentage(35), Constraint.Length(1), Constraint.Percentage(65)),
      highlightStyle = selected_style,
      highlightSymbol = Some(">> "),
      header = Some(header),
      rows = rows
    )
    t
  }
}
