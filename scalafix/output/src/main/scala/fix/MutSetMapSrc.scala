


package fix

import scala.collection.mutable

class MutSetMapSrc(map: mutable.Map[Int, Int], set: mutable.Set[Int]) {
  set.clone() += 2
  map.clone() += (2 -> 3)
  (set.clone() += 2).size
}