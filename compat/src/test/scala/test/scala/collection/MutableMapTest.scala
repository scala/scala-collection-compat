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
import scala.collection.{mutable => m}

class MutableMapTest {

  @Test
  def updateWith: Unit = {
    val map  = m.Map("a" -> 1, "c" -> 3)
    map.updateWith("b"){
      case None => Some(2)
      case Some(x) => Some(-1)
    }
    map.updateWith("c"){
      case None => Some(-1)
      case Some(_) => None
    }
    assertEquals(map.get("b"), Some(2))
    assertFalse(map.contains("c"))
  }

}
