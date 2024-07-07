package tuiBrain

import tuiBrain.UI.Global.{QABoundary, eventBoundary, eventFinder}
import tuiBrain.UI.SearchableList
import tuiBrain.UI.SearchableList.{StatefulList, listOfString}


case class AppData(
                    load : String = currentDate,
                    input: String = "",
                    input_mode: InputMode = InputMode.Normal,
                    tempBuffer: Array[(Int, String)] = Array((0, "")),
                    pop: PopMode = PopMode.NoPop,
                    curPos: CurPos = CurPos(0, 0),
                    menu: MenuSelector = MenuSelector.Journaling,
                    topics: Array[String] = Array(),
                    journal: Array[(EditStatus, JournalEntry)] = Array(emptyEntry),
                    filteredTopics: SearchableList.StatefulList[String] = StatefulList[String](items = Array())
                  ){
  def getCurPosText : String = this.journal(this.curPos.y)._2.message
  def getCurPosType : String = this.journal(this.curPos.y)._2.entryTp
  def getCurPosTopics : String = this.journal(this.curPos.y)._2.topics
  def getCurPosOrder : Double = this.journal(this.curPos.y)._2.roworder
  def getCurPosLevel : Int = this.journal(this.curPos.y)._2.msglevel

  def gX : Int = this.curPos.x

  def editStat : EditStatus = this.journal(this.curPos.y)._1 match {
    case Insert => Insert
    case _ => Update(this.getEditStatDelList, this.getEditStatAddList)
  }
  
  def isEmpty : Boolean = (this.journal.length == 1) && (this.getCurPosText == "")
  

  def setCurText(nTxt : String, tp : String) : Unit = {
    this.journal(this.curPos.y) = (this.editStat, this.journal(this.curPos.y)._2.copy(message = nTxt, entryTp = tp))
  }

  def setTask(tsk : String) : Unit = {
    val k = if this.getCurPosText(0) == '[' then tsk + this.getCurPosText.drop(3) else tsk + this.getCurPosText
    this.journal(this.curPos.y) =
      (this.editStat, this.journal(this.curPos.y)._2.copy(message = k,
        entryTp = if tsk == "" then messageTypes.text else messageTypes.task))
  }

  def setQA(): Unit = {
    this.journal(this.curPos.y) =
      (this.editStat, this.journal(this.curPos.y)._2.copy(
        entryTp = if this.getCurPosText.contains(QABoundary) then messageTypes.qa else messageTypes.text))
  }

  def deleteCurrent() : Array[(EditStatus, JournalEntry)] =
    if this.editStat == Insert then this.journal.zipWithIndex.filter(_._2 != this.curPos.y).map(_._1)
    else {
      this.journal(this.curPos.y) = (Delete(this.getEditStatDelList), this.journal(this.curPos.y)._2)
      this.journal
    }

  def removeTopic(tp : String) : Unit = {
    val k = this.editStat match {
      case Update(None, z) => Update(Some(Set(tp)), z)
      case Update(Some(s), z) => Update(Some(s + tp), z)
      case Insert => Insert
    }
    this.journal(this.curPos.y) = (k, this.journal(this.curPos.y)._2
      .copy(topics = (this.getCurPosTopics.split(",").toSet - tp).mkString(",")))
  }

  def addTopic(tp : String): Unit = this.journal(this.curPos.y) = {
    val k = if this.journal(this.curPos.y)._1 == Insert then Insert else Update(this.getEditStatDelList, this.getEditStatAddList.map(_ + tp))
    val top = (this.getCurPosTopics.split(",").toSet + tp).mkString(",")
    (k, this.journal(this.curPos.y)._2
      .copy(topics = if top(0) == ',' then top.drop(1) else top))
  }


  def increaseCurrentLevel() : Unit =
    this.journal(this.curPos.y) = (this.journal(this.curPos.y)._1, this.journal(this.curPos.y)._2.copy(msglevel = this.journal(this.curPos.y)._2.msglevel + 1))
    
  def decreaseCurrentLevel() : Unit = {
    val z = this.journal(this.curPos.y)._2.msglevel - 1
    this.journal(this.curPos.y) = (this.journal(this.curPos.y)._1, this.journal(this.curPos.y)._2.copy(msglevel = if z < 0 then 0 else z))
  }

  def getNewOrder : Double = {
    val k = getCurPosOrder
    if this.curPos.y >= (this.journal.length - 1) then k + 1.0 else k + ((this.journal(this.curPos.y + 1)._2.roworder - k) / 2)
  }

  def getEditStatDelList : Option[Set[String]] = this.journal(this.curPos.y)._1 match {
    case Insert => None
    case DoNothing => None
    case u : Update => u.del
    case d : Delete => d.del
    case _ => None
  }

  def getEditStatAddList: Option[Set[String]] = this.journal(this.curPos.y)._1 match {
    case u: Update => u.add
    case _ => None
  }

}
