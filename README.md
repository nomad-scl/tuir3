# tuir3

---

a simple scala program that functions as a logseq alternative for taking notes in the terminal

---

## details

this program mainly uses the tui library for drawing and handeling the user input in the terminal aas well as using typelevel doobie library for accessing the sqlite database



the following code demonstrates the use of functional programming to create functions that can be composed together in order to handle inputs



```scala
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

```


