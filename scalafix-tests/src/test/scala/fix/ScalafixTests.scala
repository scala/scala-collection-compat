package fix

import scala.meta._
import scalafix.testkit._
import scalafix._

class ScalafixTests
  extends SemanticRuleSuite(
    SemanticdbIndex.load(Classpath(AbsolutePath(BuildInfo.inputClassdirectory))),
    AbsolutePath(BuildInfo.inputSourceroot),
    Seq(
      AbsolutePath(BuildInfo.output212Sourceroot),
      AbsolutePath(BuildInfo.output213Sourceroot),
      AbsolutePath(BuildInfo.output213FailureSourceroot)
    )
  ) {
  runAllTests()
}
