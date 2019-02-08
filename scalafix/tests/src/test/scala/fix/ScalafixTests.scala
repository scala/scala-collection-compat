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

package fix

import scala.meta._
import scalafix.v0._
import scalafix.testkit._

class ScalafixTests extends scalafix.testkit.SemanticRuleSuite {

  val only: Option[String] =
    // Some("Playground") // << to run only one test:
    None

  def testOnly(file: String): Unit = {
    testsToRun
      .filter(_.path.testPath.toNIO.getFileName.toString.stripSuffix(".scala") == file)
      .foreach(runOn)
  }

  only match {
    case Some(file) => testOnly(file)
    case None       => runAllTests()
  }
}
