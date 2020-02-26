package test.scala.collection

import org.junit.Assert.assertEquals
import org.junit.Test

import scala.collection.compat._

class LazyZipTest {

  private val ws      = List(1, 2, 3)
  private val xs      = List(1, 2, 3, 4, 5, 6)
  private val ys      = List("a", "b", "c", "d", "e", "f")
  private val zs      = List(true, false, true, false, true, false)
  private val zipped2 = ws lazyZip xs
  private val zipped3 = ws lazyZip xs lazyZip ys
  private val zipped4 = ws lazyZip xs lazyZip ys lazyZip zs
  private val map     = Map(1 -> "foo", 2 -> "bar")

  @Test
  def lazyZipTest(): Unit = {
    val res: List[(Int, Int)] = zipped2.map((a, b) => (a, b))
    assertEquals(List((1, 1), (2, 2), (3, 3)), res)
  }

  @Test
  def lazyZip3_map(): Unit = {
    val res: List[(Int, Int, String)] = zipped3.map((a: Int, b: Int, c: String) => (a, b, c))
    assertEquals(List((1, 1, "a"), (2, 2, "b"), (3, 3, "c")), res)
  }

  @Test
  def collectionValueIsNotEvaluated(): Unit = {
    val st = Stream.cons(1, throw new AssertionError("should not be evaluated"))
    ws.lazyZip(st)
  }

  @Test
  def zip3collectionValueIsNotEvaluated(): Unit = {
    val st = Stream.cons(1, throw new AssertionError("should not be evaluated"))
    ws.lazyZip(st).lazyZip(st)
  }

}
