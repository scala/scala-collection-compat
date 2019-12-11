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
    val msg = allProblems.iterator
        .map(_.description("new"))
        .mkString(s"The following ${allProblems.size} problems were reported but not expected:\n  - ", "\n  - ", "\n")
    Assert.assertEquals(msg, Nil, allProblems)
  }
}
