/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.collection.compat

import scala.annotation.{nowarn, tailrec}
import scala.collection.generic._
import scala.reflect.ClassTag
import scala.collection.{
  GenTraversable,
  IterableLike,
  IterableView,
  MapLike,
  TraversableLike,
  immutable => i,
  mutable => m
}
import scala.runtime.{Tuple2Zipped, Tuple3Zipped}
import scala.util.Random
import scala.{collection => c}

/** The collection compatibility API */
private[compat] trait PackageShared {
  import CompatImpl._

  /**
   * A factory that builds a collection of type `C` with elements of type `A`.
   *
   * @tparam A Type of elements (e.g. `Int`, `Boolean`, etc.)
   * @tparam C Type of collection (e.g. `List[Int]`, `TreeMap[Int, String]`, etc.)
   */
  type Factory[-A, +C] = CanBuildFrom[Nothing, A, C]

  implicit class FactoryOps[-A, +C](private val factory: Factory[A, C]) {

    /**
     * @return A collection of type `C` containing the same elements
     *         as the source collection `it`.
     * @param it Source collection
     */
    def fromSpecific(it: TraversableOnce[A]): C = (factory() ++= it).result()

    /** Get a Builder for the collection. For non-strict collection types this will use an intermediate buffer.
     * Building collections with `fromSpecific` is preferred because it can be lazy for lazy collections. */
    def newBuilder: m.Builder[A, C] = factory()
  }

  implicit class StringOps(s: String) {

    /**
     * Try to parse as a `Boolean`
     * @return `Some(true)` if the string is "true" case insensitive,
     * `Some(false)` if the string is "false" case insensitive,
     * and `None` if the string is anything else
     * @throws java.lang.NullPointerException if the string is `null`
     */
    def toBooleanOption: Option[Boolean] = StringParsers.parseBool(s)

    /**
     * Try to parse as a `Byte`
     * @return `Some(value)` if the string contains a valid byte value, otherwise `None`
     * @throws java.lang.NullPointerException if the string is `null`
     */
    def toByteOption: Option[Byte] = StringParsers.parseByte(s)

    /**
     * Try to parse as a `Short`
     * @return `Some(value)` if the string contains a valid short value, otherwise `None`
     * @throws java.lang.NullPointerException if the string is `null`
     */
    def toShortOption: Option[Short] = StringParsers.parseShort(s)

    /**
     * Try to parse as an `Int`
     * @return `Some(value)` if the string contains a valid Int value, otherwise `None`
     * @throws java.lang.NullPointerException if the string is `null`
     */
    def toIntOption: Option[Int] = StringParsers.parseInt(s)

    /**
     * Try to parse as a `Long`
     * @return `Some(value)` if the string contains a valid long value, otherwise `None`
     * @throws java.lang.NullPointerException if the string is `null`
     */
    def toLongOption: Option[Long] = StringParsers.parseLong(s)

    /**
     * Try to parse as a `Float`
     * @return `Some(value)` if the string is a parsable `Float`, `None` otherwise
     * @throws java.lang.NullPointerException If the string is null
     */
    def toFloatOption: Option[Float] = StringParsers.parseFloat(s)

    /**
     * Try to parse as a `Double`
     * @return `Some(value)` if the string is a parsable `Double`, `None` otherwise
     * @throws java.lang.NullPointerException If the string is null
     */
    def toDoubleOption: Option[Double] = StringParsers.parseDouble(s)
  }

  implicit def genericCompanionToCBF[A, CC[X] <: GenTraversable[X]](
      fact: GenericCompanion[CC]): CanBuildFrom[Any, A, CC[A]] = {
    /* see https://github.com/scala/scala-collection-compat/issues/337
       `simpleCBF.apply` takes a by-name parameter and relies on
       repeated references generating new builders, thus this expression
       must be non-strict
     */
    def builder: m.Builder[A, CC[A]] = fact match {
      case c.Seq | i.Seq =>
        new IdentityPreservingBuilder[A, i.Seq](i.Seq.newBuilder[A])

      case c.LinearSeq | i.LinearSeq =>
        new IdentityPreservingBuilder[A, i.LinearSeq](i.LinearSeq.newBuilder[A])
      case i.Queue =>
        new IdentityPreservingBuilder[A, i.Queue](i.Queue.newBuilder[A])
      case i.Stream =>
        new IdentityPreservingBuilder[A, i.Stream](i.Stream.newBuilder[A])
      case i.Stack =>
        new IdentityPreservingBuilder[A, i.Stack](i.Stack.newBuilder[A]): @nowarn("cat=deprecation")
      case i.List =>
        new IdentityPreservingBuilder[A, i.List](i.List.newBuilder[A])

      case c.IndexedSeq | i.IndexedSeq =>
        new IdentityPreservingBuilder[A, i.IndexedSeq](i.IndexedSeq.newBuilder[A])
      case i.Vector =>
        new IdentityPreservingBuilder[A, i.Vector](i.Vector.newBuilder[A])

      case c.Set | i.Set =>
        new IdentityPreservingBuilder[A, i.Set](i.Set.newBuilder[A])

      case i.HashSet =>
        new IdentityPreservingBuilder[A, i.HashSet](i.HashSet.newBuilder[A])
      case i.ListSet =>
        new IdentityPreservingBuilder[A, i.ListSet](i.ListSet.newBuilder[A])

      case c.Iterable | i.Iterable =>
        new IdentityPreservingBuilder[A, i.Iterable](i.Iterable.newBuilder[A])

      case c.Traversable | i.Traversable =>
        new IdentityPreservingBuilder[A, i.Traversable](i.Traversable.newBuilder[A])

      case _ => fact.newBuilder[A]
    }
    simpleCBF(builder)
  }

  implicit def sortedSetCompanionToCBF[A: Ordering,
                                       CC[X] <: c.SortedSet[X] with c.SortedSetLike[X, CC[X]]](
      fact: SortedSetFactory[CC]): CanBuildFrom[Any, A, CC[A]] = {
    def builder: m.Builder[A, CC[A]] = {
      val b = fact match {
        case c.SortedSet | i.SortedSet =>
          new IdentityPreservingBuilder[A, i.SortedSet](i.SortedSet.newBuilder[A])
        case i.TreeSet =>
          new IdentityPreservingBuilder[A, i.TreeSet](i.TreeSet.newBuilder[A])
        case _ =>
          fact.newBuilder[A]
      }
      // Cast needed because GADT inference doesn't unify CC (didn't dig down why). Example:
      //   def t: CC[A] = fact match { case i.SortedSet => null: i.SortedSet[A] }
      b.asInstanceOf[m.Builder[A, CC[A]]]
    }
    simpleCBF(builder)
  }

  implicit def arrayCompanionToCBF[A: ClassTag](fact: Array.type): CanBuildFrom[Any, A, Array[A]] =
    simpleCBF(Array.newBuilder[A])

  // bounds should be `c.` but binary compatibility
  implicit def mapFactoryToCBF[K,
                               V,
                               CC[A, B] <: /*c.*/ Map[A, B] with /*c.*/ MapLike[A, B, CC[A, B]]](
      fact: MapFactory[CC]): CanBuildFrom[Any, (K, V), CC[K, V]] = {
    def builder: m.Builder[(K, V), CC[K, V]] = {
      val b = fact match {
        case c.Map | i.Map =>
          new IdentityPreservingMapBuilder[K, V, i.Map](i.Map.newBuilder[K, V])
        case i.HashMap =>
          new IdentityPreservingMapBuilder[K, V, i.HashMap](i.HashMap.newBuilder[K, V])
        case i.ListMap =>
          new IdentityPreservingMapBuilder[K, V, i.ListMap](i.ListMap.newBuilder[K, V])
        case _ =>
          fact.newBuilder[K, V]
      }
      // Cast needed because GADT inference doesn't unify CC (didn't dig down why). Example:
      //   def t: CC[K, V] = fact match { case i.Map => null: i.Map[K, V] }
      b.asInstanceOf[m.Builder[(K, V), CC[K, V]]]
    }
    simpleCBF(builder)
  }

  implicit def sortedMapFactoryToCBF[
      K: Ordering,
      V,
      CC[A, B] <: c.SortedMap[A, B] with c.SortedMapLike[A, B, CC[A, B]]](
      fact: SortedMapFactory[CC]): CanBuildFrom[Any, (K, V), CC[K, V]] = {
    def builder: m.Builder[(K, V), CC[K, V]] = {
      val b = fact match {
        case c.SortedMap | i.SortedMap =>
          new IdentityPreservingMapBuilder[K, V, i.SortedMap](i.SortedMap.newBuilder[K, V])
        case i.TreeMap =>
          new IdentityPreservingMapBuilder[K, V, i.TreeMap](i.TreeMap.newBuilder[K, V])
        case _ =>
          fact.newBuilder[K, V]
      }
      b.asInstanceOf[m.Builder[(K, V), CC[K, V]]]
    }
    simpleCBF(builder)
  }

  implicit def bitSetFactoryToCBF(
      fact: BitSetFactory[c.BitSet]): CanBuildFrom[Any, Int, c.BitSet] = {
    def builder: m.Builder[Int, c.BitSet] = fact match {
      case c.BitSet =>
        new IdentityPreservingBitSetBuilder[i.BitSet](i.BitSet.newBuilder)
      case _ =>
        fact.newBuilder
    }
    simpleCBF(builder)
  }

  implicit def immutableBitSetFactoryToCBF(
      fact: BitSetFactory[i.BitSet]): CanBuildFrom[Any, Int, ImmutableBitSetCC[Int]] = {
    def builder: m.Builder[Int, i.BitSet] = fact match {
      case i.BitSet =>
        new IdentityPreservingBitSetBuilder[i.BitSet](i.BitSet.newBuilder)
      case _ =>
        fact.newBuilder
    }
    simpleCBF(builder)
  }

  implicit def mutableBitSetFactoryToCBF(
      fact: BitSetFactory[m.BitSet]): CanBuildFrom[Any, Int, MutableBitSetCC[Int]] =
    simpleCBF(fact.newBuilder)

  implicit class IterableFactoryExtensionMethods[CC[X] <: GenTraversable[X]](
      private val fact: GenericCompanion[CC]) {
    def from[A](source: TraversableOnce[A]): CC[A] =
      fact.apply(source.toSeq: _*)
  }

  implicit def toGenericCompanionExtensionMethods[CC[X] <: GenTraversable[X]](
      companion: GenericCompanion[CC]
  ): GenericCompanionExtensionMethods[CC] = new GenericCompanionExtensionMethods[CC](companion)

  implicit class MapFactoryExtensionMethods[CC[A, B] <: Map[A, B] with MapLike[A, B, CC[A, B]]](
      private val fact: MapFactory[CC]) {
    def from[K, V](source: TraversableOnce[(K, V)]): CC[K, V] =
      fact.apply(source.toSeq: _*)
  }

  implicit class BitSetFactoryExtensionMethods[
      C <: scala.collection.BitSet with scala.collection.BitSetLike[C]](
      private val fact: BitSetFactory[C]) {
    def fromSpecific(source: TraversableOnce[Int]): C =
      fact.apply(source.toSeq: _*)
  }

  private[compat] def build[T, CC](builder: m.Builder[T, CC], source: TraversableOnce[T]): CC = {
    builder ++= source
    builder.result()
  }

  implicit def toArrayExtensions(fact: Array.type): ArrayExtensions =
    new ArrayExtensions(fact)

  implicit def toImmutableSortedMapExtensions(
      fact: i.SortedMap.type): ImmutableSortedMapExtensions =
    new ImmutableSortedMapExtensions(fact)

  implicit def toImmutableListMapExtensions(fact: i.ListMap.type): ImmutableListMapExtensions =
    new ImmutableListMapExtensions(fact)

  implicit def toImmutableHashMapExtensions(fact: i.HashMap.type): ImmutableHashMapExtensions =
    new ImmutableHashMapExtensions(fact)

  implicit def toImmutableTreeMapExtensions(fact: i.TreeMap.type): ImmutableTreeMapExtensions =
    new ImmutableTreeMapExtensions(fact)

  implicit def toImmutableIntMapExtensions(fact: i.IntMap.type): ImmutableIntMapExtensions =
    new ImmutableIntMapExtensions(fact)

  implicit def toImmutableLongMapExtensions(fact: i.LongMap.type): ImmutableLongMapExtensions =
    new ImmutableLongMapExtensions(fact)

  implicit def toMutableLongMapExtensions(fact: m.LongMap.type): MutableLongMapExtensions =
    new MutableLongMapExtensions(fact)

  implicit def toMutableHashMapExtensions(fact: m.HashMap.type): MutableHashMapExtensions =
    new MutableHashMapExtensions(fact)

  implicit def toMutableListMapExtensions(fact: m.ListMap.type): MutableListMapExtensions =
    new MutableListMapExtensions(fact)

  implicit def toMutableMapExtensions(fact: m.Map.type): MutableMapExtensions =
    new MutableMapExtensions(fact)

  implicit def toStreamExtensionMethods[A](stream: Stream[A]): StreamExtensionMethods[A] =
    new StreamExtensionMethods[A](stream)

  implicit def toSortedExtensionMethods[K, V <: Sorted[K, V]](
      fact: Sorted[K, V]): SortedExtensionMethods[K, V] =
    new SortedExtensionMethods[K, V](fact)

  implicit def toSortedMapExtensionMethods[K, V](
      fact: collection.SortedMap[K, V]): SortedMapExtensionMethods[K, V] =
    new SortedMapExtensionMethods[K, V](fact)

  implicit def toSortedSetExtensionMethods[A](
      fact: collection.SortedSet[A]): SortedSetExtensionMethods[A] =
    new SortedSetExtensionMethods[A](fact)

  implicit def toIteratorExtensionMethods[A](self: Iterator[A]): IteratorExtensionMethods[A] =
    new IteratorExtensionMethods[A](self)

  implicit def toTraversableExtensionMethods[A](
      self: Traversable[A]): TraversableExtensionMethods[A] =
    new TraversableExtensionMethods[A](self)

  implicit def toTraversableOnceExtensionMethods[A](
      self: TraversableOnce[A]): TraversableOnceExtensionMethods[A] =
    new TraversableOnceExtensionMethods[A](self)

  // This really belongs into scala.collection but there's already a package object
  // in scala-library so we can't add to it
  type IterableOnce[+X] = c.TraversableOnce[X]
  val IterableOnce = c.TraversableOnce

  implicit def toMapExtensionMethods[K, V](
      self: scala.collection.Map[K, V]): MapExtensionMethods[K, V] =
    new MapExtensionMethods[K, V](self)

  implicit def toImmutableMapExtensionMethods[K, V](
      self: scala.collection.immutable.Map[K, V]): ImmutableMapExtensionMethods[K, V] =
    new ImmutableMapExtensionMethods[K, V](self)

  implicit def toMutableMapExtensionMethods[K, V](
      self: scala.collection.mutable.Map[K, V]): MutableMapExtensionMethods[K, V] =
    new MutableMapExtensionMethods[K, V](self)

  implicit def toMapViewExtensionMethods[K, V, C <: scala.collection.Map[K, V]](
      self: IterableView[(K, V), C]): MapViewExtensionMethods[K, V, C] =
    new MapViewExtensionMethods[K, V, C](self)

  implicit def toOptionCompanionExtension(fact: Option.type): OptionCompanionExtensionMethods =
    new OptionCompanionExtensionMethods(fact)

  implicit def toRandomExtensions(self: Random): RandomExtensions =
    new RandomExtensions(self)
}

final class ArrayExtensions(private val fact: Array.type) extends AnyVal {
  def from[A: ClassTag](source: TraversableOnce[A]): Array[A] =
    source match {
      case it: Iterable[A] => it.toArray[A]
      case _ => source.toIterator.toArray[A]
    }
}

class ImmutableSortedMapExtensions(private val fact: i.SortedMap.type) extends AnyVal {
  def from[K: Ordering, V](source: TraversableOnce[(K, V)]): i.SortedMap[K, V] =
    build(i.SortedMap.newBuilder[K, V], source)
}

class ImmutableListMapExtensions(private val fact: i.ListMap.type) extends AnyVal {
  def from[K, V](source: TraversableOnce[(K, V)]): i.ListMap[K, V] =
    build(i.ListMap.newBuilder[K, V], source)
}

class ImmutableHashMapExtensions(private val fact: i.HashMap.type) extends AnyVal {
  def from[K, V](source: TraversableOnce[(K, V)]): i.HashMap[K, V] =
    build(i.HashMap.newBuilder[K, V], source)
}

class ImmutableTreeMapExtensions(private val fact: i.TreeMap.type) extends AnyVal {
  def from[K: Ordering, V](source: TraversableOnce[(K, V)]): i.TreeMap[K, V] =
    build(i.TreeMap.newBuilder[K, V], source)
}

class ImmutableIntMapExtensions(private val fact: i.IntMap.type) extends AnyVal {
  def from[V](source: TraversableOnce[(Int, V)]): i.IntMap[V] =
    build(i.IntMap.canBuildFrom[Int, V](), source)
}

class ImmutableLongMapExtensions(private val fact: i.LongMap.type) extends AnyVal {
  def from[V](source: TraversableOnce[(Long, V)]): i.LongMap[V] =
    build(i.LongMap.canBuildFrom[Long, V](), source)
}

class MutableLongMapExtensions(private val fact: m.LongMap.type) extends AnyVal {
  def from[V](source: TraversableOnce[(Long, V)]): m.LongMap[V] =
    build(m.LongMap.canBuildFrom[Long, V](), source)
}

class MutableHashMapExtensions(private val fact: m.HashMap.type) extends AnyVal {
  def from[K, V](source: TraversableOnce[(K, V)]): m.HashMap[K, V] =
    build(m.HashMap.canBuildFrom[K, V](), source)
}

class MutableListMapExtensions(private val fact: m.ListMap.type) extends AnyVal {
  def from[K, V](source: TraversableOnce[(K, V)]): m.ListMap[K, V] =
    build(m.ListMap.canBuildFrom[K, V](), source)
}

class MutableMapExtensions(private val fact: m.Map.type) extends AnyVal {
  def from[K, V](source: TraversableOnce[(K, V)]): m.Map[K, V] =
    build(m.Map.canBuildFrom[K, V](), source)
}

class StreamExtensionMethods[A](private val stream: Stream[A]) extends AnyVal {
  def lazyAppendedAll(as: => TraversableOnce[A]): Stream[A] = stream.append(as)
}

class SortedExtensionMethods[K, T <: Sorted[K, T]](private val fact: Sorted[K, T]) {
  def rangeFrom(from: K): T = fact.from(from)
  def rangeTo(to: K): T = fact.to(to)
  def rangeUntil(until: K): T = fact.until(until)
}

class SortedMapExtensionMethods[K, V](
    private val self: collection.SortedMap[K, V]
) extends AnyVal {

  def minAfter(key: K): Option[(K, V)] = self.from(key).headOption

  def maxBefore(key: K): Option[(K, V)] = self.until(key).lastOption
}

class SortedSetExtensionMethods[A](
    private val self: collection.SortedSet[A]
) extends AnyVal {

  def minAfter(key: A): Option[A] = self.from(key).headOption

  def maxBefore(key: A): Option[A] = self.until(key).lastOption
}

class IteratorExtensionMethods[A](private val self: c.Iterator[A]) extends AnyVal {
  def sameElements[B >: A](that: c.TraversableOnce[B]): Boolean = {
    self.sameElements(that.iterator)
  }
  def nextOption(): Option[A] = {
    if (self.hasNext) Some(self.next()) else None
  }
  def concat[B >: A](that: c.TraversableOnce[B]): c.TraversableOnce[B] = self ++ that
  def tapEach[U](f: A => U): c.Iterator[A] = self.map(a => { f(a); a })
}

class TraversableOnceExtensionMethods[A](private val self: c.TraversableOnce[A]) extends AnyVal {
  def iterator: Iterator[A] = self.toIterator

  def minOption[B >: A](implicit ord: Ordering[B]): Option[A] = {
    if (self.isEmpty)
      None
    else
      Some(self.min(ord))
  }

  def maxOption[B >: A](implicit ord: Ordering[B]): Option[A] = {
    if (self.isEmpty)
      None
    else
      Some(self.max(ord))
  }

  def minByOption[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] = {
    if (self.isEmpty)
      None
    else
      Some(self.minBy(f)(cmp))
  }

  def maxByOption[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] = {
    if (self.isEmpty)
      None
    else
      Some(self.maxBy(f)(cmp))
  }
}

class TraversableExtensionMethods[A](private val self: c.Traversable[A]) extends AnyVal {
  def iterableFactory: GenericCompanion[Traversable] = self.companion

  def sizeCompare(otherSize: Int): Int = SizeCompareImpl.sizeCompareInt(self)(otherSize)
  def sizeIs: SizeCompareOps = new SizeCompareOps(self)
  def sizeCompare(that: c.Traversable[_]): Int = SizeCompareImpl.sizeCompareColl(self)(that)

}

class SeqExtensionMethods[A](private val self: c.Seq[A]) extends AnyVal {
  def lengthIs: SizeCompareOps = new SizeCompareOps(self)
}

class SizeCompareOps private[compat] (private val it: c.Traversable[_]) extends AnyVal {
  import SizeCompareImpl._

  /** Tests if the size of the collection is less than some value. */
  @inline def <(size: Int): Boolean = sizeCompareInt(it)(size) < 0

  /** Tests if the size of the collection is less than or equal to some value. */
  @inline def <=(size: Int): Boolean = sizeCompareInt(it)(size) <= 0

  /** Tests if the size of the collection is equal to some value. */
  @inline def ==(size: Int): Boolean = sizeCompareInt(it)(size) == 0

  /** Tests if the size of the collection is not equal to some value. */
  @inline def !=(size: Int): Boolean = sizeCompareInt(it)(size) != 0

  /** Tests if the size of the collection is greater than or equal to some value. */
  @inline def >=(size: Int): Boolean = sizeCompareInt(it)(size) >= 0

  /** Tests if the size of the collection is greater than some value. */
  @inline def >(size: Int): Boolean = sizeCompareInt(it)(size) > 0
}

private object SizeCompareImpl {
  def sizeCompareInt(self: c.Traversable[_])(otherSize: Int): Int =
    self match {
      case self: c.SeqLike[_, _] => self.lengthCompare(otherSize)
      case _ =>
        if (otherSize < 0) 1
        else {
          var i = 0
          val it = self.toIterator
          while (it.hasNext) {
            if (i == otherSize) return 1
            it.next()
            i += 1
          }
          i - otherSize
        }
    }

  // `IndexedSeq` is the only thing that we can safely say has a known size
  def sizeCompareColl(self: c.Traversable[_])(that: c.Traversable[_]): Int =
    that match {
      case that: c.IndexedSeq[_] => sizeCompareInt(self)(that.length)
      case _ =>
        self match {
          case self: c.IndexedSeq[_] =>
            val res = sizeCompareInt(that)(self.length)
            // can't just invert the result, because `-Int.MinValue == Int.MinValue`
            if (res == Int.MinValue) 1 else -res
          case _ =>
            val thisIt = self.toIterator
            val thatIt = that.toIterator
            while (thisIt.hasNext && thatIt.hasNext) {
              thisIt.next()
              thatIt.next()
            }
            java.lang.Boolean.compare(thisIt.hasNext, thatIt.hasNext)
        }
    }
}

class TraversableLikeExtensionMethods[A, Repr](private val self: c.GenTraversableLike[A, Repr])
    extends AnyVal {
  def tapEach[U](f: A => U)(implicit bf: CanBuildFrom[Repr, A, Repr]): Repr =
    self.map(a => { f(a); a })

  def partitionMap[A1, A2, That, Repr1, Repr2](f: A => Either[A1, A2])(
      implicit bf1: CanBuildFrom[Repr, A1, Repr1],
      bf2: CanBuildFrom[Repr, A2, Repr2]
  ): (Repr1, Repr2) = {
    val l = bf1()
    val r = bf2()
    self.foreach { x =>
      f(x) match {
        case Left(x1) => l += x1
        case Right(x2) => r += x2
      }
    }
    (l.result(), r.result())
  }

  def groupMap[K, B, That](key: A => K)(f: A => B)(
      implicit bf: CanBuildFrom[Repr, B, That]): Map[K, That] = {
    val map = m.Map.empty[K, m.Builder[B, That]]
    for (elem <- self) {
      val k = key(elem)
      val bldr = map.getOrElseUpdate(k, bf(self.repr))
      bldr += f(elem)
    }
    val res = Map.newBuilder[K, That]
    for ((k, bldr) <- map) res += ((k, bldr.result()))
    res.result()
  }

  def groupMapReduce[K, B](key: A => K)(f: A => B)(reduce: (B, B) => B): Map[K, B] = {
    val map = m.Map.empty[K, B]
    for (elem <- self) {
      val k = key(elem)
      val v = map.get(k) match {
        case Some(b) => reduce(b, f(elem))
        case None => f(elem)
      }
      map.put(k, v)
    }
    map.toMap
  }

  def distinctBy[B, That](f: A => B)(implicit cbf: CanBuildFrom[Repr, A, That]): That = {
    val builder = cbf()
    val keys = collection.mutable.Set.empty[B]
    for (element <- self) {
      val key = f(element)
      if (!keys.contains(key)) {
        builder += element
        keys += key
      }
    }
    builder.result()
  }
}

class TrulyTraversableLikeExtensionMethods[El1, Repr1](
    private val self: TraversableLike[El1, Repr1])
    extends AnyVal {

  def lazyZip[El2, Repr2, T2](t2: T2)(
      implicit w2: T2 => IterableLike[El2, Repr2]
  ): Tuple2Zipped[El1, Repr1, El2, Repr2] = new Tuple2Zipped((self, t2))
}

class Tuple2ZippedExtensionMethods[El1, Repr1, El2, Repr2](
    private val self: Tuple2Zipped[El1, Repr1, El2, Repr2]) {

  def lazyZip[El3, Repr3, T3](t3: T3)(implicit
      w3: T3 => IterableLike[El3, Repr3]): Tuple3Zipped[El1, Repr1, El2, Repr2, El3, Repr3] =
    new Tuple3Zipped((self.colls._1, self.colls._2, t3))
}

class MapExtensionMethods[K, V](private val self: scala.collection.Map[K, V]) extends AnyVal {

  def foreachEntry[U](f: (K, V) => U): Unit = {
    self.foreach { case (k, v) => f(k, v) }
  }

}

class ImmutableMapExtensionMethods[K, V](private val self: scala.collection.immutable.Map[K, V])
    extends AnyVal {

  def updatedWith[V1 >: V](key: K)(remappingFunction: (Option[V]) => Option[V1]): Map[K, V1] =
    remappingFunction(self.get(key)) match {
      case Some(v) => self.updated(key, v)
      case None => self - key
    }
}

class MutableMapExtensionMethods[K, V](private val self: scala.collection.mutable.Map[K, V])
    extends AnyVal {

  def updateWith(key: K)(remappingFunction: (Option[V]) => Option[V]): Option[V] = {
    val updatedEntry = remappingFunction(self.get(key))
    updatedEntry match {
      case Some(v) => self.update(key, v)
      case None => self.remove(key)
    }
    updatedEntry
  }
}

class MapViewExtensionMethods[K, V, C <: scala.collection.Map[K, V]](
    private val self: IterableView[(K, V), C])
    extends AnyVal {

  def mapValues[W, That](f: V => W)(
      implicit bf: CanBuildFrom[IterableView[(K, V), C], (K, W), That]): That =
    self.map[(K, W), That] { case (k, v) => (k, f(v)) }

  // TODO: Replace the current implementation of `mapValues` with this
  //       after major version bump when bincompat can be broken.
  //       At the same time, remove `canBuildFromIterableViewMapLike`
  /*
  def mapValues[W](f: V => W): IterableView[(K, W), C] =
    // the implementation of `self.map` also casts the result
    self.map({ case (k, v) => (k, f(v)) }).asInstanceOf[IterableView[(K, W), C]]
   */

  def filterKeys(p: K => Boolean): IterableView[(K, V), C] =
    self.filter { case (k, _) => p(k) }
}

class ImmutableQueueExtensionMethods[A](private val self: i.Queue[A]) extends AnyVal {
  def enqueueAll[B >: A](iter: c.Iterable[B]): i.Queue[B] =
    self.enqueue(iter.to[i.Iterable])
}

class MutableQueueExtensionMethods[Element](private val self: m.Queue[Element]) extends AnyVal {
  def enqueueAll(iter: c.Iterable[Element]): Unit =
    self.enqueue(iter.toIndexedSeq: _*)
}

class OptionCompanionExtensionMethods(private val fact: Option.type) extends AnyVal {
  def when[A](cond: Boolean)(a: => A): Option[A] = if (cond) Some(a) else None

  @inline def unless[A](cond: Boolean)(a: => A): Option[A] = when(!cond)(a)
}

class GenericCompanionExtensionMethods[CC[X] <: GenTraversable[X]](
    private val companion: GenericCompanion[CC]) extends AnyVal {
  def unfold[A, S](init: S)(f: S => Option[(A, S)])(
      implicit cbf: CanBuildFrom[CC[A], A, CC[A]]
  ): CC[A] = {
    val builder = cbf()

    @tailrec
    def loop(s1: S): Unit = {
      f(s1) match {
        case Some((a, s2)) =>
          builder += a
          loop(s2)
        case None =>
      }
    }

    loop(init)
    builder.result()
  }
}
