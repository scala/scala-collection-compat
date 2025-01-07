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

package scala.jdk.javaapi

import java.{lang => jl, util => ju}, java.util.{concurrent => juc}
import scala.collection.convert.{WrapAsJava, WrapAsScala}
import scala.collection._

/** This object contains methods that convert between Scala and Java collections.
 *
 * The explicit conversion methods defined here are intended to be used in Java code. For Scala
 * code, it is recommended to use the extension methods defined in
 * [[scala.jdk.CollectionConverters]].
 */
object CollectionConverters extends WrapAsJava with WrapAsScala {
  def asJava[A](i: Iterator[A]): ju.Iterator[A] = asJavaIterator(i)

  def asJava[A](i: Iterable[A]): jl.Iterable[A] = asJavaIterable(i)

  def asJava[A](b: mutable.Buffer[A]): ju.List[A] = bufferAsJavaList(b)

  def asJava[A](s: mutable.Seq[A]): ju.List[A] = mutableSeqAsJavaList(s)

  def asJava[A](s: Seq[A]): ju.List[A] = seqAsJavaList(s)

  def asJava[A](s: mutable.Set[A]): ju.Set[A] = mutableSetAsJavaSet(s)

  def asJava[A](s: Set[A]): ju.Set[A] = setAsJavaSet(s)

  def asJava[K, V](m: mutable.Map[K, V]): ju.Map[K, V] = mutableMapAsJavaMap(m)

  def asJava[K, V](m: Map[K, V]): ju.Map[K, V] = mapAsJavaMap(m)

  def asJava[K, V](m: concurrent.Map[K, V]): juc.ConcurrentMap[K, V] = mapAsJavaConcurrentMap(m)

  def asScala[A](i: ju.Iterator[A]): Iterator[A] = asScalaIterator(i)

  def asScala[A](e: ju.Enumeration[A]): Iterator[A] = enumerationAsScalaIterator(e)

  def asScala[A](i: jl.Iterable[A]): Iterable[A] = iterableAsScalaIterable(i)

  def asScala[A](c: ju.Collection[A]): Iterable[A] = collectionAsScalaIterable(c)

  def asScala[A](l: ju.List[A]): mutable.Buffer[A] = asScalaBuffer(l)

  def asScala[A](s: ju.Set[A]): mutable.Set[A] = asScalaSet(s)

  def asScala[A, B](m: ju.Map[A, B]): mutable.Map[A, B] = mapAsScalaMap(m)

  def asScala[A, B](m: juc.ConcurrentMap[A, B]): concurrent.Map[A, B] = mapAsScalaConcurrentMap(m)

  def asScala[A, B](d: ju.Dictionary[A, B]): mutable.Map[A, B] = dictionaryAsScalaMap(d)

  def asScala(p: ju.Properties): mutable.Map[String, String] = propertiesAsScalaMap(p)
}
