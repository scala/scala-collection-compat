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

import org.junit.{Assert, Test}

import build.BuildInfo._

import java.io.File

import com.typesafe.tools.mima.lib.MiMaLib

class BinaryCompaTest {
  @Test
  def compat(): Unit = {
    val mima = new MiMaLib(Seq())
    val allProblems = mima.collectProblems(new File(oldClasses), new File(newClasses))
    val unexpectedDescriptions =
      allProblems.iterator
        .map(_.description("new"))
        // code improvement: it would be more standard to use a ProblemFilter here
        .filterNot(
          _ == "method id(scala.collection.Iterable,scala.collection.generic.CanBuildFrom)scala.collection.Iterable in object org.example.Lib has a different generic signature in new version, where it is <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<Lscala/runtime/Nothing$;TA;TC;>;)TC; rather than <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<TC;TA;TC;>;)TC;. See https://github.com/lightbend/mima#incompatiblesignatureproblem")
        .filterNot(
          _ == "static method id(scala.collection.Iterable,scala.collection.generic.CanBuildFrom)scala.collection.Iterable in class org.example.Lib has a different generic signature in new version, where it is <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<Lscala/runtime/Nothing$;TA;TC;>;)TC; rather than <A:Ljava/lang/Object;C::Lscala/collection/Iterable<Ljava/lang/Object;>;>(TC;Lscala/collection/generic/CanBuildFrom<TC;TA;TC;>;)TC;. See https://github.com/lightbend/mima#incompatiblesignatureproblem")
        .toList
    val msg =
      unexpectedDescriptions.mkString(
        s"The following ${unexpectedDescriptions.size} problems were reported but not expected:\n  - ",
        "\n  - ",
        "\n")
    Assert.assertEquals(msg, Nil, unexpectedDescriptions)
  }
}
