package tuiBrain

sealed trait PopMode
object PopMode{
  case object NoPop extends PopMode
  case object Topic extends PopMode
  case object YesNo extends PopMode
  case object Editor extends PopMode //a popUp that can be edited
  case object Event extends PopMode {  //a popup for choosing the event date
    var action : Int = 0
  }
  case object Card extends PopMode {
    var showAnswer: Boolean = false
  }
}
