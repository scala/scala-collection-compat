/*
rule = "scala:fix.CrossCompat"
 */
package fix

trait TraversableSrc {
  val t: Traversable[Int]
  val to: TraversableOnce[Int]
}
