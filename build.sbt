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
    compat31Native,
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

lazy val junit = libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.16"
lazy val scala213 = "2.13.8"
lazy val scala30  = "3.0.2"
lazy val scala31  = "3.1.3"

lazy val compat = new MultiScalaCrossProject(
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
      Test / unmanagedSourceDirectories += {
        val sharedSourceDir = (ThisBuild / baseDirectory).value / "compat/src/test"
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((3, _) | (2, 13)) =>
            sharedSourceDir / "scala-2.13"
          case _ =>
            sharedSourceDir / "scala-2.11_2.12"
        }
      },
      versionPolicyIntention := Compatibility.BinaryCompatible,
      mimaBinaryIssueFilters ++= {
        import com.typesafe.tools.mima.core._
        import com.typesafe.tools.mima.core.ProblemFilters._
        Seq(
          exclude[ReversedMissingMethodProblem]("scala.collection.compat.PackageShared.*"), // it's package-private
          exclude[Problem]("scala.collection.compat.*PreservingBuilder*")
        )
      }
    )
    .jvmSettings(
      Test / unmanagedSourceDirectories += (ThisBuild / baseDirectory).value / "compat/src/test/scala-jvm",
      Compile / unmanagedSourceDirectories += {
        val jvmParent = (ThisBuild / baseDirectory).value / "compat/jvm/src/main"
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((3, _) | (2, 13)) =>
            jvmParent / "scala-2.13"
          case _ =>
            jvmParent / "scala-2.11_2.12"
        }
      },
      junit
    )
    .disablePlugins(ScalafixPlugin),
  _.jsSettings(
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
    Test / unmanagedSourceDirectories += (ThisBuild / baseDirectory).value / "compat/src/test/scala-js",
    Compile / unmanagedSourceDirectories += {
      val jsAndNativeSourcesParent = (ThisBuild / baseDirectory).value / "compat/jsNative/src/main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _) | (2, 13)) =>
          jsAndNativeSourcesParent / "scala-2.13"
        case _ =>
          jsAndNativeSourcesParent / "scala-2.11_2.12"
      }
    },
    Test / fork := false // Scala.js cannot run forked tests
  ).jsEnablePlugins(ScalaJSJUnitPlugin),
  _.nativeSettings(
    nativeLinkStubs := true,
    addCompilerPlugin(
      "org.scala-native" % "junit-plugin" % nativeVersion cross CrossVersion.full
    ),
    mimaPreviousArtifacts := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, 1)) => mimaPreviousArtifacts.value.filter(_.revision != "2.6.0")
      case _            => mimaPreviousArtifacts.value
    }),
    Compile / unmanagedSourceDirectories += {
      val jsAndNativeSourcesParent = (ThisBuild / baseDirectory).value / "compat/jsNative/src/main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _) | (2, 13)) =>
          jsAndNativeSourcesParent / "scala-2.13"
        case _ =>
          jsAndNativeSourcesParent / "scala-2.11_2.12"
      }
    },
    libraryDependencies += "org.scala-native" %%% "junit-runtime" % nativeVersion,
    Test / fork := false // Scala Native cannot run forked tests
  )
)

val compat211 = compat(Seq(JSPlatform, JVMPlatform, NativePlatform), scala211)
val compat212 = compat(Seq(JSPlatform, JVMPlatform, NativePlatform), scala212)
val compat213 = compat(Seq(JSPlatform, JVMPlatform, NativePlatform), scala213)
val compat30  = compat(Seq(JSPlatform, JVMPlatform), scala30)
val compat31  = compat(Seq(JVMPlatform, NativePlatform), scala31)

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
lazy val compat31Native  = compat31.native

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
    versionPolicyIntention := Compatibility.None,
    versionCheck := {}, // I don't understand why this fails otherwise?! oh well
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

val ciScalaVersion = sys.env.get("CI_SCALA_VERSION").flatMap(Version.parse)
val isScalaJs      = sys.env.get("CI_PLATFORM") == Some("js")
val isScalaNative  = sys.env.get("CI_PLATFORM") == Some("native")
val isScalafix     = sys.env.get("CI_MODE") == Some("testScalafix")
val isScalafmt     = sys.env.get("CI_MODE") == Some("testScalafmt")
val isBinaryCompat = sys.env.get("CI_MODE") == Some("testBinaryCompat")
val isHeaderCheck  = sys.env.get("CI_MODE") == Some("headerCheck")
val jdkVersion     = sys.env.get("CI_JDK").map(_.toInt)

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
            "CI_PLATFORM",
            "CI_MODE",
            "CI_JDK",
          ).foreach(k =>
            println(k.padTo(20, " ").mkString("") + " -> " + sys.env.getOrElse(k, "None")))

          val platformSuffix = if (isScalaJs) "JS" else if (isScalaNative) "Native" else ""

          val compatProject       = s"compat${ciScalaVersion.get}$platformSuffix"
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

          Seq(
            List(s"""++${sys.env.get("CI_SCALA_VERSION").get}"""),
            List(s"$projectPrefix/clean"),
            List(s"$testProjectPrefix/test"),
            List(s"$projectPrefix/publishLocal"),
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
