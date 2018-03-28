package collection

import scala.collection.{Factory, Iterable, BitSet, mutable, immutable}
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

}
