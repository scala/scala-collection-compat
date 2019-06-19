package scala.collection.compat

import scala.collection.mutable

trait AbstractFactory[-A, +C] extends Factory[A, C] {
  def fromSpecific(it: IterableOnce[A]): C
  def newBuilder: mutable.Builder[A, C]

  final def apply(from: Nothing): mutable.Builder[A, C] = newBuilder
  final def apply(): mutable.Builder[A, C] = newBuilder
}
