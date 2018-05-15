import sbtcrossproject.{crossProject, CrossType}
import ScalaModulePlugin._

inThisBuild(Seq(
  crossScalaVersions := Seq("2.12.6", "2.13.0-M4", "2.11.12")
))

lazy val `scala-collection-compat` = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(scalaModuleSettings)
  .jvmSettings(scalaModuleSettingsJVM)
  .settings(
    name := "scala-collection-compat",
    version := "0.1-SNAPSHOT",
    scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions"),
    unmanagedSourceDirectories in Compile += {
      val sharedSourceDir = baseDirectory.value.getParentFile / "src/main"
      if (scalaVersion.value.startsWith("2.13.")) sharedSourceDir / "scala-2.13"
      else sharedSourceDir / "scala-2.11_2.12"
    }
  )
  .jvmSettings(
    // TODO: should we add this?
    // OsgiKeys.exportPackage := Seq(s"scala.collection.compat.*;version=${version.value}"),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsSettings(
    fork in Test := false // Scala.js cannot run forked tests
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val `scala-collection-compatJVM` = `scala-collection-compat`.jvm
lazy val `scala-collection-compatJS` = `scala-collection-compat`.js
