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

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.collection.{immutable => i, mutable => m}

/* builder optimized for a single ++= call, which returns identity on result if possible
 * and defers to the underlying builder if not.
 */
private final class IdentityPreservingSeqBuilder[A, C <: Seq[A]](that: Builder[A, C])
    extends Builder[A, Seq[A]] {
  var collection: Seq[A] = null
  var ruined = false

  final override def ++=(elems: TraversableOnce[A]): this.type =
      if(!ruined && collection == null && elems.isInstanceOf[Seq[_]]) {
        collection = elems.asInstanceOf[Seq[A]]
        this
      }
      else {
        ruined = true
        if (collection != null) that ++= collection
        that ++= elems
        collection = null
        this
      }

  final def +=(elem: A): this.type = {
    collection = null
    ruined = true
    that += elem
    this
  }
  final def clear(): Unit = {
    collection = null
    if (ruined) that.clear()
  }
  final def result(): Seq[A] = if(ruined) that.result() else if (collection eq null) Nil else collection
}

private[compat] object CompatImpl {
  def simpleCBF[A, C](f: => Builder[A, C]): CanBuildFrom[Any, A, C] = new CanBuildFrom[Any, A, C] {
    def apply(from: Any): Builder[A, C] = apply()
    def apply(): Builder[A, C]          = f
  }

  type ImmutableBitSetCC[X] = ({ type L[_] = i.BitSet })#L[X]
  type MutableBitSetCC[X]   = ({ type L[_] = m.BitSet })#L[X]
}
