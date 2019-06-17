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

class ViewTest {

  @Test
  def mapValues: Unit = {
    val m = Map("a" -> 1, "b" -> 2, "c" -> 3)
    val oldStyle = m.mapValues(x => x*x)
    val newStyle = m.view.mapValues(x => x*x)
    assertEquals(oldStyle.toMap, newStyle.toMap)
  }

}
