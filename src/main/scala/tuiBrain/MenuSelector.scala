package tuiBrain

sealed trait MenuSelector {
  var subMenu : String
  var msgBox : String = ""
}
object MenuSelector {
  case object Journaling extends MenuSelector{
    var subMenu : String = "noSubMenu"
  }
  case object QA extends MenuSelector{
    var subMenu : String = "0"
  }
}
