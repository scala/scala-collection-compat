package scala.collection

import scala.collection.generic._
import scala.collection.mutable.Builder
import scala.reflect.ClassTag

/** The collection compatibility API */
package object compat {
  import scala.collection.compat_impl._

  implicit def genericCompanionToCBF[A, CC[X] <: GenTraversable[X]](fact: GenericCompanion[CC]): CanBuildFrom[Any, A, CC[A]] =
    simpleCBF(fact.newBuilder[A])

  implicit def sortedSetCompanionToCBF[A : Ordering, CC[X] <: SortedSet[X] with SortedSetLike[X, CC[X]]](fact: SortedSetFactory[CC]): CanBuildFrom[Any, A, CC[A]] =
    simpleCBF(fact.newBuilder[A])

  implicit def arrayCompanionToCBF[A : ClassTag](fact: Array.type): CanBuildFrom[Any, A, Array[A]] =
    simpleCBF(Array.newBuilder[A])

  implicit def mapFactoryToCBF[K, V, CC[A, B] <: Map[A, B] with MapLike[A, B, CC[A, B]]](fact: MapFactory[CC]): CanBuildFrom[Any, (K, V), CC[K, V]] =
    simpleCBF(fact.newBuilder[K, V])

  implicit def sortedMapFactoryToCBF[K : Ordering, V, CC[A, B] <: SortedMap[A, B] with SortedMapLike[A, B, CC[A, B]]](fact: SortedMapFactory[CC]): CanBuildFrom[Any, (K, V), CC[K, V]] =
    simpleCBF(fact.newBuilder[K, V])

  implicit def bitSetFactoryToCBF(fact: BitSetFactory[BitSet]): CanBuildFrom[Any, Int, BitSet] =
    simpleCBF(fact.newBuilder)

  implicit def immutableBitSetFactoryToCBF(fact: BitSetFactory[immutable.BitSet]): CanBuildFrom[Any, Int, ImmutableBitSetCC[Int]] =
    simpleCBF(fact.newBuilder)

  implicit def mutableBitSetFactoryToCBF(fact: BitSetFactory[mutable.BitSet]): CanBuildFrom[Any, Int, MutableBitSetCC[Int]] =
    simpleCBF(fact.newBuilder)

  implicit class IterableFactoryExtensionMethods[CC[X] <: GenTraversable[X]](private val fact: GenericCompanion[CC]) {
    def from[A](source: TraversableOnce[A]): CC[A] = (fact.newBuilder[A] ++= source).result()
  }

  implicit class MapFactoryExtensionMethods[CC[A, B] <: Map[A, B] with MapLike[A, B, CC[A, B]]](private val fact: MapFactory[CC]) {
    def from[K, V](source: TraversableOnce[(K, V)]): CC[K, V] = (fact.newBuilder[K, V] ++= source).result()
  }

  implicit class SortedMapFactoryExtensionMethods[CC[A, B] <: SortedMap[A, B] with SortedMapLike[A, B, CC[A, B]]](private val fact: SortedMapFactory[CC]) extends AnyVal {
    def from[K : Ordering, V](source: TraversableOnce[(K, V)]): CC[K, V] = (fact.newBuilder[K, V] ++= source).result()
  }

  implicit class BitSetFactoryExtensionMethods[C <: scala.collection.BitSet with scala.collection.BitSetLike[C]](private val fact: BitSetFactory[C]) {
    def fromSpecific(source: TraversableOnce[Int]): C = (fact.newBuilder ++= source).result()
  }

  implicit class SortedSetFactoryExtensionMethods[CC[X] <: SortedSet[X] with SortedSetLike[X, CC[X]]](private val fact: SortedSetFactory[CC]) extends AnyVal {
    def from[A : Ordering](source: TraversableOnce[A]): CC[A] = (fact.newBuilder[A] ++= source).result()
  }

  implicit class StreamExtensionMethods[A](private val stream: Stream[A]) extends AnyVal {
    def lazyAppendAll(as: => TraversableOnce[A]): Stream[A] = stream.append(as)
  }

  implicit class IterableExtensionMethods[A](private val as: IterableLike[A, _]) extends AnyVal {
    def iterator(): Iterator[A] = as.iterator
  }

  implicit class BuilderExtensionMethods[Elem, To](val builder: Builder[Elem, To]) extends AnyVal {
    def apply(): builder.type = builder
  }

  // This really belongs into scala.collection but there's already a package object in scala-library so we can't add to it
  type IterableOnce[+X] = TraversableOnce[X]
}
