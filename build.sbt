import sbtcrossproject.{crossProject, CrossType}

inThisBuild(Def.settings(
  organization := "org.scala-lang",
  version := "0.1-SNAPSHOT",
  resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
  crossScalaVersions := Seq("2.12.5", "2.13.0-pre-b11db01", "2.11.12"),
  scalaVersion := crossScalaVersions.value.head
))

lazy val `scala-collection-compat` = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    name := "scala-collection-compat",
    unmanagedSourceDirectories in Compile += {
      val sharedSourceDir = baseDirectory.value.getParentFile / "src/main"
      if (scalaVersion.value.startsWith("2.13.")) sharedSourceDir / "scala-2.13"
      else sharedSourceDir / "scala-2.11_2.12"
    }
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val `scala-collection-compatJVM` = `scala-collection-compat`.jvm
lazy val `scala-collection-compatJS` = `scala-collection-compat`.js
