package collection

import java.util

import org.junit.Test
import org.junit.Assert._

import scala.collection.immutable.BitSet
import scala.collection.compat._

class CollectionTest {
  @Test
  def testTo: Unit = {
    val xs = List(1,2,3)
    val v = xs.to(Vector)
    val vT: Vector[Int] = v
    assertEquals(Vector(1,2,3), v)

    val a = xs.to(Array)
    val aT: Array[Int] = a
    assertEquals(Vector(1,2,3), a.toVector)

    val b = xs.to(BitSet) // we can fake a type constructor for the 2 standard BitSet types
    val bT: BitSet = b
    assertEquals(BitSet(1,2,3), b)

    val ys = List(1 -> "a", 2 -> "b")
    val m = ys.to(Map)
    // Not possible - `to` returns a Col[A] so this is only typed as an Iterable[(Int, String)]
    //val mT: Map[Int, String] = m
    assertEquals(Map(1 -> "a", 2 -> "b"), m)
    assertTrue(m.isInstanceOf[Map[_, _]])
  }

  @Test
  def testFrom: Unit = {
    val xs = List(1,2,3)
    val v = Vector.from(xs)
    val vT: Vector[Int] = v
    assertEquals(Vector(1,2,3), v)

    val b = BitSet.fromSpecific(xs)
    val bT: BitSet = b
    assertEquals(BitSet(1,2,3), b)

    val ys = List(1 -> "a", 2 -> "b")
    val m = Map.from(ys)
    val mT: Map[Int, String] = m
    assertEquals(Map(1 -> "a", 2 -> "b"), m)
  }
}
