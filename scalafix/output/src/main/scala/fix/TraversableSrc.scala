


package fix

import scala.collection.immutable
import scala.collection.mutable
import scala.{ Iterable, collection }
import scala.collection.{ immutable, mutable }
import scala.collection.compat._

trait TraversableSrc {
  val to: IterableOnce[Int]
  val cto: IterableOnce[Int]

  val t: Iterable[Int]
  t.iterator
  val ct: collection.Iterable[Int]
  val it: immutable.Iterable[Int]
  val mt: mutable.Iterable[Int]
}
