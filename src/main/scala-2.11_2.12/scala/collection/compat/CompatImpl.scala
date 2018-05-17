package scala.collection.compat

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.collection.{immutable => i, mutable => m}

private[compat] object CompatImpl {
  def simpleCBF[A, C](f: => Builder[A, C]): CanBuildFrom[Any, A, C] = new CanBuildFrom[Any, A, C] {
    def apply(from: Any): Builder[A, C] = apply()
    def apply(): Builder[A, C] = f
  }

  type ImmutableBitSetCC[X] = ({ type L[_] = i.BitSet })#L[X]
  type MutableBitSetCC[X] = ({ type L[_] = m.BitSet })#L[X]
}
