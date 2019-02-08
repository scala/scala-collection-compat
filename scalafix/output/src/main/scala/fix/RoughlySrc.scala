package fix

import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}

object RoughlySrc {

  def id[T](x: T): T = x
  def f[T](x: T): Boolean = true
  def f2[T](x: T): Int = 1
  val props = new scala.sys.SystemProperties()
  val d = 1 -> 1
  val d2 = 1L -> 1
  val multi = new m.HashMap[Int, m.Set[Int]] with m.MultiMap[Int, Int]

  (Map(d) mapValues (id)).toMap          : i.Map[Int, Int]
  Map(d).mapValues(id).toMap           : i.Map[Int, Int]
  // i.SortedMap(d).mapValues(id): i.SortedMap[Int, Int]
  // m.SortedMap(d).mapValues(id): c.SortedMap[Int, Int]
  i.IntMap(d).mapValues(f2).toMap      : i.Map[Int, Int]
  i.LongMap(d2).mapValues(f2).toMap    : i.Map[Long, Int]
  i.Map(d).mapValues(id).toMap         : i.Map[Int, Int]
  m.LongMap(d2).mapValues(f2).toMap    : c.Map[Long, Int]
  m.Map(d).mapValues(id).toMap         : c.Map[Int, Int]
  m.OpenHashMap(d).mapValues(id).toMap : c.Map[Int, Int]
  multi.mapValues(f2).toMap            : c.Map[Int, Int]
  props.mapValues(id).toMap            : c.Map[String, String]

  (Map(d) filterKeys (f)).toMap          : i.Map[Int, Int]
  Map(d).filterKeys(f).toMap           : i.Map[Int, Int]
  // i.SortedMap(d).filterKeys(f): i.SortedMap[Int, Int]
  // m.SortedMap(d).filterKeys(f): c.SortedMap[Int, Int]
  i.IntMap(d).filterKeys(f).toMap    : i.Map[Int, Int]
  i.LongMap(d2).filterKeys(f).toMap  : i.Map[Long, Int]
  i.Map(d).filterKeys(f).toMap       : i.Map[Int, Int]
  m.LongMap(d2).filterKeys(f).toMap  : c.Map[Long, Int]
  m.Map(d).filterKeys(f).toMap       : c.Map[Int, Int]
  m.Map(d).filterKeys(f).toMap       : c.Map[Int, Int]
  multi.filterKeys(f).toMap          : c.Map[Int, m.Set[Int]]
  props.filterKeys(f).toMap          : c.Map[String, String]

}
