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
import scala.collection.{immutable => i, mutable => m}

class MapTest {

  @Test
  def foreachEntry: Unit = {
    val map  = Map("a" -> 1, "b" -> 2, "c" -> 3)
    var copy = Map.empty[String, Int]
    map.foreachEntry((k, v) => copy += k -> v)
    assertEquals(map, copy)
  }

  @Test
  def mutableUpdateWith: Unit = {
    val map = m.Map("a" -> 1, "c" -> 3)
    map.updateWith("b") {
      case None    => Some(2)
      case Some(_) => Some(-1)
    }
    map.updateWith("c") {
      case None    => Some(-1)
      case Some(_) => None
    }
    // unmodified entries are preserved
    assertEquals(map.get("a"), Some(1))
    // updateWith can add entries
    assertEquals(map.get("b"), Some(2))
    // updateWith can remove entries
    assertFalse(map.contains("c"))
  }

  @Test
  def immutableUpdatedWith: Unit = {
    val map = i.Map("a" -> 1, "c" -> 3)
    val bAdded = map.updatedWith("b") {
      case None    => Some(2)
      case Some(_) => Some(-1)
    }
    val cRemoved = map.updatedWith("c") {
      case None    => Some(-1)
      case Some(_) => None
    }
    // unmodified entries are preserved
    assertEquals(map.get("a"), bAdded.get("a"))
    assertEquals(map.get("a"), cRemoved.get("a"))
    // updatedWith can add entries
    assertEquals(bAdded.get("b"), Some(2))
    // updatedWith can remove entries
    assertFalse(cRemoved.contains("c"))
  }

}
