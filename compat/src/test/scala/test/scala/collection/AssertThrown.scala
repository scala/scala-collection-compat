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

import org.junit.Assert._

import scala.reflect.ClassTag
import scala.util.control.NonFatal

class AssertThrown {

  // next two methods copied from AssertUtil in scala/scala repo

  /** Check that throwable T (or a subclass) was thrown during evaluation of `body`,
   *  and that its message satisfies the `checkMessage` predicate.
   *  Any other exception is propagated.
   */
  def assertThrows[T <: Throwable: ClassTag](body: => Any,
                                             checkMessage: String => Boolean = s => true): Unit = {
    assertThrown[T](t => checkMessage(t.getMessage))(body)
  }

  def assertThrown[T <: Throwable: ClassTag](checker: T => Boolean)(body: => Any): Unit =
    try {
      body
      fail("Expression did not throw!")
    } catch {
      case e: T if checker(e) => ()
      case failed: T =>
        val ae = new AssertionError(s"Exception failed check: $failed")
        ae.addSuppressed(failed)
        throw ae
      case NonFatal(other) =>
        val ae = new AssertionError(
          s"Wrong exception: expected ${implicitly[ClassTag[T]]} but was ${other.getClass.getName}")
        ae.addSuppressed(other)
        throw ae
    }
}
