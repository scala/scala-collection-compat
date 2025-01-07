/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

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
