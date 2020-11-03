val crossVer = "1.0.0"
val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("1.3.0")
val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).filter(_.nonEmpty).getOrElse("0.3.9")

addSbtPlugin("org.scala-js"           % "sbt-scalajs"                   % scalaJSVersion)
addSbtPlugin("org.portable-scala"     % "sbt-scalajs-crossproject"      % crossVer)
addSbtPlugin("org.scala-native"       % "sbt-scala-native"              % scalaNativeVersion)
addSbtPlugin("org.portable-scala"     % "sbt-scala-native-crossproject" % crossVer)
addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module"              % "2.2.2")
addSbtPlugin("ch.epfl.scala"          % "sbt-scalafix"                  % "0.9.21")
addSbtPlugin("com.eed3si9n"           % "sbt-buildinfo"                 % "0.10.0")
