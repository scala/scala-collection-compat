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

package test.scala.collection

import org.junit.Assert._
import org.junit.Test

import scala.collection.compat._
import scala.collection.immutable.Queue

class ImmutableQueueTest {

  @Test
  def testEnqueueAll: Unit = {
    val q                = Queue(1, 2)
    val i: Iterable[Int] = List(3, 4)
    val eq               = q.enqueueAll(i)
    assertEquals(Queue(1, 2, 3, 4), eq)
  }
}
