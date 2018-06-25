import sbtcrossproject.{crossProject, CrossType}
import ScalaModulePlugin._

inThisBuild(Seq(
  crossScalaVersions := Seq("2.12.6", "2.13.0-M4", "2.11.12")
))

lazy val root = project
  .in(file("."))
  .aggregate(
    `scala-collection-compatJVM`,
    `scala-collection-compatJS`,
    `scala-collection-compatNative`
  )


lazy val `scala-collection-compat` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(scalaModuleSettings)
  .jvmSettings(scalaModuleSettingsJVM)
  .settings(
    name := "scala-collection-compat",
    version := "0.1-SNAPSHOT",
    scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions"),
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.6.4",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    unmanagedSourceDirectories in Compile += {
      val sharedSourceDir = baseDirectory.value.getParentFile / "src/main"
      if (scalaVersion.value.startsWith("2.13.")) sharedSourceDir / "scala-2.13"
      else sharedSourceDir / "scala-2.11_2.12"
    }
  )
  .jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.collection.compat.*;version=${version.value}")
  )
  .jsSettings(
    scalacOptions += {
      val x = (baseDirectory in LocalRootProject).value.toURI.toString
      val y = "https://raw.githubusercontent.com/scala/scala-collection-compat/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$x->$y/"
    },
    fork in Test := false // Scala.js cannot run forked tests
  )
  .nativeSettings(
    scalaVersion := "2.11.12"
  )

lazy val `scala-collection-compatJVM` = `scala-collection-compat`.jvm
lazy val `scala-collection-compatJS` = `scala-collection-compat`.js
lazy val `scala-collection-compatNative` = `scala-collection-compat`.native
