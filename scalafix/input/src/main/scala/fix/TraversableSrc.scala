/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.collection.immutable
import scala.collection.mutable

trait TraversableSrc {
  val to: TraversableOnce[Int]
  val cto: collection.TraversableOnce[Int]

  val t: Traversable[Int]
  val ct: collection.Traversable[Int]
  val it: immutable.Traversable[Int]
  val mt: mutable.Traversable[Int]
}
