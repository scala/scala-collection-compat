import _root_.scalafix.sbt.BuildInfo.{scalafixVersion, scala212 => scalafixScala212}
import com.lightbend.tools.scalamoduleplugin.ScalaModulePlugin._

import scala.sys.process._

lazy val commonSettings = Seq(
  headerLicense := Some(HeaderLicense.Custom(s"""|Scala (https://www.scala-lang.org)
                                                 |
                                                 |Copyright EPFL and Lightbend, Inc.
                                                 |
                                                 |Licensed under Apache License 2.0
                                                 |(http://www.apache.org/licenses/LICENSE-2.0).
                                                 |
                                                 |See the NOTICE file distributed with this work for
                                                 |additional information regarding copyright ownership.
                                                 |""".stripMargin))
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "scala-collection-compat",
    publish / skip := true
  )
  .aggregate(
    compat211JVM,
    compat211JS,
    compat211Native,
    compat212JVM,
    compat212JS,
    compat212Native,
    compat213JVM,
    compat213JS,
    compat213Native,
    compat30JVM,
    compat30JS,
    `scalafix-data211`,
    `scalafix-data212`,
    `scalafix-data213`,
    `scalafix-input`,
    `scalafix-output211`,
    `scalafix-output212`,
    `scalafix-output213`,
    // `scalafix-output213-failure`,
    `scalafix-rules`,
    `scalafix-tests`
  )
  .disablePlugins(ScalafixPlugin)

// == Core Libraries ==

lazy val junit = libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.15"
lazy val scala213 = "2.13.6"
lazy val scala30  = "3.0.2"

lazy val compat = MultiScalaCrossProject(JSPlatform, JVMPlatform, NativePlatform)(
  "compat",
  _.settings(scalaModuleSettings)
    .settings(commonSettings)
    .settings(
      name := "scala-collection-compat",
      moduleName := "scala-collection-compat",
      scalaModuleAutomaticModuleName := Some("scala.collection.compat"),
      scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions"),
      Compile / unmanagedSourceDirectories += {
        val sharedSourceDir = (ThisBuild / baseDirectory).value / "compat/src/main"
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((3, _) | (2, 13)) =>
            sharedSourceDir / "scala-2.13"
          case _ =>
            sharedSourceDir / "scala-2.11_2.12"
        }
      },
      versionPolicyIntention := Compatibility.BinaryCompatible,
    )
    .jvmSettings(
      Test / unmanagedSourceDirectories += (ThisBuild / baseDirectory).value / "compat/src/test/scala-jvm",
      junit,
      mimaBinaryIssueFilters ++= {
        import com.typesafe.tools.mima.core._
        import com.typesafe.tools.mima.core.ProblemFilters._
        Seq(
          exclude[ReversedMissingMethodProblem]("scala.collection.compat.PackageShared.*"), // it's package-private
        )
      },
    )
    .jsSettings(
      scalacOptions ++= {
        val x = (LocalRootProject / baseDirectory).value.toURI.toString
        val y = "https://raw.githubusercontent.com/scala/scala-collection-compat/" + sys.process
          .Process("git rev-parse HEAD")
          .lineStream_!
          .head
        val opt = CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((3, _)) => "-scalajs-mapSourceURI"
          case _            => "-P:scalajs:mapSourceURI"
        }
        Seq(s"$opt:$x->$y/")
      },
      Test / fork := false // Scala.js cannot run forked tests
    )
    .jsEnablePlugins(ScalaJSJUnitPlugin)
    .disablePlugins(ScalafixPlugin)
    .nativeSettings(
      nativeLinkStubs := true,
      addCompilerPlugin(
        "org.scala-native" % "junit-plugin" % nativeVersion cross CrossVersion.full
      ),
      libraryDependencies += "org.scala-native" %%% "junit-runtime" % nativeVersion,
      Test / fork := false // Scala Native cannot run forked tests
    )
)

val compat211 = compat(scala211)
val compat212 = compat(scala212)
val compat213 = compat(scala213)
val compat30  = compat(scala30)

lazy val compat211JVM    = compat211.jvm
lazy val compat211JS     = compat211.js
lazy val compat211Native = compat211.native
lazy val compat212JVM    = compat212.jvm
lazy val compat212JS     = compat212.js
lazy val compat212Native = compat212.native
lazy val compat213JVM    = compat213.jvm
lazy val compat213JS     = compat213.js
lazy val compat213Native = compat213.native
lazy val compat30JVM     = compat30.jvm
lazy val compat30JS      = compat30.js

lazy val `binary-compat-old` = project
  .in(file("binary-compat/old"))
  .settings(commonSettings)
  .settings(scalaVersion := scala212)
  .disablePlugins(ScalafixPlugin)

lazy val `binary-compat-new` = project
  .in(file("binary-compat/new"))
  .settings(commonSettings)
  .settings(scalaVersion := scala212)
  .dependsOn(compat212JVM)
  .disablePlugins(ScalafixPlugin)

lazy val `binary-compat` = project
  .in(file("binary-compat/test"))
  .settings(commonSettings)
  .settings(
    scalaVersion := scala212,
    libraryDependencies += "com.typesafe" %% "mima-core" % "0.8.0" % Test,
    junit,
    versionPolicyIntention := Compatibility.None,
    buildInfoPackage := "build",
    buildInfoKeys := Seq[BuildInfoKey](
      "oldClasses" -> (`binary-compat-old` / Compile / classDirectory).value.toString,
      "newClasses" -> (`binary-compat-new` / Compile / classDirectory).value.toString
    ),
    Test / test := (Test / test)
      .dependsOn(
        `binary-compat-old` / Compile / compile,
        `binary-compat-new` / Compile / compile,
      )
      .value
  )
  .enablePlugins(BuildInfoPlugin)
  .disablePlugins(ScalafixPlugin)

lazy val `scalafix-rules` = project
  .in(file("scalafix/rules"))
  .settings(scalaModuleSettings)
  .settings(commonSettings)
  .settings(
    scalaModuleAutomaticModuleName := None,
    organization := (compat212JVM / organization).value,
    publishTo := (compat212JVM / publishTo).value,
    versionPolicyIntention := Compatibility.None,
    name := "scala-collection-migrations",
    scalaVersion := scalafixScala212,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
  )

// == Scalafix Test Setup ==
lazy val sharedScalafixSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked"
  )
)

// common part between input/output
lazy val `scalafix-data` = MultiScalaProject(
  "scalafix-data",
  "scalafix/data",
  _.settings(sharedScalafixSettings)
    .settings(commonSettings)
    .settings(publish / skip := true)
)

val `scalafix-data211` = `scalafix-data`(scala211, _.dependsOn(compat211JVM))
val `scalafix-data212` = `scalafix-data`(scalafixScala212, _.dependsOn(compat212JVM))
val `scalafix-data213` = `scalafix-data`(scala213, _.dependsOn(compat213JVM))

lazy val `scalafix-input` = project
  .in(file("scalafix/input"))
  .settings(commonSettings)
  .settings(sharedScalafixSettings)
  .settings(
    scalaVersion := scalafixScala212,
    publish / skip := true,
    excludeFilter := HiddenFileFilter || "*.scala",
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
  .dependsOn(`scalafix-data212`)

val `scalafix-output` = MultiScalaProject(
  "scalafix-output",
  "scalafix/output",
  _.settings(sharedScalafixSettings)
    .settings(commonSettings)
    .settings(
      publish / skip := true,
      excludeFilter := HiddenFileFilter || "*.scala",
    )
    .disablePlugins(ScalafixPlugin)
)

lazy val outputCross =
  Def.setting((ThisBuild / baseDirectory).value / "scalafix/output/src/main/scala")

lazy val output212 =
  Def.setting((ThisBuild / baseDirectory).value / "scalafix/output212/src/main/scala")
lazy val addOutput212 = Compile / unmanagedSourceDirectories += output212.value

lazy val output212Plus =
  Def.setting((ThisBuild / baseDirectory).value / "scalafix/output212+/src/main/scala")
lazy val addOutput212Plus = Compile / unmanagedSourceDirectories += output212Plus.value

lazy val output213 =
  Def.setting((ThisBuild / baseDirectory).value / "scalafix/output213/src/main/scala")
lazy val addOutput213 = Compile / unmanagedSourceDirectories += output213.value

lazy val `scalafix-output211` = `scalafix-output`(
  scala211,
  _.dependsOn(`scalafix-data211`)
)

lazy val `scalafix-output212` = `scalafix-output`(
  scala212,
  _.settings(addOutput212)
    .settings(addOutput212Plus)
    .dependsOn(`scalafix-data212`)
)

lazy val `scalafix-output213` = `scalafix-output`(
  scala213,
  _.settings(addOutput213)
    .settings(addOutput212Plus)
    .dependsOn(`scalafix-data213`)
)

lazy val `scalafix-output213-failure` = project
  .in(file("scalafix/output213-failure"))
  .settings(commonSettings)
  .settings(sharedScalafixSettings)
  .settings(publish / skip := true)

lazy val `scalafix-tests` = project
  .in(file("scalafix/tests"))
  .settings(commonSettings)
  .settings(sharedScalafixSettings)
  .settings(
    scalaVersion := scalafixScala212,
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories := Seq(
      outputCross.value,
      output212.value,
      output212Plus.value,
      output213.value
    ),
    scalafixTestkitInputSourceDirectories := (`scalafix-input` / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath := (`scalafix-input` / Compile / fullClasspath).value,
  )
  .dependsOn(`scalafix-input`, `scalafix-rules`)
  .enablePlugins(BuildInfoPlugin, ScalafixTestkitPlugin)
