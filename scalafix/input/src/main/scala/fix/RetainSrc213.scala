/*
rule = "Collection213Upgrade"
 */
package fix

import scala.collection.mutable.{Map, Set}

class RetainSrc213(xs: Map[Int, Int], ys: Set[Int]) {
  xs.retain((_, _) => true)
  xs.retain{case (x, y) => true}
  ys.retain(_ => true)
}
