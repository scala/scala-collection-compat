package org.example

import scala.collection.generic._

object Lib {
  def id[A, C[X] <: Iterable[X]](x: C[A])(implicit cbf: CanBuildFrom[C[A], A, C[A]]) = {
    val builder = cbf()
    builder ++= x
    builder.result
  }

  def test1 =
    id(List(1, 2, 3))
}
