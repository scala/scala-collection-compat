/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package test.scala.collection

import org.junit.Assert._
import org.junit.Test

import scala.collection.compat._
import scala.collection.{immutable => i, mutable => m}

class SortedTest {
  @Test
  def immutableRangeOps(): Unit = {
    val st = i.SortedSet(-1, 2, 4, 3)
    for (x <- List(0, 1, 4, -1, -4)) {
      val s1 = st.rangeFrom(x)
      assertEquals(s1: i.SortedSet[Int], st.from(x))
      val s2 = st.rangeTo(x)
      assertEquals(s2: i.SortedSet[Int], st.to(x))
      val s3 = st.rangeUntil(x)
      assertEquals(s3: i.SortedSet[Int], st.until(x))
    }
    val mp = i.SortedMap("" -> 0, "ds" -> -3, "-??" -> 13, "Df" -> 33, "d!" -> -32)
    for (x <- List("", "-", "@", "aa", "D", "d")) {
      val m1 = mp.rangeFrom(x)
      assertEquals(m1: i.SortedMap[String, Int], mp.from(x))
      val m2 = mp.rangeTo(x)
      assertEquals(m2: i.SortedMap[String, Int], mp.to(x))
      val m3 = mp.rangeUntil(x)
      assertEquals(m3: i.SortedMap[String, Int], mp.until(x))
    }
  }

  @Test
  def mutableRangeOps(): Unit = {
    val st = m.SortedSet(-1, 2, 4, 3)
    for (x <- List(0, 1, 4, -1, -4)) {
      val s1 = st.rangeFrom(x)
      assertEquals(s1: m.SortedSet[Int], st.from(x))
      val s2 = st.rangeTo(x)
      assertEquals(s2: m.SortedSet[Int], st.to(x))
      val s3 = st.rangeUntil(x)
      assertEquals(s3: m.SortedSet[Int], st.until(x))
    }
    /* 2.11 doesn't have a mutable.SortedMap
    val mp = m.SortedMap("" -> 0, "ds" -> -3, "-??" -> 13, "Df" -> 33, "d!" -> -32)
    for (x <- List("", "-", "@", "aa", "D", "d")) {
      val m1 = mp.rangeFrom(x)
      assertEquals(m1: m.SortedMap[String, Int], mp.from(x))
      val m2 = mp.rangeTo(x)
      assertEquals(m2: m.SortedMap[String, Int], mp.to(x))
      val m3 = mp.rangeUntil(x)
      assertEquals(m3: m.SortedMap[String, Int], mp.until(x))
    }
   */
  }
}
