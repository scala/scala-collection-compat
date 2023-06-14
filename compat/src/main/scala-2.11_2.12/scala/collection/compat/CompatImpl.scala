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

package scala.collection.compat

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}

/* builder optimized for a single ++= call, which returns identity on result if possible
 * and defers to the underlying builder if not.
 */
private abstract class PreservingBuilder[A, C <: TraversableOnce[A]] extends m.Builder[A, C] {
  val that: m.Builder[A, C]
  val ct: ClassTag[C]

  // invariant: ruined => (collection == null)
  var collection: C = null.asInstanceOf[C]
  var ruined        = false

  private[this] def ruin(): Unit = {
    if (collection != null) that ++= collection
    collection = null.asInstanceOf[C]
    ruined = true
  }

  override def ++=(elems: TraversableOnce[A]): this.type = {
    (if (collection == null && !ruined) ct.unapply(elems) else None) match {
      case Some(c) => collection = c
      case _ =>
        ruin()
        that ++= elems
    }
    this
  }

  def +=(elem: A): this.type = {
    ruin()
    that += elem
    this
  }

  def clear(): Unit = {
    collection = null.asInstanceOf[C]
    if (ruined) {
      that.clear()
      ruined = false
    }
  }

  def result(): C = if (collection == null) that.result() else collection
}

private final class IdentityPreservingBuilder[A, CC[X] <: TraversableOnce[X]](
    val that: m.Builder[A, CC[A]])(implicit val ct: ClassTag[CC[A]])
    extends PreservingBuilder[A, CC[A]]

private final class IdentityPreservingBitSetBuilder[C <: c.BitSet](val that: m.Builder[Int, C])(
    implicit val ct: ClassTag[C])
    extends PreservingBuilder[Int, C]

private final class IdentityPreservingMapBuilder[
    K,
    V,
    CC[X, Y] <: c.Map[X, Y] with c.MapLike[X, Y, CC[X, Y]]](val that: m.Builder[(K, V), CC[K, V]])(
    implicit val ct: ClassTag[CC[K, V]])
    extends PreservingBuilder[(K, V), CC[K, V]]

private[compat] object CompatImpl {
  def simpleCBF[A, C](f: => m.Builder[A, C]): CanBuildFrom[Any, A, C] =
    new CanBuildFrom[Any, A, C] {
      def apply(from: Any): m.Builder[A, C] = apply()
      def apply(): m.Builder[A, C]          = f
    }

  type ImmutableBitSetCC[X] = ({ type L[_] = i.BitSet })#L[X]
  type MutableBitSetCC[X]   = ({ type L[_] = m.BitSet })#L[X]
}
