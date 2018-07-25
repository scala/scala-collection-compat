/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.collection.{immutable => i, mutable => m}
import scala.{collection => c}

object CompanionSrc {

  (null: c.IndexedSeq[Int]).companion
  (null: c.Iterable[Int]).companion
  (null: c.Seq[Int]).companion
  (null: c.Traversable[Int]).companion

  (null: i.HashSet[Int]).companion
  (null: i.IndexedSeq[Int]).companion
  (null: i.Iterable[Int]).companion
  (null: i.LinearSeq[Int]).companion
  (null: i.List[Int]).companion
  (null: i.ListSet[Int]).companion
  (null: i.Queue[Int]).companion
  (null: i.Seq[Int]).companion
  (null: i.Set[Int]).companion
  (null: i.Stack[Int]).companion
  (null: i.Stream[Int]).companion
  (null: i.Traversable[Int]).companion
  (null: i.Vector[Int]).companion

  (null: m.ArrayBuffer[Int]).companion
  (null: m.ArraySeq[Int]).companion
  (null: m.ArrayStack[Int]).companion
  (null: m.Buffer[Int]).companion
  (null: m.DoubleLinkedList[Int]).companion
  (null: m.HashSet[Int]).companion
  (null: m.IndexedSeq[Int]).companion
  (null: m.Iterable[Int]).companion
  (null: m.LinearSeq[Int]).companion
  (null: m.LinkedHashSet[Int]).companion
  (null: m.LinkedList[Int]).companion
  (null: m.MutableList[Int]).companion
  (null: m.Queue[Int]).companion
  (null: m.ResizableArray[Int]).companion
  (null: m.Seq[Int]).companion
  (null: m.Set[Int]).companion
  (null: m.Traversable[Int]).companion
}
