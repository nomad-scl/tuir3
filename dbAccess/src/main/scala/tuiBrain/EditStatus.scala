package tuiBrain

trait EditStatus

case object Insert extends EditStatus
case object DoNothing extends EditStatus
//case object Delete extends EditStatus
case class Update(del : Option[Set[String]] = None, add : Option[Set[String]] = None) extends EditStatus
case class Delete(del : Option[Set[String]] = None) extends EditStatus
