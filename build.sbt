def scalafixVersion = _root_.scalafix.Versions.version
inScope(Global)(
  List(
    scalaVersion := _root_.scalafix.Versions.scala212
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(
    rules, input, output, tests
  )

lazy val rules = project.settings(
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
)

lazy val input = project
  .settings(
    scalafixSourceroot := sourceDirectory.in(Compile).value
  )

lazy val output = project
  .settings(
    resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    scalaVersion := "2.13.0-M4-pre-20d3c21"
  )

lazy val tests = project
  .settings(
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    buildInfoPackage := "fix",
    buildInfoKeys := Seq[BuildInfoKey](
      "inputSourceroot" ->
        sourceDirectory.in(input, Compile).value,
      "outputSourceroot" ->
        sourceDirectory.in(output, Compile).value,
      "inputClassdirectory" ->
        classDirectory.in(input, Compile).value
    )
  )
  .dependsOn(input, rules)
  .enablePlugins(BuildInfoPlugin)
