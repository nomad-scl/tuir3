package tuiBrain.HandleInputs

import tuiBrain.AppData
import tui.crossterm.Event.Key

trait InputHandlers{
  def handleInput(app : AppData, key: Key) : (AppData, InputHandlers)
  }

trait O(val app : AppData, val key: Key, val nextState : Option[InputHandlers]){
  def handle : (AppData, Option[InputHandlers])
}

extension (s : O)
  def orHandle(f : (AppData, Key) => (AppData, Option[InputHandlers])) : O = {
    if s.nextState.isEmpty then
      val res = s.handle
      new O(res._1, s.key, res._2):
        override def handle: (AppData, Option[InputHandlers]) = if this.nextState.nonEmpty then (this.app, this.nextState) else f(this.app, this.key)
    else
      s
  }

object O{
  def apply(app : AppData, key : Key, f : (AppData, Key) => (AppData, Option[InputHandlers])) : O =
    new O(app, key, None):
      override def handle: (AppData, Option[InputHandlers]) = if this.nextState.nonEmpty then (this.app, this.nextState) else f(this.app, this.key)
}
