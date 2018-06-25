package test.scala.collection

import utest._

import scala.collection.compat._
import scala.collection.immutable.BitSet

object CollectionTest extends TestSuite{

  val tests = Tests{
    'testTo - {
      val xs = List(1,2,3)
      val v = xs.to(Vector)
      val vT: Vector[Int] = v
      assert(Vector(1,2,3) == v)

      val a = xs.to(Array)
      val aT: Array[Int] = a
      assert(Vector(1,2,3) == a.toVector)

      val b = xs.to(BitSet) // we can fake a type constructor for the 2 standard BitSet types
      val bT: BitSet = b
      assert(BitSet(1,2,3) == b)

      val ys = List(1 -> "a", 2 -> "b")
      val m = ys.to(Map)
      // Not possible - `to` returns a Col[A] so this is only typed as an Iterable[(Int, String)]
      //val mT: Map[Int, String] = m
      assert(Map(1 -> "a", 2 -> "b") == m)
      assert(m.isInstanceOf[Map[_, _]])
    }

    'testFrom- {
      val xs = List(1,2,3)
      val v = Vector.from(xs)
      val vT: Vector[Int] = v
      assert(Vector(1,2,3) == v)

      val b = BitSet.fromSpecific(xs)
      val bT: BitSet = b
      assert(BitSet(1,2,3) == b)

      val ys = List(1 -> "a", 2 -> "b")
      val m = Map.from(ys)
      val mT: Map[Int, String] = m
      assert(Map(1 -> "a", 2 -> "b") == m)
    }

    'testIterator - {
      val xs = Iterator(1, 2, 3).iterator.toList
      assert(List(1, 2, 3) == xs)
    }
  }
}
