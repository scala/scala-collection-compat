def scalafixVersion = _root_.scalafix.Versions.version

lazy val baseSettings = Seq(
  scalaVersion := _root_.scalafix.Versions.scala212
)

lazy val root = project
  .in(file("."))
  .settings(baseSettings)
  .aggregate(rules, input, output, tests)

lazy val rules = project
  .settings(baseSettings)
  .settings(
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
  )

lazy val input = project
.settings(baseSettings)
  .settings(
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= {
      val sourceroot = sourceDirectory.in(Compile).value / "scala"
      Seq(
        "-Yrangepos",
        s"-P:semanticdb:sourceroot:$sourceroot"
      )
    }
  )

lazy val output = project
  .settings(
    resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    scalaVersion := "2.13.0-M4"
  )

lazy val tests = project
  .settings(baseSettings)
  .settings(
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value
  )
  .dependsOn(input, rules)
  .enablePlugins(ScalafixTestkitPlugin)
