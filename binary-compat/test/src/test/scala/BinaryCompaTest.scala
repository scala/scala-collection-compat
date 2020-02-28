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

import org.junit.{Assert, Test}

import build.BuildInfo._

import com.typesafe.tools.mima.lib.MiMaLib
import com.typesafe.tools.mima.core.Config

class BinaryCompaTest {
  @Test
  def compat(): Unit = {
    Config.setup("foo", Array(oldClasspath, newClasspath))
    val mima        = new MiMaLib(Config.baseClassPath)
    val allProblems = mima.collectProblems(oldClasspath, newClasspath)
    val unexpectedDescriptions =
      allProblems.iterator
        .map(_.description("new"))
        // code improvement: it would be more standard to use a ProblemFilter here
        .filterNot(
          _ == "static method id(scala.collection.Iterable,scala.collection.generic.CanBuildFrom)scala.collection.Iterable in class org.example.Lib has a different signature in new version, where it is <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<Lscala/runtime/Nothing$;TA;TC;>;)TC; rather than <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<TC;TA;TC;>;)TC;")
        .filterNot(
          _ == "method id(scala.collection.Iterable,scala.collection.generic.CanBuildFrom)scala.collection.Iterable in object org.example.Lib has a different signature in new version, where it is <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<Lscala/runtime/Nothing$;TA;TC;>;)TC; rather than <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<TC;TA;TC;>;)TC;")
        .toList
    val msg =
      unexpectedDescriptions.mkString(
        s"The following ${unexpectedDescriptions.size} problems were reported but not expected:\n  - ",
        "\n  - ",
        "\n")
    Assert.assertEquals(msg, Nil, unexpectedDescriptions)
  }
}
