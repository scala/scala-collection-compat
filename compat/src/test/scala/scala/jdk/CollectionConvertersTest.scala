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

package scala.jdk

import java.{lang => jl, util => ju}

import org.junit.Test

import scala.collection.compat._
import scala.collection.{mutable => m}
import scala.jdk.CollectionConverters._
import scala.{collection => c}

class CollectionConvertersTest {
  @Test
  def extensions(): Unit = {
    val it = "a b c".split(" ").iterator

    {
      val j = it.asJava
      val je = it.asJavaEnumeration
      val s = (j: ju.Iterator[String]).asScala
      assert((s: Iterator[String]) eq it)
      val es = (je: ju.Enumeration[String]).asScala
      assert((es: Iterator[String]) eq it)
    }

    {
      val i: c.Iterable[String] = it.to(Iterable)
      val j = i.asJava
      val jc = i.asJavaCollection
      val s = (j: jl.Iterable[String]).asScala
      assert((s: c.Iterable[String]) eq i)
      val cs = (jc: ju.Collection[String]).asScala
      assert((cs: c.Iterable[String]) eq i)
    }

    {
      val b: m.Buffer[String] = it.to(m.Buffer)
      val j = b.asJava
      val s = (j: ju.List[String]).asScala
      assert((s: m.Buffer[String]) eq b)
    }

    {
      val q: m.Seq[String] = it.to(m.Seq)
      val j = q.asJava
      val s = (j: ju.List[String]).asScala
      assert((s: m.Buffer[String]) == q) // not eq
    }

    {
      val q: c.Seq[String] = it.to(c.Seq)
      val j = q.asJava
      val s = (j: ju.List[String]).asScala
      assert((s: m.Buffer[String]) == q) // not eq
    }

    {
      val t: m.Set[String] = it.to(m.Set)
      val j = t.asJava
      val s = (j: ju.Set[String]).asScala
      assert((s: m.Set[String]) eq t)
    }

    {
      val t: c.Set[String] = it.to(c.Set)
      val j = t.asJava
      val s = (j: ju.Set[String]).asScala
      assert((s: m.Set[String]) == t) // not eq
    }

    {
      val p: m.Map[String, String] = m.Map(it.map(a => (a, a)).toSeq: _*)
      val j = p.asJava
      val jd = p.asJavaDictionary
      val s = (j: ju.Map[String, String]).asScala
      assert((s: m.Map[String, String]) eq p)
      val ds = (jd: ju.Dictionary[String, String]).asScala
      assert((ds: m.Map[String, String]) eq p)
    }

    {
      val p: c.Map[String, String] = c.Map(it.map(a => (a, a)).toSeq: _*)
      val j = p.asJava
      val s = (j: ju.Map[String, String]).asScala
      assert((s: m.Map[String, String]) == p) // not eq
    }

//    Scala.js doesn't like the concurrent packages
//    {
//      val p: scala.collection.concurrent.Map[String, String] = scala.collection.concurrent.TrieMap(it.map(a => (a, a)).toSeq: _*)
//      val j = p.asJava
//      val s = (j: java.util.concurrent.ConcurrentMap[String, String]).asScala
//      assert((s: scala.collection.concurrent.Map[String, String]) eq p)
//    }
  }
}
