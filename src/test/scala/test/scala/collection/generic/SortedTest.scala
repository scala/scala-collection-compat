package test.scala.collection.generic

import utest._

import scala.collection.compat._
import scala.collection.{immutable => i, mutable => m}

object SortedTest extends TestSuite{
  val tests = Tests{
    'immutableRangeOps - {
      val st = i.SortedSet(-1, 2, 4, 3)
      for (x <- List(0, 1, 4, -1, -4)) {
        val s1 = st.rangeFrom(x)
        assert((s1: i.SortedSet[Int]) == st.from(x))
        val s2 = st.rangeTo(x)
        assert((s2: i.SortedSet[Int]) == st.to(x))
        val s3 = st.rangeUntil(x)
        assert((s3: i.SortedSet[Int]) == st.until(x))
      }
      val mp = i.SortedMap("" -> 0, "ds" -> -3, "-??" -> 13, "Df" -> 33, "d!" -> -32)
      for (x <- List("", "-", "@", "aa", "D", "d")) {
        val m1 = mp.rangeFrom(x)
        assert((m1: i.SortedMap[String, Int]) == mp.from(x))
        val m2 = mp.rangeTo(x)
        assert((m2: i.SortedMap[String, Int]) == mp.to(x))
        val m3 = mp.rangeUntil(x)
        assert((m3: i.SortedMap[String, Int]) == mp.until(x))
      }
    }

    'mutableRangeOps - {
      val st = m.SortedSet(-1, 2, 4, 3)
      for (x <- List(0, 1, 4, -1, -4)) {
        val s1 = st.rangeFrom(x)
        assert((s1: m.SortedSet[Int]) == st.from(x))
        val s2 = st.rangeTo(x)
        assert((s2: m.SortedSet[Int]) == st.to(x))
        val s3 = st.rangeUntil(x)
        assert((s3: m.SortedSet[Int]) == st.until(x))
      }
      /* 2.11 doesn't have a mutable.SortedMap
      val mp = m.SortedMap("" -> 0, "ds" -> -3, "-??" -> 13, "Df" -> 33, "d!" -> -32)
      for (x <- List("", "-", "@", "aa", "D", "d")) {
        val m1 = mp.rangeFrom(x)
        assert(m1: m.SortedMap[String, Int], mp.from(x))
        val m2 = mp.rangeTo(x)
        assert(m2: m.SortedMap[String, Int], mp.to(x))
        val m3 = mp.rangeUntil(x)
        assert(m3: m.SortedMap[String, Int], mp.until(x))
      }
      */
    }
  }
}
