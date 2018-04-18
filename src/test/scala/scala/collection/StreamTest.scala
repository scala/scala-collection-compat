package scala.collection

import org.junit.Test

import scala.collection.compat._

class StreamTest {

  @Test
  def lazyAppendAll(): Unit = {
    val s = 1 #:: 2 #:: 3 #:: Stream.Empty
    s.lazyAppendAll(List(4, 5, 6))
  }

}
