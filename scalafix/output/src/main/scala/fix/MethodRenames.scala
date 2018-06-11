package fix

import scala.collection.mutable.Map

object MethodRenames {
  Map(1 -> 1).filterInPlace{ case (x, y) => true }
}