/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

import scala.collection.breakOut

object BreakoutSrc {
  val xs = List(1, 2, 3)

  xs.collect{ case x => x }(breakOut): Set[Int]
  xs.flatMap(x => List(x))(breakOut): Set[Int]
  xs.map(_ + 1)(breakOut): Set[Int]
  xs.reverseMap(_ + 1)(breakOut): Set[Int]
  xs.scanLeft(0)((a, b) => a + b)(breakOut): Set[Int]
  xs.union(xs)(breakOut): Set[Int]
  xs.updated(0, 1)(breakOut): Set[Int]
  xs.zip(xs)(breakOut): Array[(Int, Int)]
  xs.zipAll(xs, 0, 0)(breakOut): Array[(Int, Int)]

  (xs ++ xs)(breakOut): Set[Int]
  (1 +: xs)(breakOut): Set[Int]
  (xs :+ 1)(breakOut): Set[Int]
  (xs ++: xs)(breakOut): Set[Int]
}