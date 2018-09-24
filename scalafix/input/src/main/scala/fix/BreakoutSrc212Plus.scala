/*
rule = "Collection213CrossCompat"
 */
package fix

import scala.collection.mutable
import scala.collection.breakOut

object BreakoutSrc212Plus {
  List(1 -> "1").map(x => x)(breakOut): mutable.SortedMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): mutable.TreeMap[Int, String]
}
