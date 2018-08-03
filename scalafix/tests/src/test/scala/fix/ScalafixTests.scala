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
        AbsolutePath(build.BuildInfo.output212Sourceroot),
        AbsolutePath(build.BuildInfo.output213Sourceroot),
        AbsolutePath(build.BuildInfo.output212PlusSourceroot),
        AbsolutePath(build.BuildInfo.output213FailureSourceroot)
      )
    ) {

  runAllTests()
  // to run only one test:
  // testsToRun.filter(_.filename.toNIO.getFileName.toString == "Playground.scala" ).foreach(runOn)
}
