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

import org.junit.{Assert, Test}

import scala.collection.compat._
import scala.collection.{BitSet, immutable, mutable}

class FactoryTest {

  implicitly[Factory[Char, String]]
  implicitly[Factory[Char, Array[Char]]]
  implicitly[Factory[Int, collection.BitSet]]
  implicitly[Factory[Int, mutable.BitSet]]
  implicitly[Factory[Int, immutable.BitSet]]
  implicitly[Factory[Nothing, Seq[Nothing]]]

  def f[A] = implicitly[Factory[A, Stream[A]]]

  BitSet: Factory[Int, BitSet]
  Iterable: Factory[Int, Iterable[Int]]
  immutable.TreeSet: Factory[Int, immutable.TreeSet[Int]]
  Map: Factory[(Int, String), Map[Int, String]]
  immutable.TreeMap: Factory[(Int, String), immutable.TreeMap[Int, String]]

  @Test
  def streamFactoryPreservesLaziness(): Unit = {
    val factory = implicitly[Factory[Int, Stream[Int]]]
    var counter = 0
    val source  = Stream.continually { counter += 1; 1 }
    val result  = factory.fromSpecific(source)
    Assert.assertEquals(1, counter) // One element has been evaluated because Stream is not lazy in its head
  }

}
