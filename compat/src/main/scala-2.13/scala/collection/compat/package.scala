package scala.collection

package object compat extends PackageShared0 {
  type Factory[-A, +C] = scala.collection.Factory[A, C]
  val Factory = scala.collection.Factory

  type BuildFrom[-From, -A, +C] = scala.collection.BuildFrom[From, A, C]
  val BuildFrom = scala.collection.BuildFrom

  type IterableOnce[+X] = scala.collection.IterableOnce[X]
  val IterableOnce = scala.collection.IterableOnce
}
