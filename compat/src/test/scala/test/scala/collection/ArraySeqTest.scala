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

import org.junit.{Assert, Test}

import scala.reflect.ClassTag
import scala.collection.compat.immutable.ArraySeq

// The unmodified ArraySeqTest from collection-strawman
class ArraySeqTest {
  @Test
  def slice(): Unit = {

    implicit def array2ArraySeq[T](array: Array[T]): ArraySeq[T] =
      ArraySeq.unsafeWrapArray(array)

    val booleanArray = Array(true, false, true, false)
    check(booleanArray, Array(true, false), Array(false, true))

    val shortArray = Array(1.toShort, 2.toShort, 3.toShort, 4.toShort)
    check(shortArray, Array(1.toShort, 2.toShort), Array(2.toShort, 3.toShort))

    val intArray = Array(1, 2, 3, 4)
    check(intArray, Array(1, 2), Array(2, 3))

    val longArray = Array(1L, 2L, 3L, 4L)
    check(longArray, Array(1L, 2L), Array(2L, 3L))

    val byteArray = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    check(byteArray, Array(1.toByte, 2.toByte), Array(2.toByte, 3.toByte))

    val charArray = Array('1', '2', '3', '4')
    check(charArray, Array('1', '2'), Array('2', '3'))

    val doubleArray = Array(1.0, 2.0, 3.0, 4.0)
    check(doubleArray, Array(1.0, 2.0), Array(2.0, 3.0))

    val floatArray = Array(1.0f, 2.0f, 3.0f, 4.0f)
    check(floatArray, Array(1.0f, 2.0f), Array(2.0f, 3.0f))

    val refArray = Array("1", "2", "3", "4")
    check[String](refArray, Array("1", "2"), Array("2", "3"))

    def unit1(): Unit = {}
    def unit2(): Unit = {}
    Assert.assertEquals(unit1(), unit2())
    // unitArray is actually an instance of Immutable[BoxedUnit], the check to which is actually checked slice
    // implementation of ofRef
    val unitArray: ArraySeq[Unit] = Array(unit1(), unit2(), unit1(), unit2())
    check(unitArray, Array(unit1(), unit1()), Array(unit1(), unit1()))
  }

  private def check[T](array: ArraySeq[T],
                       expectedSliceResult1: ArraySeq[T],
                       expectedSliceResult2: ArraySeq[T])(implicit elemTag: ClassTag[T]): Unit = {
    Assert.assertEquals(array, array.slice(-1, 4))
    Assert.assertEquals(array, array.slice(0, 5))
    Assert.assertEquals(array, array.slice(-1, 5))
    Assert.assertEquals(expectedSliceResult1, array.slice(0, 2))
    Assert.assertEquals(expectedSliceResult2, array.slice(1, 3))
    Assert.assertEquals(ArraySeq[T](), array.slice(1, 1))
    Assert.assertEquals(ArraySeq[T](), array.slice(2, 1))
  }

  @Test def ArraySeqIndexedSeqOptimized(): Unit = {
    val x = ArraySeq(1, 2)
    val y = ArraySeq(3, 4)
    val z: ArraySeq[Int] = x ++ y
    assert(z.toList == List(1, 2, 3, 4))
  }
}
