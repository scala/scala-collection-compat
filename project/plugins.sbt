val crossVer = "1.1.0"
val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("1.7.0")
val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).filter(_.nonEmpty).getOrElse("0.4.0")

addSbtPlugin("org.scala-js"           % "sbt-scalajs"                   % scalaJSVersion)
addSbtPlugin("org.portable-scala"     % "sbt-scalajs-crossproject"      % crossVer)
addSbtPlugin("org.scala-native"       % "sbt-scala-native"              % scalaNativeVersion)
addSbtPlugin("org.portable-scala"     % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module"              % "2.4.0")
addSbtPlugin("ch.epfl.scala"          % "sbt-scalafix"                  % "0.9.31")
addSbtPlugin("com.eed3si9n"           % "sbt-buildinfo"                 % "0.10.0")
