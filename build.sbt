import ScalaModulePlugin._
import sbtcrossproject.{crossProject, CrossType}
import _root_.scalafix.internal.sbtscalafix.BuildInfo.{
  scalafixVersion,
  scala212 => scalafixScala212
}

lazy val root = project
  .in(file("."))
  .settings(dontPublish)
  .aggregate(
    compat211JVM,
    compat211JS,
    compat212JVM,
    compat212JS,
    compat213JVM,
    compat213JS,
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

lazy val scala211   = "2.11.12"
lazy val scala212   = "2.12.6"
lazy val scalaJs213 = "2.13.0-M4" // Scala.js does no have -pre

lazy val scala213 = "2.13.0-M4"
// lazy val scala213 = "2.13.0-pre-3ae6282" // use the sbt command `latest-213` to fetch the latest version

lazy val scala213Settings = Seq(
  resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
  scalaVersion := scala213
)

lazy val compat = MultiScalaCrossProject(JSPlatform, JVMPlatform)(
  "compat",
  _.settings(scalaModuleSettings)
    .jvmSettings(scalaModuleSettingsJVM)
    .settings(
      name := "scala-collection-compat",
      moduleName := "scala-collection-compat",
      version := "0.2.0-SNAPSHOT",
      scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions"),
      unmanagedSourceDirectories in Compile += {
        val sharedSourceDir = (baseDirectory in ThisBuild).value / "compat/src/main"
        if (scalaVersion.value.startsWith("2.13.")) sharedSourceDir / "scala-2.13"
        else sharedSourceDir / "scala-2.11_2.12"
      }
    )
    .jvmSettings(
      OsgiKeys.exportPackage := Seq(s"scala.collection.compat.*;version=${version.value}"),
      junit,
      javaHome in Compile := {
        val oldValue = (javaHome in Compile).value
        val isOnCi   = sys.env.get("CI").isDefined

        if (isOnCi) {
          // switch back to the jdk set by the build matrix
          val ciJavaHome =
            sys.env("TRAVIS_JDK_VERSION") match {
              case "openjdk6"   => "/usr/lib/jvm/java-6-openjdk-amd64"
              case "oraclejdk8" => "/usr/lib/jvm/java-8-oracle"
            }
          println(s"using JAVA_HOME: $ciJavaHome")
          Some(file(ciJavaHome))
        } else oldValue
      },
      javaHome in Test := (javaHome in Compile).value
    )
    .jsSettings(
      scalacOptions += {
        val x = (baseDirectory in LocalRootProject).value.toURI.toString
        val y = "https://raw.githubusercontent.com/scala/scala-collection-compat/" + sys.process
          .Process("git rev-parse HEAD")
          .lines_!
          .head
        s"-P:scalajs:mapSourceURI:$x->$y/"
      },
      fork in Test := false // Scala.js cannot run forked tests
    )
    .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
    .disablePlugins(ScalafixPlugin)
)

val compat211 = compat(scala211)
val compat212 = compat(scala212)
val compat213 = compat(scala213, scalaJs213, _.jvmSettings(scala213Settings))

lazy val compat211JVM = compat211.jvm
lazy val compat211JS  = compat211.js
lazy val compat212JVM = compat212.jvm
lazy val compat212JS  = compat212.js
lazy val compat213JVM = compat213.jvm
lazy val compat213JS  = compat213.js

lazy val `binary-compat-old` = project
  .in(file("binary-compat/old"))
  .settings(scalaVersion := scala212)
  .disablePlugins(ScalafixPlugin)

lazy val `binary-compat-new` = project
  .in(file("binary-compat/new"))
  .settings(scalaVersion := scala212)
  .dependsOn(compat212JVM)
  .disablePlugins(ScalafixPlugin)

lazy val `binary-compat` = project
  .in(file("binary-compat/test"))
  .settings(
    scalaVersion := scala212,
    libraryDependencies += "com.typesafe" %% "mima-reporter" % "0.3.0" % Test,
    junit,
    buildInfoPackage := "build",
    buildInfoKeys := Seq[BuildInfoKey](
      "oldClasspath" -> (classDirectory in (`binary-compat-old`, Compile)).value.toString,
      "newClasspath" -> (classDirectory in (`binary-compat-new`, Compile)).value.toString
    ),
    test in Test := (test in Test)
      .dependsOn(
        compile in (`binary-compat-old`, Compile),
        compile in (`binary-compat-new`, Compile)
      )
      .value
  )
  .enablePlugins(BuildInfoPlugin)
  .disablePlugins(ScalafixPlugin)

lazy val `scalafix-rules` = project
  .in(file("scalafix/rules"))
  .settings(
    organization := (organization in compat212JVM).value,
    version := (version in compat212JVM).value,
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
lazy val `scalafix-data` = MultiScalaProject("scalafix-data",
                                             "scalafix/data",
                                             _.settings(sharedScalafixSettings)
                                               .settings(dontPublish))

val `scalafix-data211` = `scalafix-data`(scala211, _.dependsOn(compat211JVM))
val `scalafix-data212` = `scalafix-data`(scalafixScala212, _.dependsOn(compat212JVM))
val `scalafix-data213` =
  `scalafix-data`(scala213, _.settings(scala213Settings).dependsOn(compat213JVM))

lazy val `scalafix-input` = project
  .in(file("scalafix/input"))
  .settings(sharedScalafixSettings)
  .settings(dontPublish)
  .settings(
    scalaVersion := scalafixScala212,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
  .dependsOn(`scalafix-data212`)

val `scalafix-output` = MultiScalaProject("scalafix-output",
                                          "scalafix/output",
                                          _.settings(sharedScalafixSettings)
                                            .settings(dontPublish)
                                            .disablePlugins(ScalafixPlugin))

lazy val outputCross =
  Def.setting((baseDirectory in ThisBuild).value / "scalafix/output/src/main/scala")

lazy val output212 =
  Def.setting((baseDirectory in ThisBuild).value / "scalafix/output212/src/main/scala")
lazy val addOutput212 = unmanagedSourceDirectories in Compile += output212.value

lazy val output212Plus =
  Def.setting((baseDirectory in ThisBuild).value / "scalafix/output212+/src/main/scala")
lazy val addOutput212Plus = unmanagedSourceDirectories in Compile += output212Plus.value

lazy val output213 =
  Def.setting((baseDirectory in ThisBuild).value / "scalafix/output213/src/main/scala")
lazy val addOutput213 = unmanagedSourceDirectories in Compile += output213.value

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
    .settings(scala213Settings)
    .dependsOn(`scalafix-data213`)
)

lazy val `scalafix-output213-failure` = project
  .in(file("scalafix/output213-failure"))
  .settings(sharedScalafixSettings)
  .settings(scala213Settings)
  .settings(dontPublish)

lazy val `scalafix-tests` = project
  .in(file("scalafix/tests"))
  .settings(sharedScalafixSettings)
  .settings(dontPublish)
  .settings(
    scalaVersion := scalafixScala212,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories := Seq(outputCross.value,
                                                  output212.value,
                                                  output212Plus.value,
                                                  output213.value),
    scalafixTestkitInputSourceDirectories := sourceDirectories.in(`scalafix-input`, Compile).value,
    scalafixTestkitInputClasspath := fullClasspath.in(`scalafix-input`, Compile).value
  )
  .dependsOn(`scalafix-input`, `scalafix-rules`)
  .enablePlugins(BuildInfoPlugin, ScalafixTestkitPlugin)

lazy val dontPublish = Seq(
  publishArtifact := false,
  packagedArtifacts := Map.empty,
  publish := {},
  publishLocal := {}
)

val preRelease         = "pre-release"
val travisScalaVersion = sys.env.get("TRAVIS_SCALA_VERSION").flatMap(Version.parse)
val releaseVersion     = sys.env.get("TRAVIS_TAG").flatMap(Version.parse)
val isScalaJs          = sys.env.get("SCALAJS_VERSION").nonEmpty
val isScalafix         = sys.env.get("TEST_SCALAFIX").nonEmpty
val isScalafmt         = sys.env.get("TEST_SCALAFMT").nonEmpty
val isBinaryCompat     = sys.env.get("TEST_BINARY_COMPAT").nonEmpty
val isRelease          = releaseVersion.nonEmpty

val releaseCredentials =
  if (isRelease) {
    def env(key: String): String = Option(System.getenv(key)).getOrElse("")

    Seq(
      pgpPassphrase := Some(env("PGP_PASSPHRASE").toArray),
      pgpPublicRing := file("admin/pubring.asc"),
      pgpSecretRing := file("admin/secring.asc"),
      credentials += Credentials("Sonatype Nexus Repository Manager",
                                 "oss.sonatype.org",
                                 env("SONA_USER"),
                                 env("SONA_PASS"))
    )
  } else {
    Seq()
  }

inThisBuild(releaseCredentials)

// required by sbt-scala-module
inThisBuild(
  Seq(
    crossScalaVersions := Seq(scala211, scala212, scala213),
    commands += Command.command("latest-213") { state =>
      LatestScala.printLatestScala213()
      state
    },
    commands += Command.command(preRelease) { state =>
      // Show Compat version, Scala version, and Java Version
      val jvmVersion = Version.parse(sys.props("java.specification.version")).get.minor
      val tagVersion = releaseVersion.get
      println(
        s"Releasing $tagVersion with Scala ${travisScalaVersion.get} on Java version $jvmVersion.")

      // Copy pgp stuff
      "admin/pre-release.sh" ! state.globalLogging.full

      state
    },
    commands += Command.command("scalafmt-test") { state =>
      Seq("admin/scalafmt.sh", "--test") ! state.globalLogging.full
      state
    },
    commands += Command.command("scalafmt") { state =>
      Seq("admin/scalafmt.sh") ! state.globalLogging.full
      state
    },
    commands += Command.command("ci") { state =>
      val toRun =
        if (isScalafmt) {
          Seq("scalafmt-test")
        } else {
          val platformSuffix = if (isScalaJs) "JS" else ""

          val compatProject       = "compat" + travisScalaVersion.get.binary + platformSuffix
          val binaryCompatProject = "binary-compat"

          val testProjectPrefix =
            if (isScalafix) {
              "scalafix-tests"
            } else if (isBinaryCompat) {
              binaryCompatProject
            } else {
              compatProject
            }

          val projectPrefix =
            if (isScalafix) {
              "scalafix-rules"
            } else if (isBinaryCompat) {
              binaryCompatProject
            } else {
              compatProject
            }

          val setPublishVersion =
            releaseVersion.map("set every version := " + _).toList

          val publishTask =
            if (releaseVersion.nonEmpty) {
              List(
                preRelease,
                s"$projectPrefix/publish-signed"
              )
            } else {
              Nil
            }

          Seq(
            setPublishVersion,
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

      toRun ::: state
    }
  ))
