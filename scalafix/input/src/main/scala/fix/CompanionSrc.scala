/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.collection.immutable
import scala.collection.mutable

object CompanionSrc {

  (null: collection.IndexedSeq[Int]).companion
  (null: collection.Iterable[Int]).companion
  (null: collection.Seq[Int]).companion
  (null: collection.Traversable[Int]).companion

  (null: immutable.HashSet[Int]).companion
  (null: immutable.IndexedSeq[Int]).companion
  (null: immutable.Iterable[Int]).companion
  (null: immutable.LinearSeq[Int]).companion
  (null: immutable.List[Int]).companion
  (null: immutable.ListSet[Int]).companion
  (null: immutable.Queue[Int]).companion
  (null: immutable.Seq[Int]).companion
  (null: immutable.Set[Int]).companion
  (null: immutable.Stream[Int]).companion
  (null: immutable.Traversable[Int]).companion
  (null: immutable.Vector[Int]).companion

  (null: mutable.ArrayBuffer[Int]).companion
  (null: mutable.ArraySeq[Int]).companion
  (null: mutable.ArrayStack[Int]).companion
  (null: mutable.Buffer[Int]).companion
  (null: mutable.HashSet[Int]).companion
  (null: mutable.IndexedSeq[Int]).companion
  (null: mutable.Iterable[Int]).companion
  (null: mutable.LinearSeq[Int]).companion
  (null: mutable.LinkedHashSet[Int]).companion
  (null: mutable.Queue[Int]).companion
  (null: mutable.Seq[Int]).companion
  (null: mutable.Set[Int]).companion
  (null: mutable.Traversable[Int]).companion
}
