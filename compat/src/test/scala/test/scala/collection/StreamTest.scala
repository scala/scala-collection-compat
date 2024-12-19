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

package test.scala.collection

import org.junit.Test

import scala.collection.compat._

class StreamTest {

  @Test
  def lazyAppendedAll(): Unit = {
    val s = 1 #:: 2 #:: 3 #:: Stream.Empty
    s.lazyAppendedAll(List(4, 5, 6))
  }

}
