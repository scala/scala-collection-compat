package test.scala.collection

import org.junit.Assert._
import org.junit.Test

class JdkVersionTest {

  @Test
  def testJavaVersion: Unit = {
    val isOnCi = sys.env.get("CI").isDefined

    if (isOnCi) {
      val travisJdkVersion = sys.env("TRAVIS_JDK_VERSION")
      val jdkVersion = sys.props("java.specification.version")

      if (travisJdkVersion == "openjdk6") {
        assertEquals(jdkVersion, "1.8")
      } else if (travisJdkVersion == "oraclejdk8") {
        assertEquals(jdkVersion, "1.6")
      } else {
        throw new Exception(s"Unknown CI jdk version: $travisJdkVersion")
      }
    }
  }
}
