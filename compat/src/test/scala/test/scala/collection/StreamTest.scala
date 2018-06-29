package test.scala.collection

import org.junit.Test

import scala.collection.compat._

class StreamTest {

  @Test
  def lazyAppendedAll(): Unit = {
    val s = 1 #:: 2 #:: 3 #:: Stream.Empty
    s.lazyAppendedAll(List(4, 5, 6))
  }

}
