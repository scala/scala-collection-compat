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

package scala.util.control.compat

import org.junit.Test

class ControlThrowableTest {
  @Test
  def doesNotSuppress(): Unit = {
    val t = new ControlThrowable {}
    t.addSuppressed(new Exception)
    assert(t.getSuppressed.isEmpty)
  }

  @Test
  def doesNotHaveStackTrace(): Unit = {
    assert(new ControlThrowable {}.getStackTrace.isEmpty)
  }
}
