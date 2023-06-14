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

package scala.util.matching.compat

import org.junit.Assert._
import org.junit.Test

class RegexOpsTest {

  @Test
  def testMatches(): Unit = {
    assertTrue(".*hello.*".r.matches("hey hello"))
    assertFalse(".*hello.*".r.matches("hey hop"))
  }

}
