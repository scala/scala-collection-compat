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

import org.junit.Assert._
import org.junit.Test

import scala.collection.compat._
import scala.collection.{immutable => i, mutable => m}

class QueueTest {

  @Test
  def testImmutableEnqueueAll: Unit = {
    val q = i.Queue(1, 2)
    val iter: Iterable[Int] = List(3, 4)
    val eq = q.enqueueAll(iter)
    assertEquals(i.Queue(1, 2, 3, 4), eq)
  }

  @Test
  def testMutableEnqueueAll: Unit = {
    val q = m.Queue(1, 2)
    val lst = i.List(3, 4)
    q.enqueueAll(lst)
    assertEquals(i.Queue(1, 2, 3, 4), q)
  }
}
