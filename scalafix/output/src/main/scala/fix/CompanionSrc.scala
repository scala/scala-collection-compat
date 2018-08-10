


package fix

import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}
import scala.collection
import scala.collection.{ immutable, mutable }
import scala.collection.compat._

object CompanionSrc {

  (null: c.IndexedSeq[Int]).iterableFactory
  (null: c.Iterable[Int]).iterableFactory
  (null: c.Seq[Int]).iterableFactory
  (null: collection.Iterable[Int]).iterableFactory

  (null: i.HashSet[Int]).iterableFactory
  (null: i.IndexedSeq[Int]).iterableFactory
  (null: i.Iterable[Int]).iterableFactory
  (null: i.LinearSeq[Int]).iterableFactory
  (null: i.List[Int]).iterableFactory
  (null: i.ListSet[Int]).iterableFactory
  (null: i.Queue[Int]).iterableFactory
  (null: i.Seq[Int]).iterableFactory
  (null: i.Set[Int]).iterableFactory
  (null: i.Stream[Int]).iterableFactory
  (null: immutable.Iterable[Int]).iterableFactory
  (null: i.Vector[Int]).iterableFactory

  (null: m.ArrayBuffer[Int]).iterableFactory
  (null: m.ArraySeq[Int]).iterableFactory
  (null: m.ArrayStack[Int]).iterableFactory
  (null: m.Buffer[Int]).iterableFactory
  (null: m.HashSet[Int]).iterableFactory
  (null: m.IndexedSeq[Int]).iterableFactory
  (null: m.Iterable[Int]).iterableFactory
  (null: m.LinearSeq[Int]).iterableFactory
  (null: m.LinkedHashSet[Int]).iterableFactory
  (null: m.Queue[Int]).iterableFactory
  (null: m.Seq[Int]).iterableFactory
  (null: m.Set[Int]).iterableFactory
  (null: mutable.Iterable[Int]).iterableFactory
}
