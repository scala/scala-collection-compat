/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.collection

import scala.collection.generic.{CanBuildFrom, GenericOrderedCompanion, IsTraversableLike}
import scala.{collection => c}

package object compat extends compat.PackageShared {
  implicit def genericOrderedCompanionToCBF[A, CC[X] <: Traversable[X]](
      fact: GenericOrderedCompanion[CC])(
      implicit ordering: Ordering[A]): CanBuildFrom[Any, A, CC[A]] =
    CompatImpl.simpleCBF(fact.newBuilder[A])

  // CanBuildFrom instances for `IterableView[(K, V), Map[K, V]]` that preserve
  // the strict type of the view to be `Map` instead of `Iterable`
  // Instances produced by this method are used to chain `filterKeys` after `mapValues`
  implicit def canBuildFromIterableViewMapLike[K, V, L, W, CC[X, Y] <: Map[X, Y]]: CanBuildFrom[IterableView[(K, V), CC[K, V]], (L, W), IterableView[(L, W), CC[L, W]]] =
    new CanBuildFrom[IterableView[(K, V), CC[K, V]], (L, W), IterableView[(L, W), CC[L, W]]] {
      // `CanBuildFrom` parameters are used as type constraints, they are not used
      // at run-time, hence the dummy builder implementations
      def apply(from: IterableView[(K, V), CC[K, V]]) = new TraversableView.NoBuilder
      def apply() = new TraversableView.NoBuilder
    }

  implicit def toTraversableLikeExtensionMethods[Repr](self: Repr)(
      implicit traversable: IsTraversableLike[Repr])
    : TraversableLikeExtensionMethods[traversable.A, Repr] =
    new TraversableLikeExtensionMethods[traversable.A, Repr](traversable.conversion(self))

  implicit def toSeqExtensionMethods[A](self: c.Seq[A]): SeqExtensionMethods[A] =
    new SeqExtensionMethods[A](self)
}
