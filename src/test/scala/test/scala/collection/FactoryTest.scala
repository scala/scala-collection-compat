package test.scala.collection

import utest._

import scala.collection.compat._
import scala.collection.{BitSet, immutable, mutable}

object FactoryTest extends TestSuite{

  implicitly[Factory[Char, String]]
  implicitly[Factory[Char, Array[Char]]]
  implicitly[Factory[Int, collection.BitSet]]
  implicitly[Factory[Int, mutable.BitSet]]
  implicitly[Factory[Int, immutable.BitSet]]

  BitSet: Factory[Int, BitSet]
  Iterable: Factory[Int, Iterable[Int]]
  immutable.TreeSet: Factory[Int, immutable.TreeSet[Int]]
  Map: Factory[(Int, String), Map[Int, String]]
  immutable.TreeMap: Factory[(Int, String), immutable.TreeMap[Int, String]]

  val tests = Tests{
    'streamFactoryPreservesLaziness - {
      val factory = implicitly[Factory[Int, Stream[Int]]]
      var counter = 0
      val source = Stream.continually { counter += 1; 1 }
      val result = factory.fromSpecific(source)
      assert(1 == counter) // One element has been evaluated because Stream is not lazy in its head
    }
  }
}
