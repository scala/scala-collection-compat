package org.example

import scala.collection.generic._
import scala.collection.compat._

object Lib {
  def id[A, C[X] <: Iterable[X]](x: C[A])(implicit factory: Factory[A, C[A]]) = {
    val builder = factory.newBuilder
    builder ++= x
    builder.result
  }

  def test1 =
    id(List(1, 2, 3))
}
