package test.scala.collection

import org.junit.{Assert, Test}

import scala.collection.compat._

class ExtensionsTest {
  @Test
  def `TraversableOnce.knownSize`(): Unit = {
    Assert.assertEquals(-1, Iterator(1).knownSize)
    Assert.assertEquals(1, List(1).knownSize)
  }
}
