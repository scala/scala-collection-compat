


package fix

import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}
import scala.collection.compat._

object WeekDay extends Enumeration {
  type WeekDay = Value
  val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
}

object SortedSrc {
  WeekDay.values.rangeFrom(WeekDay.Mon)
  c.BitSet(1).rangeFrom(0)
  i.BitSet(1).rangeFrom(0)
  m.BitSet(1).rangeFrom(0)
  new i.BitSet.BitSet1(0L).rangeFrom(0)
  new i.BitSet.BitSet2(0L, 1L).rangeFrom(0)
  new i.BitSet.BitSetN(Array(0L)).rangeFrom(0)
  c.SortedMap(1 -> 1).rangeFrom(0)
  i.SortedMap(1 -> 1).rangeFrom(0)
  m.SortedMap(1 -> 1).rangeFrom(0)
  c.SortedSet(1).rangeFrom(0)
  i.SortedSet(1).rangeFrom(0)
  m.SortedSet(1).rangeFrom(0)
  i.TreeMap(1 -> 1).rangeFrom(0)
  m.TreeMap(1 -> 1).rangeFrom(0)
  i.TreeSet(1).rangeFrom(0)
  m.TreeSet(1).rangeFrom(0)

  WeekDay.values.rangeTo(WeekDay.Mon)
  c.BitSet(1).rangeTo(0)
  i.BitSet(1).rangeTo(0)
  m.BitSet(1).rangeTo(0)
  new i.BitSet.BitSet1(0L).rangeTo(0)
  new i.BitSet.BitSet2(0L, 1L).rangeTo(0)
  new i.BitSet.BitSetN(Array(0L)).rangeTo(0)
  c.SortedMap(1 -> 1).rangeTo(0)
  i.SortedMap(1 -> 1).rangeTo(0)
  m.SortedMap(1 -> 1).rangeTo(0)
  c.SortedSet(1).rangeTo(0)
  i.SortedSet(1).rangeTo(0)
  m.SortedSet(1).rangeTo(0)
  i.TreeMap(1 -> 1).rangeTo(0)
  m.TreeMap(1 -> 1).rangeTo(0)
  i.TreeSet(1).rangeTo(0)
  m.TreeSet(1).rangeTo(0)

  WeekDay.values.rangeUntil(WeekDay.Mon)
  c.BitSet(1).rangeUntil(0)
  i.BitSet(1).rangeUntil(0)
  m.BitSet(1).rangeUntil(0)
  new i.BitSet.BitSet1(0L).rangeUntil(0)
  new i.BitSet.BitSet2(0L, 1L).rangeUntil(0)
  new i.BitSet.BitSetN(Array(0L)).rangeUntil(0)
  c.SortedMap(1 -> 1).rangeUntil(0)
  i.SortedMap(1 -> 1).rangeUntil(0)
  m.SortedMap(1 -> 1).rangeUntil(0)
  c.SortedSet(1).rangeUntil(0)
  i.SortedSet(1).rangeUntil(0)
  m.SortedSet(1).rangeUntil(0)
  i.TreeMap(1 -> 1).rangeUntil(0)
  m.TreeMap(1 -> 1).rangeUntil(0)
  i.TreeSet(1).rangeUntil(0)
  m.TreeSet(1).rangeUntil(0)
}

