package scala.collection

import scala.collection.generic._
import scala.collection.mutable.Builder
import scala.reflect.ClassTag

package object compat_impl {
  def simpleCBF[A, C](f: => Builder[A, C]): CanBuildFrom[Nothing, A, C] = new CanBuildFrom[Nothing, A, C] {
    def apply(from: Nothing): Builder[A, C] = apply()
    def apply(): Builder[A, C] = f
  }

  type ImmutableBitSetCC[X] = ({ type L[_] = immutable.BitSet })#L[X]
  type MutableBitSetCC[X] = ({ type L[_] = mutable.BitSet })#L[X]
}
