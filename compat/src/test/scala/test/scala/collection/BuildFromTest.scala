/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package test.scala.collection

import org.junit.Test

import scala.collection.compat._
import scala.collection.immutable.{HashMap, TreeMap, TreeSet}
import scala.collection.mutable.{ArrayBuffer, Builder, ListBuffer}
import scala.collection.{BitSet, SortedMap, SortedSet, immutable, mutable}

// Tests copied from the 2.13 scala-library
class BuildFromTest {

  // Using BuildFrom to abstract over both and also allow building arbitrary collection types
  def optionSequence2[CC[X] <: Iterable[X], A, To](xs: CC[Option[A]])(
      implicit bf: BuildFrom[CC[Option[A]], A, To]): Option[To] =
    xs.foldLeft[Option[Builder[A, To]]](Some(bf.newBuilder(xs))) {
      case (Some(builder), Some(a)) => Some(builder += a)
      case _ => None
    }
      .map(_.result())

  // Using dependent types:
  def optionSequence3[A, To](xs: Iterable[Option[A]])(
      implicit bf: BuildFrom[xs.type, A, To]): Option[To] =
    xs.foldLeft[Option[Builder[A, To]]](Some(bf.newBuilder(xs))) {
      case (Some(builder), Some(a)) => Some(builder += a)
      case _ => None
    }
      .map(_.result())

  def eitherSequence[A, B, To](xs: Iterable[Either[A, B]])(
      implicit bf: BuildFrom[xs.type, B, To]): Either[A, To] =
    xs.foldLeft[Either[A, Builder[B, To]]](Right(bf.newBuilder(xs))) {
      case (Right(builder), Right(b)) => Right(builder += b)
      case (Left(a), _) => Left(a)
      case (_, Left(a)) => Left(a)
    }
      .right
      .map(_.result())

  @Test
  def optionSequence2Test: Unit = {
    val xs1 = List(Some(1), None, Some(2))
    val o1 = optionSequence2(xs1)
    val o1t: Option[List[Int]] = o1

    val xs2 = TreeSet(Some("foo"), Some("bar"), None)
    val o2 = optionSequence2(xs2)
    // Not working: the resolved implicit BuildFrom results in a SortedSet instead of a TreeSet
    // val o2t: Option[TreeSet[String]] = o2
    val o2t: Option[SortedSet[String]] = o2

    // Breakout-like use case from https://github.com/scala/scala/pull/5233:
    val xs4 = List[Option[(Int, String)]](Some((1 -> "a")), Some((2 -> "b")))
    val o4 = optionSequence2(xs4)(TreeMap)
    val o4t: Option[TreeMap[Int, String]] = o4
  }

  @Test
  def optionSequence3Test: Unit = {
    val xs1 = List(Some(1), None, Some(2))
    val o1 = optionSequence3(xs1)
    val o1t: Option[List[Int]] = o1

    val xs2 = TreeSet(Some("foo"), Some("bar"), None)
    val o2 = optionSequence3(xs2)
    // Not working: the resolved implicit BuildFrom results in a SortedSet instead of a TreeSet
    // val o2t: Option[TreeSet[String]] = o2
    val o2t: Option[SortedSet[String]] = o2

    // Breakout-like use case from https://github.com/scala/scala/pull/5233:
    val xs4 = List[Option[(Int, String)]](Some((1 -> "a")), Some((2 -> "b")))
    val o4 = optionSequence3(xs4)(TreeMap) // same syntax as in `.to`
    val o4t: Option[TreeMap[Int, String]] = o4
  }

  @Test
  def eitherSequenceTest: Unit = {
    val xs3 = ListBuffer(Right("foo"), Left(0), Right("bar"))
    val e1 = eitherSequence(xs3)
    val e1t: Either[Int, ListBuffer[String]] = e1
  }

  // From https://github.com/scala/collection-strawman/issues/44
  def flatCollect[A, B, To](coll: Iterable[A])(f: PartialFunction[A, IterableOnce[B]])(
      implicit bf: BuildFrom[coll.type, B, To]): To = {
    val builder = bf.newBuilder(coll)
    for (a <- coll) {
      if (f.isDefinedAt(a)) builder ++= f(a)
    }
    builder.result()
  }

  def mapSplit[A, B, C, ToL, ToR](coll: Iterable[A])(f: A => Either[B, C])(
      implicit bfLeft: BuildFrom[coll.type, B, ToL],
      bfRight: BuildFrom[coll.type, C, ToR]): (ToL, ToR) = {
    val left = bfLeft.newBuilder(coll)
    val right = bfRight.newBuilder(coll)
    for (a <- coll)
      f(a).fold(left.+=, right.+=)
    (left.result(), right.result())
  }

  @Test
  def flatCollectTest: Unit = {
    val xs1 = List(1, 2, 3)
    val xs2 = flatCollect(xs1) { case 2 => ArrayBuffer("foo", "bar") }
    val xs3: List[String] = xs2

    val xs4 = TreeMap((1, "1"), (2, "2"))
    val xs5 = flatCollect(xs4) { case (2, v) => List((v, v)) }
    val xs6: TreeMap[String, String] = xs5

    val xs7 = HashMap((1, "1"), (2, "2"))
    val xs8 = flatCollect(xs7) { case (2, v) => List((v, v)) }
    val xs9: HashMap[String, String] = xs8

    val xs10 = TreeSet(1, 2, 3)
    val xs11 = flatCollect(xs10) { case 2 => List("foo", "bar") }
    // Not working: the resolved implicit BuildFrom results in a SortedSet instead of a TreeSet
    // val xs12: TreeSet[String] = xs11
    val xs12: SortedSet[String] = xs11
  }

  @Test
  def mapSplitTest: Unit = {
    val xs1 = List(1, 2, 3)
    val (xs2, xs3) = mapSplit(xs1)(x => if (x % 2 == 0) Left(x) else Right(x.toString))
    val xs4: List[Int] = xs2
    val xs5: List[String] = xs3

    val xs6 = TreeMap((1, "1"), (2, "2"))
    val (xs7, xs8) = mapSplit(xs6) {
      case (k, v) => Left((v, k)): Either[(String, Int), (Int, Boolean)]
    }
    val xs9: TreeMap[String, Int] = xs7
    val xs10: TreeMap[Int, Boolean] = xs8
  }

  implicitly[BuildFrom[String, Char, String]]
  implicitly[BuildFrom[Array[Int], Char, Array[Char]]]
  implicitly[BuildFrom[BitSet, Int, BitSet]]
  implicitly[BuildFrom[immutable.BitSet, Int, immutable.BitSet]]
  implicitly[BuildFrom[mutable.BitSet, Int, mutable.BitSet]]

  // Check that collection companions can implicitly be converted to a `BuildFrom` instance
  Iterable: BuildFrom[_, Int, Iterable[Int]]
  Map: BuildFrom[_, (Int, String), Map[Int, String]]
  SortedSet: BuildFrom[_, Int, SortedSet[Int]]
  SortedMap: BuildFrom[_, (Int, String), SortedMap[Int, String]]

  // Implement BuildFrom
  class MyBuildFrom[From, A, C] extends BuildFrom[From, A, C] {
    def fromSpecific(from: From)(it: IterableOnce[A]): C = ???
    def newBuilder(from: From): Builder[A, C] = ???
  }
}
