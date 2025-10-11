val scalaJSVersion =
  sys.env.get("CI_SCALAJS_VERSION").filter(_.nonEmpty).getOrElse("1.20.1")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.8")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "3.3.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.4")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
