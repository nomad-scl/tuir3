package tuiBrain.UI

import tui.*
import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tuiBrain.UI.Global.{QABoundary, TopicBolder, TopicBrake, eventBoundary, eventFinder}
import tuiBrain.{AppData, CurPos, DeleteMe, PopMode, messageTypes}

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
      DeleteMe.tempFilteredQAs = DeleteMe.tempQAs.filter((m1, m2) => subs.exists((m1+m2).contains(_)))
      app.copy(pop = PopMode.YesNo, input = "")

    case c : KeyCode.Char if (c.c == 'N') && (app.menu.subMenu == "2") => app.copy(pop = PopMode.Topic, input = "")

    case c : KeyCode.Char if (c.c == 'A') && (app.menu.subMenu == "3") =>
      val mm = DeleteMe.tempFilteredQAs.map((m1,m2) => (0, m1 + QABoundary + m2))
      if mm.nonEmpty then
        app.copy(tempBuffer = mm, curPos = CurPos(0,0), pop = PopMode.Card, input = "Card { 1 of " + DeleteMe.tempFilteredQAs.length.toString + " }")
      else {
        app.menu.subMenu = "0"
        app.menu.msgBox = "There are no questions with these selected topics"
        app.copy(tempBuffer = Array((0, "")), input = "", pop = PopMode.YesNo)
      }

    case c : KeyCode.Char if c.c.isDigit && (app.menu.subMenu == "3") => app.copy(input = app.input + c.c)

    case _: KeyCode.Enter if app.menu.subMenu == "3" =>
      val mm = DeleteMe.tempFilteredQAs.map((m1,m2) => (0, m1 + QABoundary + m2)).take(1)
      val n2 = if app.input.toInt != 0 then app.input.toInt else 1
      val n = if n2 > DeleteMe.tempFilteredQAs.length then DeleteMe.tempFilteredQAs.length else n2
      if mm.nonEmpty then
        app.copy(tempBuffer = (1 to n).map(_ => mm(Random.nextInt(mm.length))).toArray, curPos = CurPos(0,0), pop = PopMode.Card, input = "Card { 1 of " + n.toString + " }")
      else {
        app.menu.subMenu = "0"
        app.menu.msgBox = "There are no questions with these selected topics"
        app.copy(tempBuffer = Array((0, "")), input = "", pop = PopMode.YesNo)
      }

    case c: KeyCode.Char if (c.c.toLower == 'y') || (c.c.toLower == 'n') => if c.c.toLower == 'n' then {
//      SearchableList.listOfString = SearchableList.StatefulList(items = app.topics)
      app.copy(pop = PopMode.Topic, input = "", filteredTopics = SearchableList.StatefulList(items = app.topics))
    } else {
//      val gg = app.input.splitAt(app.input.indexOf("\"")+1)._2.dropRight(2)
      val gg = app.input
      //TODO: create table if not exist for topic gg
//      tempList = tempList :+ gg
      val tmp = TopicBrake + gg + TopicBolder + TopicBrake
      val line = app.getCurPosText.splitAt(app.curPos.x)
//      app.messages(app.curPos.y) = (app.messages(app.curPos.y)._1, line._1 + tmp + line._2)
      app.setCurText(line._1 + tmp + line._2, app.getCurPosType)
      app.copy(pop = PopMode.NoPop,
        input = "",
        curPos = CurPos(line._1.length + tmp.length, app.curPos.y),
        topics = app.topics.appended(gg)
      )
    }
  }
  }

//  def popEvent(app : AppData, key : Key) : AppData = {
//    key.keyEvent().code() match {
//      case _: KeyCode.Enter if ((PopMode.Event.action == 2) || (PopMode.Event.action == 3)) && (app.input(0) == 'c') =>
//        PopMode.Event.action += 1
//        app.copy(input = app.input + "\\")

//      case _: KeyCode.Enter if (PopMode.Event.action == 4) && (app.input(0) == 'c') =>
//        val num = checkDate(app.input.drop(1))
//
//        if num.contains(None) then {
//          PopMode.Event.action = 0
//          app.menu.msgBox = "Entered date is incorrect"
//          app.copy(input = "", pop = PopMode.NoPop)
//        } else {
//          val k = "c" + num.map(_.get).mkString("\\")
//          val cr = app.getCurPosText.splitAt(app.curPos.x)
//          app.setCurText(cr._1 + eventBoundary + k + eventFinder + eventBoundary + cr._2, messageTypes.event)
////          app.messages(app.curPos.y) = (app.messages(app.curPos.y)._1, cr._1 + eventBoundary + k + eventFinder + eventBoundary + cr._2)
//          PopMode.Event.action = 0
//          app.copy(input = "", pop = PopMode.NoPop)
//        }

//      case c: KeyCode.Char if (c.c == '*') && ((PopMode.Event.action == 2) || (PopMode.Event.action == 3) || (PopMode.Event.action == 4)) =>
//        val tt = if app.input.length == 1 then app.input + "*\\" else if !app.input.contains("\\") then "c*\\" else
//          app.input.substring(0, app.input.lastIndexOf("\\")) + "\\*\\"
//        PopMode.Event.action += 1
//        if PopMode.Event.action == 5 then {
//          val num = checkDate(tt.drop(1))
//          if num.contains(None) then {
//            PopMode.Event.action = 0
//            app.menu.msgBox = "Entered date is incorrect"
//            app.copy(input = "", pop = PopMode.NoPop)
//          } else{
//            val k = "c" + num.map(_.get).mkString("\\")
//            val cr = app.getCurPosText.splitAt(app.curPos.x)
////            app.messages(app.curPos.y) = (app.messages(app.curPos.y)._1, cr._1 + eventBoundary + k + eventFinder + eventBoundary + cr._2)
//            app.setCurText(cr._1 + eventBoundary + k + eventFinder + eventBoundary + cr._2, messageTypes.event)
//            PopMode.Event.action = 0
//            app.copy(input = "", pop = PopMode.NoPop)
//          }
//        }
//        else app.copy(input = tt)
//
//      case c: KeyCode.Char if c.c.isDigit && ((PopMode.Event.action == 2) || (PopMode.Event.action == 3) || (PopMode.Event.action == 4)) =>
//        app.copy(input = app.input + c.c)

//      case _: KeyCode.Backspace if (PopMode.Event.action == 2) || (PopMode.Event.action == 3) || (PopMode.Event.action == 4) =>
//        if (app.input.length > 1) && (app.input.last != '\\') then app.copy(input = app.input.dropRight(1))
//        else app

//      case c: KeyCode.Char if (c.c.toLower == 'w') && (PopMode.Event.action == 0) =>
//        PopMode.Event.action = 1
//        app.copy(input = "w")
//
//      case c: KeyCode.Char if (c.c.toLower == 'c') && (PopMode.Event.action == 0) =>
//        PopMode.Event.action = 2
//        app.copy(input = "c")
//
//      case c: KeyCode.Char if (checkWeek(c.c.toUpper) != "") && (PopMode.Event.action == 1) =>
//        val dd = checkWeek(c.c.toUpper)
//        if app.input.contains(dd) then app.copy(input = app.input.replace(" : " + dd, "")) else app.copy(input = app.input + " : " + dd)


//      case _: KeyCode.Enter if (PopMode.Event.action == 1) && (app.input(0) == 'w') =>
//        val k = app.input.splitAt(1)._2.split(" : ").drop(1).map(wk2Ltr).mkString("")
//        val cr = app.getCurPosText.splitAt(app.curPos.x)
//        app.setCurText(cr._1 + eventBoundary + app.input(0) + k + eventFinder + eventBoundary + cr._2, messageTypes.event)
//        //        app.messages(app.curPos.y) = (app.messages(app.curPos.y)._1, cr._1 + eventBoundary + app.input(0) + k + eventFinder + eventBoundary + cr._2)
//        PopMode.Event.action = 0
//        app.copy(input = "", pop = PopMode.NoPop)



//      case _: KeyCode.Esc => app.copy(pop = PopMode.NoPop, input = "")

//    }
//  }

//  private def checkDate(dt: String) = dt.split('\\').map {
//    case "*" => Some(-5)
//    case i => i.toIntOption
//  }.zipWithIndex.map {
//    case (None, i) => None
//    case (Some(s), 0) if (s >= 2020) && (s <= 2500) => Some(s.toString)
//    case (Some(s), 0) if s == -5 => Some("*")
//    case (Some(s), 1) if (s >= 1) && (s <= 12) => Some(s.toString)
//    case (Some(s), 1) if s == -5 => Some("*")
//    case (Some(s), 2) if (s >= 1) && (s <= 31) => Some(s.toString)
//    case (Some(s), 2) if s == -5 => Some("*")
//    case _ => None
//  }

//  private def wk2Ltr(str: String): String = str match {
//    case "Saturday" => "A"
//    case "Thursday" => "H"
//    case wk => wk(0).toString
//  }

//  private def checkWeek(c: Char): String = c match {
//    case 'S' => "Sunday"
//    case 'M' => "Monday"
//    case 'T' => "Tuesday"
//    case 'W' => "Wednesday"
//    case 'H' => "Thursday"
//    case 'F' => "Friday"
//    case 'A' => "Saturday"
//    case _ => ""
//  }
}
