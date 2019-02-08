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
