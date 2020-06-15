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

import org.junit.Test
import org.junit.Assert._

import scala.collection.compat._

class MapTest {

  @Test
  def foreachEntry: Unit = {
    val map  = Map("a" -> 1, "b" -> 2, "c" -> 3)
    var copy = Map.empty[String, Int]
    map.foreachEntry((k, v) => copy += k -> v)
    assertEquals(map, copy)
  }

}
