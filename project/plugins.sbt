val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("0.6.26")

addSbtPlugin("org.scala-js"           % "sbt-scalajs"              % scalaJSVersion)
addSbtPlugin("org.portable-scala"     % "sbt-scalajs-crossproject" % "0.6.0")
addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module"         % "2.0.0")
addSbtPlugin("ch.epfl.scala"          % "sbt-scalafix"             % "0.9.4")
addSbtPlugin("com.eed3si9n"           % "sbt-buildinfo"            % "0.7.0")
addSbtPlugin("com.jsuereth"           % "sbt-pgp"                  % "2.0.0-M2")
addSbtPlugin("de.heikoseeberger"      % "sbt-header"               % "5.0.0")
