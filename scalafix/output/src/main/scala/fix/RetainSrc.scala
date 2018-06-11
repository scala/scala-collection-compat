package fix

import scala.collection.mutable.Map

class MethodRenames(xs: Map[Int, Int]) {
  xs.filterInPlace{case (_, _) => true}
  xs.filterInPlace{case (x, y) => true}
}