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

  @Test
  def sortedSetMinAfter(): Unit = {
    val values = 1 to 10
    assertEquals(values.to(collection.SortedSet).minAfter(8), Some(8))
    assertEquals(values.to(collection.immutable.SortedSet).minAfter(9), Some(9))
    assertEquals(values.to(collection.mutable.SortedSet).minAfter(10), Some(10))

    assertEquals(values.to(collection.SortedSet).minAfter(11), None)
    assertEquals(values.to(collection.immutable.SortedSet).minAfter(12), None)
    assertEquals(values.to(collection.mutable.SortedSet).minAfter(13), None)
  }

  @Test
  def sortedSetMaxBefore(): Unit = {
    val values = 1 to 10
    assertEquals(values.to(collection.SortedSet).maxBefore(4), Some(3))
    assertEquals(values.to(collection.immutable.SortedSet).maxBefore(3), Some(2))
    assertEquals(values.to(collection.mutable.SortedSet).maxBefore(2), Some(1))

    assertEquals(values.to(collection.SortedSet).maxBefore(1), None)
    assertEquals(values.to(collection.immutable.SortedSet).maxBefore(0), None)
    assertEquals(values.to(collection.mutable.SortedSet).maxBefore(-1), None)
  }

  @Test
  def sortedMapMinAfter(): Unit = {
    val values = (1 to 10).map(x => x -> x.toString)
    assertEquals(collection.SortedMap(values: _*).minAfter(9), Some(9 -> "9"))
    assertEquals(collection.immutable.SortedMap(values: _*).minAfter(10), Some(10 -> "10"))

    assertEquals(collection.SortedMap(values: _*).minAfter(11), None)
    assertEquals(collection.immutable.SortedMap(values: _*).minAfter(12), None)
  }

  @Test
  def sortedMapMaxBefore(): Unit = {
    val values = (1 to 10).map(x => x -> x.toString)
    assertEquals(collection.SortedMap(values: _*).maxBefore(3), Some(2 -> "2"))
    assertEquals(collection.immutable.SortedMap(values: _*).maxBefore(2), Some(1 -> "1"))

    assertEquals(collection.SortedMap(values: _*).maxBefore(1), None)
    assertEquals(collection.immutable.SortedMap(values: _*).maxBefore(0), None)
  }
}
