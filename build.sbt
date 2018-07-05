import ScalaModulePlugin._
import sbtcrossproject.{crossProject, CrossType}
import _root_.scalafix.Versions.{version => scalafixVersion, scala212 => scalafixScala212}

lazy val root = project
  .in(file("."))
  .settings(dontPublish)
  .aggregate(
    compatJVM, compatJS,
    scalafixRules, scalafixInput, scalafixTests,
    scalafixOutput212, scalafixOutput213
  )
  .disablePlugins(ScalafixPlugin)

// == Core Libraries ==

lazy val compat = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("compat"))
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
    OsgiKeys.exportPackage := Seq(s"scala.collection.compat.*;version=${version.value}"),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    javaHome := {
      val oldValue = javaHome.value
      val isOnCi = sys.env.get("CI").isDefined

      if (isOnCi) {
        // switch back to the jdk set by the build matrix
        val ciJavaHome =
          sys.env("TRAVIS_JDK_VERSION") match {
            case "openjdk6"   => "/usr/lib/jvm/java-6-openjdk-amd64"
            case "oraclejdk8" => "/usr/lib/jvm/java-8-oracle"
          }

        println(s"using JAVA_HOME: $ciJavaHome")
        Some(file(ciJavaHome))
      }
      else oldValue
    }
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
lazy val compatJS  = compat.js

lazy val scalafixRules = project
  .in(file("scalafix/rules"))
  .settings(scalaModuleSettings)
  .settings(scalaModuleSettingsJVM)
  .settings(
    name := "scala-collection-migrations",
    scalaVersion := scalafixScala212,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
  )

// == Scalafix Test Setup ==

lazy val scalafixInput = project
  .in(file("scalafix/input"))
  .settings(dontPublish)
  .settings(
    scalaVersion := scalafixScala212,
    scalafixSourceroot := sourceDirectory.in(Compile).value
  )

lazy val scalafixOutput212 = project
  .in(file("scalafix/output212"))
  .settings(scalaVersion := scalafixScala212)
  .settings(dontPublish)
  .dependsOn(compatJVM)

lazy val scalafixOutput213 = project
  .in(file("scalafix/output213"))
  .settings(scala213Settings)
  .settings(dontPublish)

lazy val scalafixOutput213Failure = project
  .in(file("scalafix/output213-failure"))
  .settings(scala213Settings)
  .settings(dontPublish)

lazy val scalafixTests = project
  .in(file("scalafix/tests"))
  .settings(dontPublish)
  .settings(
    scalaVersion := scalafixScala212,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    buildInfoPackage := "fix",
    buildInfoKeys := Seq[BuildInfoKey](
      "inputSourceroot" ->
        sourceDirectory.in(scalafixInput, Compile).value,
      "output212Sourceroot" ->
        sourceDirectory.in(scalafixOutput212, Compile).value,
      "output213Sourceroot" ->
        sourceDirectory.in(scalafixOutput213, Compile).value,
      "output213FailureSourceroot" ->
        sourceDirectory.in(scalafixOutput213Failure, Compile).value,
      "inputClassdirectory" ->
        classDirectory.in(scalafixInput, Compile).value
    ),
    test in Test := (test in Test).dependsOn(
      compile in (scalafixOutput212, Compile),
      compile in (scalafixOutput213, Compile)
    ).value
  )
  .dependsOn(scalafixInput, scalafixRules)
  .enablePlugins(BuildInfoPlugin)

lazy val dontPublish = Seq(
  publishArtifact := false,
  packagedArtifacts := Map.empty,
  publish := {},
  publishLocal := {}
)

lazy val scala212 = "2.12.6"
lazy val scala213 = "2.13.0-M4"

lazy val scala213Settings = Seq(
  resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
  scalaVersion := scala213
)

// required by sbt-scala-module
inThisBuild(Seq(
  crossScalaVersions := Seq(scala212, scala213, "2.11.12"),
  commands += Command.command("noop") { state =>
    println("noop")
    state
  }
))
