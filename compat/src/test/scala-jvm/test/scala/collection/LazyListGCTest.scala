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

import scala.collection.compat.immutable.LazyList
import scala.ref.WeakReference
import scala.util.Try

// TODO: fill this out with all relevant LazyList methods
class LazyListGCTest {

  /** Test helper to verify that the given LazyList operation allows
   * GC of the head during processing of the tail.
   */
  def assertLazyListOpAllowsGC(op: (=> LazyList[Int], Int => Unit) => Any, f: Int => Unit): Unit = {
    val msgSuccessGC = "GC success"
    val msgFailureGC = "GC failure"

    // A LazyList of 500 elements at most. We will test that the head can be collected
    // while processing the tail. After each element we will GC and wait 10 ms, so a
    // failure to collect will take roughly 5 seconds.
    val ref = WeakReference(LazyList.from(1).take(500))

    def gcAndThrowIfCollected(n: Int): Unit = {
      System.gc()                                                   // try to GC
      Thread.sleep(10)                                              // give it 10 ms
      if (ref.get.isEmpty) throw new RuntimeException(msgSuccessGC) // we're done if head collected
      f(n)
    }

    val res = Try { op(ref(), gcAndThrowIfCollected) }.failed // success is indicated by an
    val msg = res.map(_.getMessage).getOrElse(msgFailureGC)   // exception with expected message
    // failure is indicated by no
    assertTrue(msg == msgSuccessGC) // exception, or one with different message
  }

  @Test
  def foreach_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.foreach(_), _ => ())
  }

  @Test
  def filter_all_foreach_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.filter(_ => true).foreach(_), _ => ())
  }

  @Test
  def filter_none_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC((ll, check) => ll.filter(i => { check(i); false }).headOption, _ => ())
  }

  @Test // scala/bug#8990
  def withFilter_after_first_foreach_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.withFilter(_ > 1).foreach(_), _ => ())
  }

  @Test // scala/bug#8990
  def withFilter_after_first_withFilter_foreach_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.withFilter(_ > 1).withFilter(_ < 100).foreach(_), _ => ())
  }

  @Test // scala/bug#11443
  def find_allowsGC(): Unit = {
    assertLazyListOpAllowsGC((ll, check) => ll.find(i => { check(i); false }), _ => ())
  }

  @Test
  def collect_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(
      (ll, check) => ll.collect({ case i if { check(i); false } => i }).headOption,
      _ => ())
  }

  @Test // scala/bug#11443
  def collectFirst_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(
      (ll, check) => ll.collectFirst({ case i if { check(i); false } => i }),
      _ => ())
  }

  @Test
  def map_foreach_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.map(_ + 1).foreach(_), _ => ())
  }

  @Test
  def tapEach_foreach_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.tapEach(_ + 1).foreach(_), _ => ())
  }

  @Test
  def tapEach_tail_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.tapEach(_).tail.headOption, _ => ())
  }

  @Test
  def flatMap_none_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC((ll, check) => ll.flatMap(i => { check(i); Nil }).headOption, _ => ())
  }

  @Test
  def tapEach_drop_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.tapEach(_).drop(1000000).headOption, _ => ())
  }

  @Test
  def dropWhile_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(
      (ll, check) => ll.dropWhile(i => { check(i); i < 1000000 }).headOption,
      _ => ())
  }

  @Test
  def tapEach_takeRight_headOption_allowsGC(): Unit = {
    assertLazyListOpAllowsGC(_.tapEach(_).takeRight(2).headOption, _ => ())
  }

  @Test
  def serialization(): Unit =
    if (scala.util.Properties.releaseVersion.exists(_.startsWith("2.12"))) {
      import java.io._

      def serialize(obj: AnyRef): Array[Byte] = {
        val buffer = new ByteArrayOutputStream
        val out    = new ObjectOutputStream(buffer)
        out.writeObject(obj)
        buffer.toByteArray
      }

      def deserialize(a: Array[Byte]): AnyRef = {
        val in = new ObjectInputStream(new ByteArrayInputStream(a))
        in.readObject
      }

      def serializeDeserialize[T <: AnyRef](obj: T) = deserialize(serialize(obj)).asInstanceOf[T]

      val l = LazyList.from(10)

      val ld1 = serializeDeserialize(l)
      assertEquals(l.take(10).toList, ld1.take(10).toList)

      l.tail.head
      val ld2 = serializeDeserialize(l)
      assertEquals(l.take(10).toList, ld2.take(10).toList)

      LazyListGCTest.serializationForceCount = 0
      val u = LazyList
        .from(10)
        .map(x => {
          LazyListGCTest.serializationForceCount += 1; x
        })

      def printDiff(): Unit = {
        val a = serialize(u)
        classOf[LazyList[_]]
          .getDeclaredField("scala$collection$compat$immutable$LazyList$$stateEvaluated")
          .setBoolean(u, true)
        val b = serialize(u)
        val i = a.zip(b).indexWhere(p => p._1 != p._2)
        println("difference: ")
        println(s"val from = ${a.slice(i - 10, i + 10).mkString("List[Byte](", ", ", ")")}")
        println(s"val to   = ${b.slice(i - 10, i + 10).mkString("List[Byte](", ", ", ")")}")
      }

      // to update this test, comment-out `LazyList.writeReplace` and run `printDiff`
      // printDiff()

      val from = List[Byte](83, 116, 97, 116, 101, 59, 120, 112, 0, 0, 0, 115, 114, 0, 33, 106, 97,
        118, 97, 46)
      val to = List[Byte](83, 116, 97, 116, 101, 59, 120, 112, 0, 0, 1, 115, 114, 0, 33, 106, 97,
        118, 97, 46)

      assertEquals(LazyListGCTest.serializationForceCount, 0)

      u.head
      assertEquals(LazyListGCTest.serializationForceCount, 1)

      val data = serialize(u)
      var i    = data.indexOfSlice(from)
      to.foreach(x => {
        data(i) = x; i += 1
      })

      val ud1 = deserialize(data).asInstanceOf[LazyList[Int]]

      // this check failed before scala/scala#10118, deserialization triggered evaluation
      assertEquals(LazyListGCTest.serializationForceCount, 1)

      ud1.tail.head
      assertEquals(LazyListGCTest.serializationForceCount, 2)

      u.tail.head
      assertEquals(LazyListGCTest.serializationForceCount, 3)
    }
}

object LazyListGCTest {
  var serializationForceCount = 0
}
