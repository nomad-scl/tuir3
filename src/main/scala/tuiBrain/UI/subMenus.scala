package tuiBrain.UI

import tui.crossterm.Event.Key
import tui.crossterm.KeyCode
import tui.widgets.TableWidget
//import tuiBrain.Handelers.getSelected
import tuiBrain.UI.Global.{QABoundary, TopicBolder, TopicBrake}
import tuiBrain.{AppData, CurPos, DeleteMe, ExitStatus, MenuSelector, PopMode}

import scala.util.Random

object subMenus {

//  def d6(app : AppData) : AppData = {
//    val k = if app.input.nonEmpty then app.copy(input = app.input.dropRight(1)) else app.copy(input = "")
////    SearchableList.listOfString = SearchableList.StatefulList(items = tempList.filter(_.contains(k.input)))
//    SearchableList.listOfString = SearchableList.StatefulList(items = app.topics.filter(_.contains(k.input)))
//    k
//  }

//  def subMenu0(app : AppData, key : Key) : (AppData, ExitStatus) = key.keyEvent().code() match {
//    case c : KeyCode.Char if c.c == 'J' => (app.copy(menu = MenuSelector.Journaling, tempBuffer = Array((0, ""))), ExitStatus.Run)
//    case c : KeyCode.Char if c.c == 'q' => (app, ExitStatus.Quit)
//    case c : KeyCode.Char if c.c == 'V' => app.menu.subMenu = "1" ; (app.copy(pop = PopMode.Topic), ExitStatus.Run)
//    case c : KeyCode.Char if c.c == 'B' => app.menu.subMenu = "2" ; (app.copy(pop = PopMode.Topic), ExitStatus.Run)
//
//    case _ => (app, ExitStatus.Run)
//  }
//
//
//  def subMenu1(app : AppData, key : Key) : AppData = key.keyEvent().code() match {
//    case _: KeyCode.Up if app.pop == PopMode.NoPop => TableMaker.tableOfString.previous()
//                                                      app
//    case _: KeyCode.Down if app.pop == PopMode.NoPop => TableMaker.tableOfString.previous()
//                                                        app
//
//    case _: KeyCode.Enter if app.pop == PopMode.NoPop => app.copy(pop = PopMode.YesNo)
//    case _: KeyCode.Enter if app.pop == PopMode.Topic => val
//      kk = getSelected(app)
//      DeleteMe.tempFilteredQAs =
//        DeleteMe.tempQAs.filter(z => z._1.contains(TopicBrake + kk.input + TopicBolder + TopicBrake) || z._2.contains(TopicBrake + kk.input + TopicBolder + TopicBrake));
//      TableMaker.tableOfString = TableMaker.TableData(state = TableWidget.State(), items = DeleteMe.tempFilteredQAs.map(Array(_, _)))
//      kk
//
//    case _: KeyCode.Esc => app.menu.subMenu = "0"; app.copy(pop = PopMode.NoPop)
//
//    case c : KeyCode.Char if (c.c == 'D') && (app.pop == PopMode.NoPop) => app.menu.subMenu = "4"; app.copy(pop = PopMode.YesNo)
//    case c : KeyCode.Char if (c.c == 'A') && (app.pop == PopMode.YesNo) => app.menu.subMenu = "4"
//                                                                          val kr = TableMaker.tableOfString.state.selected.get
//      app.copy(pop = PopMode.Editor, tempBuffer = Array((kr, TableMaker.tableOfString.items(kr)(1)), (kr, TableMaker.tableOfString.items(kr)(1))))
//
//    case c : KeyCode.Char if (c.c == 'U') && (app.menu.subMenu == "1") && (app.pop == PopMode.YesNo) => app.menu.subMenu = "4"
//      val kr = TableMaker.tableOfString.state.selected.get
//      app.copy(pop = PopMode.Editor, tempBuffer = Array((kr, TableMaker.tableOfString.items(kr)(0)), (kr, TableMaker.tableOfString.items(kr)(0))))
//
//    case _ => app
//  }
//
//  def subMenu3(app : AppData, key : Key) : AppData = key.keyEvent().code() match {
//    case _: KeyCode.Esc => app.menu.subMenu = "0"; app.copy(pop = PopMode.NoPop)
//
//    case c : KeyCode.Char if (c.c == 'N') && (app.pop == PopMode.Card) && PopMode.Card.showAnswer =>
//      PopMode.Card.showAnswer = false
//      val in2 = app.input.split(" ")
//      val in3 = in2(2).toInt + 1
//      if in3 <= in2(4).toInt then app.copy(curPos = CurPos(0, app.curPos.y + 1), input = s"Card { ${in3.toString} of ${in2(4)} }")
//      else {
//        app.menu.subMenu = "0"
//        app.menu.msgBox = "You have finished this question round"
//        app.copy(curPos = CurPos(0, 0), input = "", tempBuffer = Array((0, "")), pop = PopMode.NoPop, menu = MenuSelector.QA)
//      }
//
//    case c : KeyCode.Char if (c.c == 'S') && (app.pop == PopMode.Card) && (!PopMode.Card.showAnswer) =>
//      PopMode.Card.showAnswer = true
//      app
//
//    case c : KeyCode.Char if (c.c == 'A') && (app.pop == PopMode.YesNo) =>
//      val mm = DeleteMe.tempFilteredQAs.map((m1,m2) => (0, m1 + QABoundary + m2))
//      if mm.nonEmpty then
//      app.copy(tempBuffer = mm, curPos = CurPos(0,0), pop = PopMode.Card, input = "Card { 1 of " + DeleteMe.tempFilteredQAs.length.toString + " }")
//      else {
//        app.menu.subMenu = "0"
//        app.menu.msgBox = "There are no questions with these selected topics"
//        app.copy(tempBuffer = Array((0, "")), input = "", pop = PopMode.YesNo)
//      }
//
//    case c : KeyCode.Char if c.c.isDigit && (app.pop == PopMode.YesNo) => app.copy(input = app.input + c.c)
//
//    case _: KeyCode.Enter if app.pop == PopMode.YesNo =>
//      val mm = DeleteMe.tempFilteredQAs.map((m1,m2) => (0, m1 + QABoundary + m2)).take(1)
//      val n2 = if app.input.toInt != 0 then app.input.toInt else 1
//      val n = if n2 > DeleteMe.tempFilteredQAs.length then DeleteMe.tempFilteredQAs.length else n2
//      if mm.nonEmpty then
//      app.copy(tempBuffer = (1 to n).map(_ => mm(Random.nextInt(mm.length))).toArray, curPos = CurPos(0,0), pop = PopMode.Card, input = "Card { 1 of " + n.toString + " }")
//      else {
//        app.menu.subMenu = "0"
//        app.menu.msgBox = "There are no questions with these selected topics"
//        app.copy(tempBuffer = Array((0, "")), input = "", pop = PopMode.YesNo)
//      }
//
//    case _ => app
//  }
//
//  def subMenu2(app : AppData, key : Key) : AppData = key.keyEvent().code() match {
//    case _: KeyCode.Esc => app.menu.subMenu = "0"; app.copy(pop = PopMode.NoPop)
////    case c: KeyCode.Char if app.pop == PopMode.Topic => SearchableList.listOfString = SearchableList.StatefulList(items = tempList.filter(_.contains(app.input + c.c)))
//    case c: KeyCode.Char if app.pop == PopMode.Topic => SearchableList.listOfString = SearchableList.StatefulList(items = app.topics.filter(_.contains(app.input + c.c)))
//                                        app.copy(input = app.input + c.c)
////    case _: KeyCode.Backspace if app.pop == PopMode.Topic => d6(app)
//    case _: KeyCode.Enter if app.pop == PopMode.Topic => getSelected(app)
//    case c : KeyCode.Char if (c.c == 'Y') && (app.pop == PopMode.YesNo) =>
//      app.tempBuffer(app.tempBuffer.length - 1) = (0, app.input)
//      app.copy(pop = PopMode.Topic, tempBuffer = app.tempBuffer :+ (0, ""), input = "")
//
//    case c : KeyCode.Char if (c.c == 'N') && (app.pop == PopMode.YesNo) =>
//      app.copy(pop = PopMode.Topic, input = "")
//
//    case c : KeyCode.Char if (c.c == 'G') && (app.pop == PopMode.YesNo) =>
//      app.menu.subMenu = "3"
//      app.tempBuffer(app.tempBuffer.length - 1) = (0, app.input)
//      val subs = app.tempBuffer.map(_._2).distinct.map(TopicBrake + _ + TopicBolder + TopicBrake)
//      DeleteMe.tempFilteredQAs = DeleteMe.tempQAs.filter((m1, m2) => subs.exists((m1+m2).contains(_)))
//      app.copy(pop = PopMode.YesNo, input = "")
//
//    case _ => app
//  }
//
//  def subMenu4(app : AppData, key : Key) : AppData = key.keyEvent().code() match {
//    case c : KeyCode.Char if (c.c == 'Y') && (app.pop == PopMode.YesNo) =>
//      //TODO: Delete from original datasource
//      //TODO: Delete from filtered source
//      app.copy(pop = PopMode.NoPop)
//    case c : KeyCode.Char if (c.c == 'N') && (app.pop == PopMode.YesNo) =>
//      app.menu.subMenu = "1"
//      app.copy(pop = PopMode.NoPop)
//
//    case c : KeyCode.Char if app.pop == PopMode.Editor =>
//      app.tempBuffer(1) = (app.tempBuffer(1)._1, app.tempBuffer(1)._2 + c.c())
//      app
//
//    case _ : KeyCode.Backspace if (app.menu.subMenu == "4") && (app.pop == PopMode.Editor) =>
//      val cr = app.tempBuffer(1)
//      if cr._2.endsWith(TopicBrake) then {
//        val pos = cr._2.lastIndexOf(TopicBrake, cr._2.lastIndexOf(TopicBrake) - 1)
//        app.tempBuffer(1) = (cr._1, cr._2.substring(0, pos))
//      } else app.tempBuffer(1) = (cr._1, app.tempBuffer(1)._2.dropRight(1))
//
//      app
//
//    case _: KeyCode.Esc => app.menu.subMenu = "1"; app.copy(pop = PopMode.NoPop)
//
//    case _ : KeyCode.Enter if (app.menu.subMenu == "4") && (app.pop == PopMode.Editor) =>
//      //TODO: Update original source
//      //TODO: Update filtered source source
//      app.menu.subMenu = "1"
//      app.copy(pop = PopMode.NoPop, tempBuffer = Array((0, "")))
//
//    case _ => app
//  }

}
