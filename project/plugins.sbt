if (System.getProperty("java.version").startsWith("1."))
  Seq()
else
  // override to version that works on Java 9,
  // see https://github.com/scala/sbt-scala-module/issues/35
  Seq(addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.3"))

val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("0.6.23")

addSbtPlugin("org.scala-js"           % "sbt-scalajs"              % scalaJSVersion)
addSbtPlugin("org.portable-scala"     % "sbt-scalajs-crossproject" % "0.5.0")
addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module"         % "1.0.14")
addSbtPlugin("ch.epfl.scala"          % "sbt-scalafix"             % "0.6.0-M15")
addSbtPlugin("com.eed3si9n"           % "sbt-buildinfo"            % "0.7.0")
addSbtPlugin("com.typesafe.sbt"       % "sbt-pgp"                  % "0.8.3")

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "2.20.0",
  "org.jsoup"              % "jsoup"        % "1.10.1"
)
