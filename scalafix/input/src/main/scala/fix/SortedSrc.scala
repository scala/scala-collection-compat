/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}

object WeekDay extends Enumeration {
  type WeekDay = Value
  val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
}

object SortedSrc {
  WeekDay.values.from(WeekDay.Mon)
  c.BitSet(1).from(0)
  i.BitSet(1).from(0)
  m.BitSet(1).from(0)
  new i.BitSet.BitSet1(0L).from(0)
  new i.BitSet.BitSet2(0L, 1L).from(0)
  new i.BitSet.BitSetN(Array(0L)).from(0)
  c.SortedMap(1 -> 1).from(0)
  i.SortedMap(1 -> 1).from(0)
  i.SortedSet(1).from(0)
  m.SortedSet(1).from(0)
  i.TreeMap(1 -> 1).from(0)
  m.TreeSet(1).from(0)

  WeekDay.values.to(WeekDay.Mon)
  c.BitSet(1).to(0)
  i.BitSet(1).to(0)
  m.BitSet(1).to(0)
  new i.BitSet.BitSet1(0L).to(0)
  new i.BitSet.BitSet2(0L, 1L).to(0)
  new i.BitSet.BitSetN(Array(0L)).to(0)
  c.SortedMap(1 -> 1).to(0)
  i.SortedMap(1 -> 1).to(0)
  i.SortedSet(1).to(0)
  m.SortedSet(1).to(0)
  i.TreeMap(1 -> 1).to(0)
  m.TreeSet(1).to(0)

  WeekDay.values.until(WeekDay.Mon)
  c.BitSet(1).until(0)
  i.BitSet(1).until(0)
  m.BitSet(1).until(0)
  new i.BitSet.BitSet1(0L).until(0)
  new i.BitSet.BitSet2(0L, 1L).until(0)
  new i.BitSet.BitSetN(Array(0L)).until(0)
  c.SortedMap(1 -> 1).until(0)
  i.SortedMap(1 -> 1).until(0)
  i.SortedSet(1).until(0)
  m.SortedSet(1).until(0)
  i.TreeMap(1 -> 1).until(0)
  m.TreeSet(1).until(0)
}

