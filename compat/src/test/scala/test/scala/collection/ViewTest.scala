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
import org.junit.Assert._

import scala.collection.compat._

class ViewTest {

  @Test
  def mapValues: Unit = {
    val m = Map("a" -> 1, "b" -> 2, "c" -> 3)
    val oldStyle = m.mapValues(x => x * x)
    val newStyle = m.view.mapValues(x => x * x)
    assertEquals(oldStyle.toMap, newStyle.toMap)
  }

  @Test
  def filterKeys: Unit = {
    val m = Map("a" -> 1, "b" -> 2, "c" -> 3)
    val oldStyle = m.filterKeys(_ > "a")
    val newStyle = m.view.filterKeys(_ > "a")
    assertEquals(oldStyle.toMap, newStyle.toMap)
  }

  @Test
  def filterKeysMapValues(): Unit = {
    val m = Map("a" -> 1, "b" -> 2, "c" -> 3)
    assertEquals(Map(), m.view.filterKeys(_.length > 1).mapValues(_ + 1).toMap)
    assertEquals(Map(), m.view.mapValues(_ + 1).filterKeys(_.length > 1).toMap)
  }

}
