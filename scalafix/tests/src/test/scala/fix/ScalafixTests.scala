package fix

import scala.meta._
import scalafix.testkit._
import scalafix._

class ScalafixTests
    extends SemanticRuleSuite(
      SemanticdbIndex.load(Classpath(AbsolutePath(build.BuildInfo.inputClassdirectory))),
      AbsolutePath(build.BuildInfo.inputSourceroot),
      Seq(
        AbsolutePath(build.BuildInfo.outputSourceroot),
        AbsolutePath(build.BuildInfo.outputScalaSpecific)
      )
    ) {

  runAllTests()
  // to run only one test:
  // testsToRun.filter(_.filename.toNIO.getFileName.toString == "Playground.scala" ).foreach(runOn)
}
