package test.scala.collection

import org.junit.Assert._
import org.junit.Test

class JdkVersionTest {

  @Test
  def testJavaVersion: Unit = {
    val isOnCi = sys.env.get("CI").isDefined
    if (isOnCi) {
      val travisJdkVersion = sys.env("TRAVIS_JDK_VERSION")
      val obtained         = sys.props("java.specification.version")

      println(s"travisJdkVersion: $travisJdkVersion")
      println(s"jdkVersion: $obtained")

      val expectedJdkVersion = Map(
        "openjdk6"   -> "1.6",
        "oraclejdk8" -> "1.8"
      )

      expectedJdkVersion.get(travisJdkVersion) match {
        case Some(expected) => assertEquals(obtained, expected)
        case None           => throw new Exception(s"Unknown CI jdk version: $travisJdkVersion")
      }
    }
  }
}
