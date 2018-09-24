/*
rule = "Collection213CrossCompat"
 */
package fix

import scala.collection.{mutable => m}

object SortedSrc212Plus {
  m.SortedMap(1 -> 1).from(0)
  m.TreeMap(1 -> 1).from(0)
  m.SortedMap(1 -> 1).to(0)
  m.TreeMap(1 -> 1).to(0)
  m.SortedMap(1 -> 1).until(0)
  m.TreeMap(1 -> 1).until(0)
}
