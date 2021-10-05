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

package scala.util

import org.junit.Assert._
import org.junit.Test

//import scala.tools.reflect.ToolBoxError
//import scala.tools.testkit.RunTesting

class ChainingOpsTest {
  import scala.util.chaining._

  @Test
  def testAnyTap(): Unit = {
    var x: Int = 0
    val result = List(1, 2, 3)
      .tap(xs => x = xs.head)

    assertEquals(1, x)
    assertEquals(List(1, 2, 3), result)
  }

  @Test def testAnyValTap(): Unit = assertEquals(42.tap(x => x), 42)

  @Test
  def testAnyPipe(): Unit = {
    val times6 = (_: Int) * 6
    val result = (1 - 2 - 3)
      .pipe(times6)
      .pipe(scala.math.abs)

    assertEquals(24, result)
  }

//  @Test(expected = classOf[ToolBoxError])
//  def testNoSelf(): Unit =
//    runner.run("import scala.util.chaining._; Nil.self")
}
