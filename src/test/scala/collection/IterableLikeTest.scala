package collection

import org.junit.Test

import scala.collection.compat._

class IterableLikeTest {

  @Test
  def iteratorTest(): Unit = {
    List(1, 2, 3).iterator()
  }

}
