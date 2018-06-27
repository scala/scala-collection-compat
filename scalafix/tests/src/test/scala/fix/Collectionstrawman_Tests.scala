package fix

import scala.meta._
import scalafix.testkit._
import scalafix._

class Collectionstrawman_Tests
  extends SemanticRuleSuite(
    SemanticdbIndex.load(Classpath(AbsolutePath(BuildInfo.inputClassdirectory))),
    AbsolutePath(BuildInfo.inputSourceroot),
    Seq(
      AbsolutePath(BuildInfo.outputSourceroot), 
      AbsolutePath(BuildInfo.outputFailureSourceroot)
    )
  ) {
  runAllTests()
}
