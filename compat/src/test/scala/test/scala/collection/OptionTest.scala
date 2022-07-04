package test.scala.collection

import scala.collection.compat._
import org.junit.Test
import org.junit.Assert._

class OptionTest {

  @Test
  def testWhenTrue: Unit = {
    val option = Option.when(true)("example")
    assertEquals(option, Some("example"))
  }

  @Test
  def testWhenFalse: Unit = {
    val option = Option.when(false)("example")
    assertEquals(option, None)
  }

  @Test
  def testUnlessTrue: Unit = {
    val option = Option.unless(true)("example")
    assertEquals(option, None)
  }

  @Test
  def testUnlessFalse: Unit = {
    val option = Option.unless(false)("example")
    assertEquals(option, Some("example"))
  }
}
