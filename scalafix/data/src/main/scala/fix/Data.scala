package fix

import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}

case class NoOrdering(v: Int)

object Data {
  val ordered: Int => Int                           = x => x
  val orderedMap: ((Int, Int)) => ((Int, Int))      = x => x
  val ordered2: Int => List[Int]                    = x => List(x)
  val orderedMap2: ((Int, Int)) => List[(Int, Int)] = x => List(x)

  val unordered: Int => NoOrdering                    = x => NoOrdering(x)
  val unorderedMap: ((Int, Int)) => (NoOrdering, Int) = { case (k, v) => (NoOrdering(k), v) }
  val unordered2: Int => List[NoOrdering]             = x => List(NoOrdering(x))
  val unorderedMap2: ((Int, Int)) => List[(NoOrdering, Int)] = {
    case (k, v) => List((NoOrdering(k), v))
  }

  val cSet: c.Set[Int]      = c.Set(1)
  val cMap: c.Map[Int, Int] = c.Map(1 -> 1)

  val cBitSet: c.BitSet                 = c.BitSet(1)
  val cSortedMap: c.SortedMap[Int, Int] = c.SortedMap(1 -> 1)
  val cSortedSet: c.SortedSet[Int]      = c.SortedSet(1)

  val iBitSet: i.BitSet                 = i.BitSet(1)
  val iSortedMap: i.SortedMap[Int, Int] = i.SortedMap(1 -> 1)
  val iSortedSet: i.SortedSet[Int]      = i.SortedSet(1)
  val iTreeMap: i.TreeMap[Int, Int]     = i.TreeMap(1 -> 1)
  val iTreeSet: i.TreeSet[Int]          = i.TreeSet(1)

  val mBitSet: m.BitSet            = m.BitSet(1)
  val mSortedSet: m.SortedSet[Int] = m.SortedSet(1)
  val mTreeSet: m.TreeSet[Int]     = m.TreeSet(1)
}
