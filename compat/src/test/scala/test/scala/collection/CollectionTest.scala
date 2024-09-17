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
import scala.collection.immutable.BitSet
import scala.collection.mutable.PriorityQueue
import scala.collection.LinearSeq

class CollectionTest {
  @Test
  def testTo: Unit = {
    val xs = List(1, 2, 3)
    val v = xs.to(Vector)
    val vT: Vector[Int] = v
    assertEquals(Vector(1, 2, 3), v)

    val a = xs.to(Array)
    val aT: Array[Int] = a
    assertEquals(Vector(1, 2, 3), a.toVector)

    val b = xs.to(BitSet) // we can fake a type constructor for the 2 standard BitSet types
    val bT: BitSet = b
    assertEquals(BitSet(1, 2, 3), b)

    val c = xs.to(PriorityQueue)
    val cT: PriorityQueue[Int] = c
    assert(PriorityQueue(1, 2, 3) sameElements c)

    val ys = List(1 -> "a", 2 -> "b")
    val m = ys.to(Map)
    // Not possible - `to` returns a Col[A] so this is only typed as an Iterable[(Int, String)]
    // val mT: Map[Int, String] = m
    assertEquals(Map(1 -> "a", 2 -> "b"), m)
    assertTrue(m.isInstanceOf[Map[_, _]])

    // Stream.to(Seq) doesn't evaluate the stream
    val strm = 1 #:: { throw new Exception("not lazy") } #:: Stream.empty[Int]
    val strmsq: Seq[Int] = strm.to(Seq)
    var strmln: LinearSeq[Int] = strm.to(LinearSeq)
  }

  @Test
  def testFrom: Unit = {
    val xs = List(1, 2, 3)
    val a = Array.from(xs)
    val aT: Array[Int] = a
    assertTrue(Array(1, 2, 3).sameElements(a))
    val v = Vector.from(xs)
    val vT: Vector[Int] = v
    assertEquals(Vector(1, 2, 3), v)

    val b = BitSet.fromSpecific(xs)
    val bT: BitSet = b
    assertEquals(BitSet(1, 2, 3), b)

    val ys = List(1 -> "a", 2 -> "b")
    val m = Map.from(ys)
    val mT: Map[Int, String] = m
    assertEquals(Map(1 -> "a", 2 -> "b"), m)
  }

  @Test
  def testIterator: Unit = {
    val xs = Iterator(1, 2, 3).iterator.toList
    assertEquals(List(1, 2, 3), xs)
  }

  @Test
  def testSameElements: Unit = {
    val it1: Iterable[Int] = List(1)
    val it2: Iterable[Int] = List(1, 2, 3)
    val it3: Iterable[Int] = List(1, 2, 3)

    assertTrue(it1.iterator.sameElements(it1))
    assertFalse(it1.iterator.sameElements(it2))
    assertTrue(it2.iterator.sameElements(it3))
  }

  @Test
  def groupMap(): Unit = {
    val res = Seq("foo", "test", "bar", "baz")
      .groupMap(_.length)(_.toUpperCase())
    assertEquals(Map(3 -> Seq("FOO", "BAR", "BAZ"), 4 -> Seq("TEST")), res)
  }

  @Test
  def groupMapReduce(): Unit = {
    val res = Seq("foo", "test", "bar", "baz")
      .groupMapReduce(_.length)(_ => 1)(_ + _)
    assertEquals(Map(3 -> 3, 4 -> 1), res)
  }

  @Test
  def partitionMapTest(): Unit = {
    val empty = Seq.empty[Int].partitionMap(Right(_))
    assertEquals((Seq(), Seq()), empty)

    val res = Seq("foo", "test", "bar", "baz")
      .partitionMap {
        case s if s.contains("a") => Left(s)
        case s => Right(s.length)
      }
    assertEquals((Seq("bar", "baz"), Seq("foo".length, "test".length)), res)
  }

  @Test
  def nextOption(): Unit = {
    val it = Iterator(1, 2)
    assertEquals(Some(1), it.nextOption())
    assertEquals(Some(2), it.nextOption())
    assertEquals(None, it.nextOption())
  }

  @Test
  def tapEach(): Unit = {
    var count = 0
    val it = Iterator(1, 2, 3).tapEach(count += _)
    assertEquals(0, count)
    it.foreach(_ => ())
    assertEquals(6, count)
    List(1, 2, 3).tapEach(count += _)
    assertEquals(12, count)
    val stream = Stream(1, 2, 3).tapEach(count += _)
    assertEquals(13, count)
    stream.force
    assertEquals(18, count)
  }

  @Test
  def sizeCompare(): Unit = {
    assertTrue(Set(1, 2, 3).sizeCompare(4) < 0)
    assertTrue(Set(1, 2, 3).sizeCompare(2) > 0)
    assertTrue(Set(1, 2, 3).sizeCompare(3) == 0)

    assertTrue(List(1, 2, 3).sizeCompare(4) < 0)
    assertTrue(List(1, 2, 3).sizeCompare(2) > 0)
    assertTrue(List(1, 2, 3).sizeCompare(3) == 0)

    assertTrue(Set(1, 2, 3).sizeCompare(List(1, 2, 3, 4)) < 0)
    assertTrue(Set(1, 2, 3).sizeCompare(List(1, 2)) > 0)
    assertTrue(Set(1, 2, 3).sizeCompare(List(1, 2, 3)) == 0)

    assertTrue(Set(1, 2, 3).sizeCompare(Vector(1, 2, 3, 4)) < 0)
    assertTrue(Set(1, 2, 3).sizeCompare(Vector(1, 2)) > 0)
    assertTrue(Set(1, 2, 3).sizeCompare(Vector(1, 2, 3)) == 0)

    assertTrue(Vector(1, 2, 3).sizeCompare(Set(1, 2, 3, 4)) < 0)
    assertTrue(Vector(1, 2, 3).sizeCompare(Set(1, 2)) > 0)
    assertTrue(Vector(1, 2, 3).sizeCompare(Set(1, 2, 3)) == 0)
  }

  @Test
  def sizeIsLengthIs(): Unit = {
    assertTrue(Set(1, 2, 3).sizeIs < 4)
    assertTrue(Set(1, 2, 3).sizeIs <= 4)
    assertTrue(Set(1, 2, 3).sizeIs <= 3)
    assertTrue(Set(1, 2, 3).sizeIs == 3)
    assertTrue(Set(1, 2, 3).sizeIs >= 3)
    assertTrue(Set(1, 2, 3).sizeIs >= 2)
    assertTrue(Set(1, 2, 3).sizeIs > 2)

    assertTrue(List(1, 2, 3).sizeIs < 4)
    assertTrue(List(1, 2, 3).sizeIs <= 4)
    assertTrue(List(1, 2, 3).sizeIs <= 3)
    assertTrue(List(1, 2, 3).sizeIs == 3)
    assertTrue(List(1, 2, 3).sizeIs >= 3)
    assertTrue(List(1, 2, 3).sizeIs >= 2)
    assertTrue(List(1, 2, 3).sizeIs > 2)

    assertTrue(List(1, 2, 3).lengthIs < 4)
    assertTrue(List(1, 2, 3).lengthIs <= 4)
    assertTrue(List(1, 2, 3).lengthIs <= 3)
    assertTrue(List(1, 2, 3).lengthIs == 3)
    assertTrue(List(1, 2, 3).lengthIs >= 3)
    assertTrue(List(1, 2, 3).lengthIs >= 2)
    assertTrue(List(1, 2, 3).lengthIs > 2)
  }

  @Test
  def testDistinctBy(): Unit = {
    assertEquals(List(1, 2, 3).distinctBy(_ % 2 == 0), List(1, 2))
    assertEquals(List(3, 1, 2).distinctBy(_ % 2 == 0), List(3, 2))
    assertEquals(List.empty[Int].distinctBy(_ % 2 == 0), List.empty)
  }

  @Test
  def testUnfold(): Unit = {
    def typed[A](x: A): Unit = ()

    val list = List.unfold(1)(x => if (x <= 5) Some((x.toString, x + 1)) else None)
    typed[List[String]](list)
    assertEquals(list, List("1", "2", "3", "4", "5"))

    val vector = Vector.unfold(1)(x => if (x <= 100) Some((x, x * 3)) else None)
    typed[Vector[Int]](vector)
    assertEquals(vector, Vector(1, 3, 9, 27, 81))

    val seq = collection.Seq.unfold(1L)(x => if (x <= 10L) Some(x, x + 2L) else None)
    typed[collection.Seq[Long]](seq)
    assertEquals(seq, collection.Seq(1L, 3L, 5L, 7L, 9L))

    val iterable = Iterable.unfold(4)(x => if (x > 0) Some(("a" * x, x - 1)) else None)
    typed[Iterable[String]](iterable)
    assertEquals(iterable, Iterable("aaaa", "aaa", "aa", "a"))

    val arrayBuffer =
      collection.mutable.ArrayBuffer.unfold(1)(x => if (x < 3) Some((x, x + 1)) else None)
    typed[collection.mutable.ArrayBuffer[Int]](arrayBuffer)
    assertEquals(arrayBuffer, collection.mutable.ArrayBuffer(1, 2))
  }
}
