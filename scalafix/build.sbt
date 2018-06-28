def scalafixVersion = _root_.scalafix.Versions.version
inScope(Global)(
  List(
    scalaVersion := _root_.scalafix.Versions.scala212
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(
    rules, input, output212, output213, output213Failure, tests
  )

lazy val rules = project.settings(
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
)

lazy val input = project
  .settings(
    scalafixSourceroot := sourceDirectory.in(Compile).value
  )

lazy val output212 = project

lazy val output213 = project
  .settings(
    resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    scalaVersion := "2.13.0-M4"
  )

lazy val output213Failure = project.in(file("output213-failure"))
  .settings(
    resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    scalaVersion := "2.13.0-M4"
  )

lazy val tests = project
  .settings(
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
    buildInfoPackage := "fix",
    buildInfoKeys := Seq[BuildInfoKey](
      "inputSourceroot" ->
        sourceDirectory.in(input, Compile).value,
      "output212Sourceroot" ->
        sourceDirectory.in(output212, Compile).value,
      "output213Sourceroot" ->
        sourceDirectory.in(output213, Compile).value,
      "output213FailureSourceroot" ->
        sourceDirectory.in(output213Failure, Compile).value,
      "inputClassdirectory" ->
        classDirectory.in(input, Compile).value
    ),
    test in Test := (test in Test).dependsOn(
      compile in (output212, Compile),
      compile in (output213, Compile)
    ).value
  )
  .dependsOn(input, rules)
  .enablePlugins(BuildInfoPlugin)
