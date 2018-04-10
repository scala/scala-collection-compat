package collection

import org.junit.{Assert, Test}

import scala.collection.{BitSet, Factory, Iterable, immutable, mutable}
import scala.collection.compat._

class FactoryTest {

  implicitly[Factory[Char, String]]
  implicitly[Factory[Char, Array[Char]]]
  implicitly[Factory[Int, BitSet]]
  implicitly[Factory[Int, mutable.BitSet]]
  implicitly[Factory[Int, immutable.BitSet]]

  BitSet: Factory[Int, BitSet]
  Iterable: Factory[Int, Iterable[Int]]
  immutable.TreeSet: Factory[Int, immutable.TreeSet[Int]]
  Map: Factory[(Int, String), Map[Int, String]]
  immutable.TreeMap: Factory[(Int, String), immutable.TreeMap[Int, String]]

  @Test
  def streamFactoryPreservesLaziness(): Unit = {
    val factory = implicitly[Factory[Int, Stream[Int]]]
    var counter = 0
    val source = Stream.continually { counter += 1; 1 }
    val result = factory.fromSpecific(source)
    Assert.assertEquals(1, counter) // One element has been evaluated because Stream is not lazy in its head
  }

}
