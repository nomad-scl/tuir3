package tuiBrain.UI

import tui.{Color, Modifier, Span, Spans, Style}

import scala.annotation.tailrec
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Global {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-dd")
  val nowDate: String = LocalDate.now().format(formatter)

  val TopicBrake = "<@%>"
  val topicModifier: Modifier = Modifier(Modifier.UNDERLINED.bits + Modifier.BOLD.bits + Modifier.REVERSED.bits)
  val TopicBolder = "!!@@!!"

  val qaModifier: Modifier = Modifier(Modifier.UNDERLINED.bits + Modifier.ITALIC.bits + Modifier.REVERSED.bits)
  val QABoundary = "<<<###>>>"

  val eventBoundary = "<<<!!!>>>"
  val eventFinder = "<<<@@@>>>"
  val eventModifier: Modifier = Modifier(Modifier.UNDERLINED.bits + Modifier.BOLD.bits + Modifier.ITALIC.bits)

  def topicer(c : String) : Array[Span] = c.split(TopicBrake).flatMap(q => {
    if q.contains(TopicBolder) then Array(Span.styled(q.replace(TopicBolder, ""), Style(addModifier = topicModifier)))
    else if c == " " then Array(Span.styled(q, Style(fg = Some(Color.White), bg = Some(Color.Black), addModifier = Modifier.REVERSED)))
    else if q.contains(QABoundary) then {
      val r1 = q.splitAt(q.indexOf(QABoundary))
      Array(Span.nostyle(r1._1), Span.styled("?", Style(addModifier = qaModifier)), Span.nostyle(r1._2.replace(QABoundary,"")))
    }
    else if q.contains(eventBoundary) then q.split(eventBoundary).map(q2 => {
      if q2.contains(eventFinder) then Span.styled(q2.replace(eventFinder, ""), Style(addModifier = eventModifier))
      else Span.nostyle(q2)
    })
    else Array(Span.nostyle(q))
  })

  def lener(c : String) : Int = c.replace(TopicBolder, "").replace(TopicBrake, "").replace(QABoundary, "?").length

  @tailrec
  def topicerLines(k : Array[Span], lim : Int, res : Array[Spans]) : Array[Spans] = {
    var ln = 0
    val na = k.takeWhile(q => {
      ln += q.width
      ln <= lim
    })

    if na.length == k.length then  res :+ Spans(na)
    else {
      val str = k.drop(na.length)
      val added = str(0).content.splitAt(lim - (ln - str(0).width) - 1)
      val r1 = Span.styled(added._1, str(0).style)
      val r2 = Span.styled(added._2, str(0).style)
      topicerLines(Array(r1,r2).concat(str.drop(1)), lim, if na.length > 0 then res :+ Spans(na) else res)
    }
  }
}
