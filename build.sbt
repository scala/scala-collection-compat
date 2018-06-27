import ScalaModulePlugin._
import sbtcrossproject.{crossProject, CrossType}
import _root_.scalafix.Versions.{version => scalafixVersion, scala212 => scalafixScala212}

lazy val scala212 = "2.12.6"
lazy val scala213 = "2.13.0-M4"

inThisBuild(Seq(
  crossScalaVersions := Seq(scala212, scala213, "2.11.12")
))

lazy val root = project
  .in(file("."))
  .aggregate(rules, input, output, tests, compatJVM, compatJS)
  .disablePlugins(ScalafixPlugin)

lazy val compat = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    moduleName := "scala-collection-compat",
    version := "0.1-SNAPSHOT",
    scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions"),
    unmanagedSourceDirectories in Compile += {
      val sharedSourceDir = baseDirectory.value.getParentFile / "src/main"
      if (scalaVersion.value.startsWith("2.13.")) sharedSourceDir / "scala-2.13"
      else sharedSourceDir / "scala-2.11_2.12"
    },
    scalaVersion := scala212
  )
  .settings(scalaModuleSettings)
  .jvmSettings(scalaModuleSettingsJVM)
  .jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.collection.compat.*;version=${version.value}"),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsSettings(
    scalacOptions += {
      val x = (baseDirectory in LocalRootProject).value.toURI.toString
      val y = "https://raw.githubusercontent.com/scala/scala-collection-compat/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$x->$y/"
    },
    fork in Test := false // Scala.js cannot run forked tests
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .disablePlugins(ScalafixPlugin)

lazy val compatJVM = compat.jvm
lazy val compatJS = compat.js

lazy val rules = project
  .in(file("scalafix/rules"))
  .settings(
    scalaVersion := scalafixScala212,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
  )

lazy val input = project
  .in(file("scalafix/input"))
  .settings(
    scalaVersion := scalafixScala212,
    scalafixSourceroot := sourceDirectory.in(Compile).value
  )

lazy val output = project
  .in(file("scalafix/output"))
  .settings(
    resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    scalaVersion := scala213
  )

lazy val outputFailure = project
  .in(file("scalafix/output-failure"))
  .settings(
    resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    scalaVersion := scala213
  )

lazy val tests = project
  .in(file("scalafix/tests"))
  .settings(
    scalaVersion := scalafixScala212,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    buildInfoPackage := "fix",
    buildInfoKeys := Seq[BuildInfoKey](
      "inputSourceroot" ->
        sourceDirectory.in(input, Compile).value,
      "outputSourceroot" ->
        sourceDirectory.in(output, Compile).value,
      "outputFailureSourceroot" ->
        sourceDirectory.in(outputFailure, Compile).value,
      "inputClassdirectory" ->
        classDirectory.in(input, Compile).value
    ),
    test in Test := (test in Test).dependsOn(compile in (output, Compile)).value
  )
  .dependsOn(input, rules)
  .enablePlugins(BuildInfoPlugin)
