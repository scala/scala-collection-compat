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

import scala.collection.compat._
import org.junit.Test
import org.junit.Assert._

class OptionTest {

  private val value: String        = "example"
  private val some: Option[String] = Some(value)
  private val none: Option[String] = None

  @Test
  def testWhenTrue: Unit = {
    val option = Option.when(true)(value)
    assertEquals(option, some)
  }

  @Test
  def testWhenFalse: Unit = {
    val option = Option.when(false)(value)
    assertEquals(option, none)
  }

  @Test
  def testUnlessTrue: Unit = {
    val option = Option.unless(true)(value)
    assertEquals(option, none)
  }

  @Test
  def testUnlessFalse: Unit = {
    val option = Option.unless(false)(value)
    assertEquals(option, some)
  }
}
