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

package test.scala.util

import org.junit.Assert._
import org.junit.Test
import scala.collection.compat._
import scala.util.Random
import test.scala.collection.AssertThrown

class RandomTest extends AssertThrown {
  @Test
  def nextLong(): Unit = {
    val rand = new Random(12345)

    assertEquals(4896762128577075113L, rand.nextLong(Long.MaxValue))
    assertEquals(2005076556L, rand.nextLong(Int.MaxValue))
    assertEquals(0L, rand.nextLong(1L))

    assertThrows[IllegalArgumentException](rand.nextLong(0L))
    assertThrows[IllegalArgumentException](rand.nextLong(-2L))
    assertThrows[IllegalArgumentException](rand.nextLong(Long.MinValue))
  }
}
