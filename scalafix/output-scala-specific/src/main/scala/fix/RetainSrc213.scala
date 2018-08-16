


package fix

import scala.collection.mutable.{Map, Set}

class RetainSrc213(xs: Map[Int, Int], ys: Set[Int]) {
  xs.filterInPlace{case (_, _) => true}
  xs.filterInPlace{case (x, y) => true}
  ys.filterInPlace(_ => true)
}
