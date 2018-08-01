package scala.collection.compat

import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}

private[compat] trait PackageShared0 {
  implicit def toCollectionBitSetExtensions(self: c.BitSet): CollectionBitSetExtensions =
    new CollectionBitSetExtensions(self)

  implicit def toCollectionSortedMapExtensions[K, V](self: c.SortedMap[K, V]): CollectionSortedMapExtensions[K, V] =
    new CollectionSortedMapExtensions[K, V](self)

  implicit def toCollectionSortedSetExtensions[A](self: c.SortedSet[A]): CollectionSortedSetExtensions[A] =
    new CollectionSortedSetExtensions[A](self)

  implicit def toImmutableBitSetExtensions(self: i.BitSet): ImmutableBitSetExtensions =
    new ImmutableBitSetExtensions(self)

  implicit def toImmutableSortedMapExtensions[K, V](self: i.SortedMap[K, V]): ImmutableSortedMapExtensions[K, V] =
    new ImmutableSortedMapExtensions[K, V](self)

  implicit def toImmutableSortedSetExtensions[A](self: i.SortedSet[A]): ImmutableSortedSetExtensions[A] =
    new ImmutableSortedSetExtensions[A](self)

  implicit def toImmutableTreeMapExtensions[K, V](self: i.TreeMap[K, V]): ImmutableTreeMapExtensions[K, V] =
    new ImmutableTreeMapExtensions[K, V](self)

  implicit def toImmutableTreeSetExtensions[A](self: i.TreeSet[A]): ImmutableTreeSetExtensions[A] =
    new ImmutableTreeSetExtensions[A](self)

  implicit def toMutableBitSetExtensions(self: m.BitSet): MutableBitSetExtensions =
    new MutableBitSetExtensions(self)

  implicit def toMutableSortedSetExtensions[A](self: m.SortedSet[A]): MutableSortedSetExtensions[A] =
    new MutableSortedSetExtensions[A](self)

  implicit def toMutableTreeSetExtensions[A](self: m.TreeSet[A]): MutableTreeSetExtensions[A] =
    new MutableTreeSetExtensions[A](self)
}

class CollectionBitSetExtensions(private val self: c.BitSet) extends AnyVal {
  def unsortedSpecific: c.Set[Int] = self
}

class CollectionSortedMapExtensions[K, V](private val self: c.SortedMap[K, V]) extends AnyVal {
  def unsortedSpecific: c.Map[K, V] = self
}

class CollectionSortedSetExtensions[A](private val self: c.SortedSet[A]) extends AnyVal {
  def unsortedSpecific: c.Set[A] = self
}

class ImmutableBitSetExtensions(private val self: i.BitSet) extends AnyVal {
  def unsortedSpecific: i.Set[Int] = self
}

class ImmutableSortedMapExtensions[K, V](private val self: i.SortedMap[K, V]) extends AnyVal {
  def unsortedSpecific: i.Map[K, V] = self
}

class ImmutableSortedSetExtensions[A](private val self: i.SortedSet[A]) extends AnyVal {
  def unsortedSpecific: i.Set[A] = self
}

class ImmutableTreeMapExtensions[K, V](private val self: i.TreeMap[K, V]) extends AnyVal {
  def unsortedSpecific: i.Map[K, V] = self
}

class ImmutableTreeSetExtensions[A](private val self: i.TreeSet[A]) extends AnyVal {
  def unsortedSpecific: i.Set[A] = self
}

class MutableBitSetExtensions(private val self: m.BitSet) extends AnyVal {
  def unsortedSpecific: m.Set[Int] = self
}

class MutableSortedSetExtensions[A](private val self: m.SortedSet[A]) extends AnyVal {
  def unsortedSpecific: m.Set[A] = self
}

class MutableTreeSetExtensions[A](private val self: m.TreeSet[A]) extends AnyVal {
  def unsortedSpecific: m.Set[A] = self
}