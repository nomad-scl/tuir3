package tuiBrain.UI

import tui.*
import tui.widgets.{BlockWidget, ParagraphWidget}
import tuiBrain.UI.Global.*

object QACards {
  def makeCard(txt: Array[String] = Array(""), nOfQA : String = ""): ParagraphWidget = {
    val tt = txt.map(z => Spans(z.split(TopicBrake).map(q => {
      if q.contains(TopicBolder) then Span.styled(q.replace(TopicBolder, ""), Style.apply(addModifier = topicModifier))
      else Span.nostyle(q)
    }))).flatMap(qq => Array(Spans.nostyle(""), Spans.nostyle(""), qq))

    ParagraphWidget(
      text = Text(tt),
      block = Some(BlockWidget(borderType = BlockWidget.BorderType.Double, borderStyle = Style(addModifier = Modifier.REVERSED),
        borders = Borders.ALL, title = Some(Spans.nostyle(nOfQA)), titleAlignment = Alignment.Center)),
      wrap = Some(ParagraphWidget.Wrap(true)),
      alignment = Alignment.Center,
    )
  }

}
