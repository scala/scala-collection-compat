package fix

import scala.meta._
import scalafix.v0._
import scalafix.testkit._

class ScalafixTests extends scalafix.testkit.SemanticRuleSuite {

  val only: Option[String] =
    Some("BreakoutSrc") // << to run only one test:
  // None

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
