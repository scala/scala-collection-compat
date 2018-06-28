/*
rule = "scala:fix.NewCollections"
 */
package fix

import scala.collection.mutable.{Map, Set}

class MethodRenames(xs: Map[Int, Int], ys: Set[Int]) {
  xs.retain((_, _) => true)
  xs.retain{case (x, y) => true}
  ys.retain(_ => true)
}
