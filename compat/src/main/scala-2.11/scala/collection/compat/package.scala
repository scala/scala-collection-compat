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

  implicit def toTraversableLikeExtensionMethods[Repr](self: Repr)(
      implicit traversable: IsTraversableLike[Repr])
    : TraversableLikeExtensionMethods[traversable.A, Repr] =
    new TraversableLikeExtensionMethods[traversable.A, Repr](traversable.conversion(self))

  implicit def toSeqExtensionMethods[A](self: c.Seq[A]): SeqExtensionMethods[A] =
    new SeqExtensionMethods[A](self)
}
