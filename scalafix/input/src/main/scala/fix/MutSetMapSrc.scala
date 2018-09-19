/*
rule = "Collection213CrossCompat"
 */
package fix

import scala.collection.mutable

class MutSetMapSrc(map: mutable.Map[Int, Int], set: mutable.Set[Int]) {
  set + 2
  map + (2 -> 3)
  (set + 2).size
  map.updated(1, 3)
}
