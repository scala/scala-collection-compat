import _root_.scalafix.sbt.BuildInfo.{scalafixVersion, scala212 => scalafixScala212}

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
    scalafixData211,
    scalafixData212,
    scalafixData213,
    scalafixInput,
    scalafixOutput211,
    scalafixOutput212,
    scalafixOutput213,
    // scalafixOutput213Failure,
    scalafixRules,
    scalafixTests
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
  _.settings(ScalaModulePlugin.scalaModuleSettings)
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

lazy val binaryCompatOld = project
  .in(file("binary-compat/old"))
  .settings(commonSettings)
  .settings(scalaVersion := scala212)
  .disablePlugins(ScalafixPlugin)

lazy val binaryCompatNew = project
  .in(file("binary-compat/new"))
  .settings(commonSettings)
  .settings(scalaVersion := scala212)
  .dependsOn(compat212JVM)
  .disablePlugins(ScalafixPlugin)

lazy val binaryCompat = project
  .in(file("binary-compat/test"))
  .settings(commonSettings)
  .settings(
    scalaVersion := scala212,
    libraryDependencies += "com.typesafe" %% "mima-core" % "0.8.0" % Test,
    junit,
    versionPolicyIntention := Compatibility.None,
    buildInfoPackage := "build",
    buildInfoKeys := Seq[BuildInfoKey](
      "oldClasses" -> (binaryCompatOld / Compile / classDirectory).value.toString,
      "newClasses" -> (binaryCompatNew / Compile / classDirectory).value.toString
    ),
    Test / test := (Test / test)
      .dependsOn(
        binaryCompatOld / Compile / compile,
        binaryCompatNew / Compile / compile,
      )
      .value
  )
  .enablePlugins(BuildInfoPlugin)
  .disablePlugins(ScalafixPlugin)

lazy val scalafixRules = project
  .in(file("scalafix/rules"))
  .settings(ScalaModulePlugin.scalaModuleSettings)
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
lazy val scalafixData = MultiScalaProject(
  "scalafixData",
  "scalafix/data",
  _.settings(sharedScalafixSettings)
    .settings(commonSettings)
    .settings(publish / skip := true)
)

val scalafixData211 = scalafixData(scala211, _.dependsOn(compat211JVM))
val scalafixData212 = scalafixData(scalafixScala212, _.dependsOn(compat212JVM))
val scalafixData213 = scalafixData(scala213, _.dependsOn(compat213JVM))

lazy val scalafixInput = project
  .in(file("scalafix/input"))
  .settings(commonSettings)
  .settings(sharedScalafixSettings)
  .settings(
    scalaVersion := scalafixScala212,
    publish / skip := true,
    headerCheck := Nil,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
  .dependsOn(scalafixData212)

val scalafixOutput = MultiScalaProject(
  "scalafixOutput",
  "scalafix/output",
  _.settings(sharedScalafixSettings)
    .settings(commonSettings)
    .settings(
      publish / skip := true,
      headerCheck := Nil,
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

lazy val scalafixOutput211 = scalafixOutput(
  scala211,
  _.dependsOn(scalafixData211)
)

lazy val scalafixOutput212 = scalafixOutput(
  scala212,
  _.settings(addOutput212)
    .settings(addOutput212Plus)
    .dependsOn(scalafixData212)
)

lazy val scalafixOutput213 = scalafixOutput(
  scala213,
  _.settings(addOutput213)
    .settings(addOutput212Plus)
    .dependsOn(scalafixData213)
)

lazy val scalafixOutput213Failure = project
  .in(file("scalafix/output213-failure"))
  .settings(commonSettings)
  .settings(sharedScalafixSettings)
  .settings(publish / skip := true)

lazy val scalafixTests = project
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
    scalafixTestkitInputSourceDirectories := (scalafixInput / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath := (scalafixInput / Compile / fullClasspath).value,
  )
  .dependsOn(scalafixInput, scalafixRules)
  .enablePlugins(BuildInfoPlugin, ScalafixTestkitPlugin)

val ciScalaVersion     = sys.env.get("CI_SCALA_VERSION").flatMap(Version.parse)
val isTravisTag        = sys.env.get("CI_TAG").exists(_.nonEmpty)
val isScalaJs          = sys.env.get("CI_PLATFORM") == Some("js")
val isScalaNative      = sys.env.get("CI_PLATFORM") == Some("native")
val isScalafix         = sys.env.get("CI_MODE") == Some("testScalafix")
val isScalafmt         = sys.env.get("CI_MODE") == Some("testScalafmt")
val isBinaryCompat     = sys.env.get("CI_MODE") == Some("testBinaryCompat")
val isHeaderCheck      = sys.env.get("CI_MODE") == Some("headerCheck")
val jdkVersion         = sys.env.get("CI_JDK").map(_.toInt)

// required by sbt-scala-module
inThisBuild {
  import scala.sys.process._
  Seq(
    commands += Command.command("scalafmtTest") { state =>
      val exitCode = Seq("admin/scalafmt.sh", "--test") ! state.globalLogging.full
      if (exitCode == 0) state else state.fail
    },
    commands += Command.command("scalafmt") { state =>
      Seq("admin/scalafmt.sh") ! state.globalLogging.full
      state
    },
    commands += Command.command("ci") { state =>
      val toRun: Seq[String] =
        if (isScalafmt)
          Seq("scalafmtTest")
        else if (isHeaderCheck)
          Seq("headerCheck")
        else {
          List(
            "CI_SCALA_VERSION",
            "CI_TAG",
            "CI_PLATFORM",
            "CI_MODE",
            "CI_JDK",
          ).foreach(k =>
            println(k.padTo(20, " ").mkString("") + " -> " + sys.env.getOrElse(k, "None")))

          val platformSuffix = if (isScalaJs) "JS" else if (isScalaNative) "Native" else ""

          val compatProject       = "compat" + ciScalaVersion.get.binary + platformSuffix
          val binaryCompatProject = "binaryCompat"

          val testProjectPrefix =
            if (isScalafix) {
              "scalafixTests"
            } else if (isBinaryCompat) {
              binaryCompatProject
            } else {
              compatProject
            }

          val projectPrefix =
            if (isScalafix) {
              "scalafixRules"
            } else if (isBinaryCompat) {
              binaryCompatProject
            } else {
              compatProject
            }

          val publishTask =
            if (isTravisTag && !isBinaryCompat && jdkVersion == Some(8)) {
              // we cannot run "ci-release" because that reads the `CI_RELEASE` / `CI_SONATYPE_RELEASE`
              // env vars, which we cannot modify from java (easily). so we inline what this command does.
              CiReleasePlugin.setupGpg()
              List(
                // same fix as https://github.com/olafurpg/sbt-ci-release/pull/66
                // need to replicate it here since we're not using the `ci-release` command
                "set pgpSecretRing := pgpSecretRing.value",
                "set pgpPublicRing := pgpPublicRing.value",
                s"$projectPrefix/publishSigned",
                "sonatypePrepare",
                "sonatypeBundleUpload",
                "sonatypeClose"
              )
            } else {
              Nil
            }

          Seq(
            List(s"""++${sys.env.get("CI_SCALA_VERSION").get}!"""),
            List(s"$projectPrefix/clean"),
            List(s"$testProjectPrefix/test"),
            List(s"$projectPrefix/publishLocal"),
            publishTask
          ).flatten
        }

      println("---------")
      println("Running CI: ")
      toRun.foreach(println)
      println("---------")

      val newCommands = toRun.toList.map(Exec(_, None))
      state.copy(remainingCommands = newCommands ::: state.remainingCommands)
    }
  )
}
