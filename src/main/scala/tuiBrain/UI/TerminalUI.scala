package tuiBrain.UI

import tui.*
import tui.widgets.{BlockWidget, ClearWidget, ListWidget, ParagraphWidget}
import tuiBrain.*
import Global._

object TerminalUI {

  private val tpCnt = (TopicBrake.length * 2) + TopicBolder.length
  private val qaCnt = QABoundary.length

  private def mainLayout(f : Frame, app : AppData) : Array[Rect] = {
    val chunks = Layout(
      direction = Direction.Vertical,
      margin = Margin(2),
      constraints = Array(Constraint.Length(1), Constraint.Min(5))
    ).split(f.size)

    val (msg, style) = app.input_mode match {
      case InputMode.Normal =>
        (
          Text.from(
            Span.nostyle("Press "),
            Span.styled("q", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to exit, "),
            Span.styled("e", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to start editing.")
          ),
          Style.DEFAULT.addModifier(Modifier.RAPID_BLINK)
        )
      case InputMode.Editing =>
        (
          Text.from(
            Span.nostyle("Press "),
            Span.styled("Esc", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to stop editing, "),
            Span.styled("Enter", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to record the message")
          ),
          Style.DEFAULT
        )
    }

    val text = msg.overwrittenStyle(style)

    val help_message = ParagraphWidget(text = text)
    f.renderWidget(help_message, chunks(0))
    val chk = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(20), Constraint.Percentage(80))
    ).split(chunks(1))

    val txtMenu = Array("   ", "   ", "   ", "[J]ournaling", "    ", "    ", "[K]nowledge Test", "    ", "    ", "[T]opc View").map{m => app.menu match{
      case MenuSelector.Journaling => if m(1) == 'J' then Spans.styled(m, Style(addModifier = Modifier.REVERSED)) else Spans.nostyle(m)
      case MenuSelector.QA => if m(1) == 'K' then Spans.styled(m, Style(addModifier = Modifier.REVERSED)) else Spans.nostyle(m)
    }}

    val input = ParagraphWidget(
      text = Text(txtMenu),
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("")))),
      alignment = Alignment.Center
    )
    f.renderWidget(input, chk(0))
    chk
  }

  private def journal(f: Frame, app : AppData, chk : Array[Rect]) : Unit = {
    app.input_mode match {
      case InputMode.Normal => ()
      case InputMode.Editing =>
        val delCnt = app.journal.slice(0, app.curPos.y).count(_._1 == Delete)
        val cnt = app.getCurPosText.splitAt(app.curPos.x)._1
          .replace(TopicBolder,"").replace(TopicBrake,"").replace(QABoundary,"?").replace(eventBoundary, "").replace(eventFinder,"").length
        val c3 = if app.getCurPosText.isEmpty || (app.getCurPosText(0) != '[') then 4 else 1
        f.setCursor(
          x = chk(1).x + cnt + c3 + (3 * app.getCurPosLevel), //(app.curPos.x + 3) + cnt,
          y = chk(1).y + app.curPos.y + 1 - delCnt
        )
    }

    val items: Array[ListWidget.Item] =
      app.journal.filter(_._1.getClass != classOf[Delete]).map { jrnl =>
        val tb = "   " * jrnl._2.msglevel
        val g = if jrnl._2.message.isEmpty || (jrnl._2.message(0) != '[') then topicer(s"$tb * ${jrnl._2.message}") else topicer(s"$tb${jrnl._2.message}")
        ListWidget.Item(content = Text.from(g: _*)) }

    val messages =
      ListWidget(
        items = items,
        block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle(currentDate)))),
        style = app.input_mode match {
          case InputMode.Normal => Style.DEFAULT
          case InputMode.Editing => Style.DEFAULT.fg(Color.Yellow)
        }
      )
    f.renderWidget(messages, chk(1))
  }

  private def popEditor(app : AppData, f: Frame, popTitle : String = "", percX : Int = 25, percY : Int = 25) : Unit = {
    val popArea = PopUp.makePopUp(f.size, percX, percY)
    val p = Layout(
      direction = Direction.Vertical,
      margin = Margin(2),
      constraints = Array(Constraint.Percentage(100))
    ).split(popArea)

    val txt = Text.from(topicer(app.tempBuffer(1)._2):_*)
    val input = ParagraphWidget(
      text = txt,
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle(popTitle)))),
      wrap = Some(ParagraphWidget.Wrap(true)),
    )
    f.renderWidget(ClearWidget, popArea)
    f.renderWidget(input, p(0))
  }

  private def popTopic(app : AppData, f: Frame, popMessage : String = "", popTitle : String = "", percX : Int = 25, percY : Int = 80) : Unit = app.pop match {
    case PopMode.NoPop => ()
    case PopMode.Topic =>
      val popArea = PopUp.makePopUp(f.size, percX, 80)
      val p = Layout(
        direction = Direction.Vertical,
        margin = Margin(2),
        constraints = Array(Constraint.Percentage(15), Constraint.Percentage(85))
      ).split(popArea)
      val par = SearchableList.makeTextBlock(app.input)
      val lst = SearchableList.makeList("Topics", app.filteredTopics, x => x)

      f.renderWidget(ClearWidget, popArea)
      f.renderWidget(par, p(0))
      f.renderStatefulWidget(lst, p(1))(app.filteredTopics.state)
      f.setCursor(x = p(0).x + Grapheme(app.input).width + 1, y = p(0).y + 1)

    case PopMode.YesNo => val popArea = PopUp.makePopUp(f.size, percX, percY)
      val p = Layout(
        direction = Direction.Vertical,
        margin = Margin(2),
        constraints = Array(Constraint.Percentage(100))
      ).split(popArea)

      val par = SearchableList.makeTextBlock(popMessage, title = popTitle)

      f.renderWidget(ClearWidget, popArea)
      f.renderWidget(par, p(0))

    case PopMode.Card => ()
    case PopMode.Editor => ()

    case PopMode.Event => val popMsg : String = PopMode.Event.action match {
      case 0 => "Is this a weekly event or by calendar?\n\n[C]alendar         [W]eekly"
      case 1 => "Enter the days of the week for the event to repeat?\n\n[S]unday\n[M]onday\n[T]uesday\n[W]ednesdat\nT[H]ursday\n[F]riday\nS[A]turday\n\nSelected Days : " + app.input.drop(1)
      case 2 => "Enter the year of the event or * if the event must repeat each year:\n\nDate : " + app.input.drop(1) + "\\__\\__"
      case 3 => "Enter the month of the event or * if the event must repeat each month:\n\nDate : " + app.input.drop(1) + "\\__"
      case 4 => "Enter the day of the event or * if the event must repeat each day:\n\nDate : " + app.input.drop(1)
      case _ => ""
    }
      val popArea = PopUp.makePopUp(f.size, percX, percY)
      val p = Layout(
        direction = Direction.Vertical,
        margin = Margin(2),
        constraints = Array(Constraint.Percentage(100))
      ).split(popArea)

      val par = SearchableList.makeTextBlock(popMsg, title = "Create an Event")
      f.renderWidget(ClearWidget, popArea)
      f.renderWidget(par, p(0))

  }

  private def makeCard(app : AppData, f : Frame) : Unit = {
    val popArea = PopUp.makePopUp(f.size, 50, 75)
    val p = Layout(
      direction = Direction.Vertical,
      margin = Margin(2),
      constraints = Array(Constraint.Percentage(100))
    ).split(popArea)
    val msg =
      app.pop match{
      case PopMode.Card if !PopMode.Card.showAnswer =>
        val tmp = ("\n\n" + app.getCurPosText).split(QABoundary)(0)
        Array(tmp, "\n\n [S]how answer")
      case PopMode.Card if PopMode.Card.showAnswer =>
        val tmp = ("\n\n" + app.getCurPosText).split(QABoundary)
        tmp :+ "\n\n [N]ext Card"
      case _ =>
        val tmp = ("\n\n" + app.getCurPosText).split(QABoundary)(0)
        Array(tmp, "\n\n [S]how answer")
    }
    val par = QACards.makeCard(msg, app.input)

    f.renderWidget(ClearWidget, popArea)
    f.renderWidget(par, p(0))
  }

  def ui2(f: Frame, app: AppData): Unit = {

    val chks = mainLayout(f, app)
    app.menu match{
      case MenuSelector.Journaling => journal(f, app, chks)
                                      popTopic(app, f, app.input + "\n\n\n        [Y]es                  [N]o\n\n", "Confirm new topic")

      case MenuSelector.QA if app.menu.subMenu == "0" => val par = SearchableList.makeTextBlock("Choose one of the following:\n\n\n        [V]iew stored QAs\n\n        [B]egin a QA run\n\n", title = "")
                              val popArea = PopUp.makePopUp(chks(1), 30, 20)
                              f.renderWidget(ClearWidget, popArea)
                              f.renderWidget(par, popArea)

      case MenuSelector.QA if (app.menu.subMenu == "1") && (app.pop == PopMode.Topic) => popTopic(app, f)
      case MenuSelector.QA if (app.menu.subMenu == "1") && (app.pop == PopMode.YesNo) =>
        popTopic(app, f, popMessage = "Do you want to edit the Question or the Answer?\n\n\n         Q[U]estion        [A]nswer", percX = 30, percY = 25)

      case MenuSelector.QA if (app.menu.subMenu == "1") && (app.pop == PopMode.NoPop) =>
        val r = TableMaker.makeTable(TableMaker.tableOfString, Array("Questions", "Answers"), chks(1))
        f.renderStatefulWidget(r, chks(1))(TableMaker.tableOfString.state)

      case MenuSelector.QA if app.menu.subMenu == "2" => popTopic(app, f,
        s"Add this topic : ${app.input} to question list?\n\n\n        [Y]es and add another topic\n\n        [N]o\n\n        [G]o to the questions", "Confirm QA topic",
        percY = 40)
      case MenuSelector.QA if (app.menu.subMenu == "3") && (app.pop != PopMode.Card) => popTopic(app, f, s"How many questions do you want?\n\n[Enter a number or A for all of them]", "Number of questions")
      case MenuSelector.QA if (app.menu.subMenu == "3") && (app.pop == PopMode.Card) => makeCard(app, f)

      case MenuSelector.QA if (app.menu.subMenu == "4") && (app.pop == PopMode.Editor) => popEditor(app, f)
      case MenuSelector.QA if (app.menu.subMenu == "4") && (app.pop == PopMode.YesNo) => popTopic(app, f, "Are you sure you want to delete this question?\n\n      [Y]es              [N]o", percY = 25)

      case _ => ()
    }

    if (app.menu.msgBox != "") {
      val popArea = PopUp.makePopUp(chks(1), 35, 35)
      val p = Layout(
        direction = Direction.Vertical,
        margin = Margin(2),
        constraints = Array(Constraint.Percentage(100))
      ).split(popArea)

      val par = SearchableList.makeTextBlock(app.menu.msgBox + "\n\n[O]k", "", align = Alignment.Center)

      f.renderWidget(ClearWidget, popArea)
      f.renderWidget(par, p(0))

    }
  }
}
