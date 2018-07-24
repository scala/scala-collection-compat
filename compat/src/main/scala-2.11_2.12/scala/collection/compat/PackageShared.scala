package scala.collection.compat

import scala.collection.generic._
import scala.reflect.ClassTag
import scala.collection.{MapLike, GenTraversable, BitSet}
import scala.collection.{immutable => i, mutable => m}
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
  type Factory[-A, +C] <: CanBuildFrom[Nothing, A, C] // Ideally, this would be an opaque type

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

  implicit def fromCanBuildFrom[A, C](implicit cbf: CanBuildFrom[Nothing, A, C]): Factory[A, C] =
    cbf.asInstanceOf[Factory[A, C]]

  implicit def fromCanBuildFromConversion[X, A, C](x: X)(implicit toCanBuildFrom: X => CanBuildFrom[Nothing, A, C]): Factory[A, C] =
    fromCanBuildFrom(toCanBuildFrom(x))

  implicit def genericCompanionToCBF[A, CC[X] <: GenTraversable[X]](
      fact: GenericCompanion[CC]): CanBuildFrom[Any, A, CC[A]] =
    simpleCBF(fact.newBuilder[A])

  implicit def sortedSetCompanionToCBF[
      A: Ordering,
      CC[X] <: c.SortedSet[X] with c.SortedSetLike[X, CC[X]]](
      fact: SortedSetFactory[CC]): CanBuildFrom[Any, A, CC[A]] =
    simpleCBF(fact.newBuilder[A])

  implicit def arrayCompanionToCBF[A: ClassTag](
      fact: Array.type): CanBuildFrom[Any, A, Array[A]] =
    simpleCBF(Array.newBuilder[A])

  implicit def mapFactoryToCBF[
      K,
      V,
      CC[A, B] <: Map[A, B] with MapLike[A, B, CC[A, B]]](
      fact: MapFactory[CC]): CanBuildFrom[Any, (K, V), CC[K, V]] =
    simpleCBF(fact.newBuilder[K, V])

  implicit def sortedMapFactoryToCBF[
      K: Ordering,
      V,
      CC[A, B] <: c.SortedMap[A, B] with c.SortedMapLike[A, B, CC[A, B]]](
      fact: SortedMapFactory[CC]): CanBuildFrom[Any, (K, V), CC[K, V]] =
    simpleCBF(fact.newBuilder[K, V])

  implicit def bitSetFactoryToCBF(
      fact: BitSetFactory[BitSet]): CanBuildFrom[Any, Int, BitSet] =
    simpleCBF(fact.newBuilder)

  implicit def immutableBitSetFactoryToCBF(fact: BitSetFactory[i.BitSet])
    : CanBuildFrom[Any, Int, ImmutableBitSetCC[Int]] =
    simpleCBF(fact.newBuilder)

  implicit def mutableBitSetFactoryToCBF(fact: BitSetFactory[m.BitSet])
    : CanBuildFrom[Any, Int, MutableBitSetCC[Int]] =
    simpleCBF(fact.newBuilder)

  implicit class IterableFactoryExtensionMethods[CC[X] <: GenTraversable[X]](
      private val fact: GenericCompanion[CC]) {
    def from[A](source: TraversableOnce[A]): CC[A] =
      fact.apply(source.toSeq: _*)
  }

  implicit class MapFactoryExtensionMethods[
      CC[A, B] <: Map[A, B] with MapLike[A, B, CC[A, B]]](
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

  private[compat] def build[T, CC](builder: m.Builder[T, CC],
                                   source: TraversableOnce[T]): CC = {
    builder ++= source
    builder.result()
  }

  implicit def toImmutableSortedMapExtensions(fact: i.SortedMap.type): ImmutableSortedMapExtensions =
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

  implicit def toSortedExtensionMethods[K, V <: Sorted[K, V]](fact: Sorted[K, V]): SortedExtensionMethods[K, V] =
    new SortedExtensionMethods[K, V](fact)

  implicit def toIteratorExtensionMethods[A](self: Iterator[A]): IteratorExtensionMethods[A] =
    new IteratorExtensionMethods[A](self)

  implicit def toTraversableOnceExtensionMethods[A](self: TraversableOnce[A]): TraversableOnceExtensionMethods[A] =
    new TraversableOnceExtensionMethods[A](self)

  // This really belongs into scala.collection but there's already a package object
  // in scala-library so we can't add to it
  type IterableOnce[+X] = c.TraversableOnce[X]
  val IterableOnce = c.TraversableOnce
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

class IteratorExtensionMethods[A](private val self: c.Iterator[A]) extends AnyVal {
  def sameElements[B >: A](that: c.TraversableOnce[B]): Boolean = {
    self.sameElements(that.iterator)
  }
  def concat[B >: A](that: c.TraversableOnce[B]): c.TraversableOnce[B] = self ++ that
}

class TraversableOnceExtensionMethods[A](private val self: c.TraversableOnce[A]) extends AnyVal {
  def iterator: Iterator[A] = self.toIterator
}
