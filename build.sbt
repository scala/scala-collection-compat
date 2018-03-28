organization := "org.scala-lang"

name := "scala-collection-compat"

version := "0.1-SNAPSHOT"

resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/"

unmanagedSourceDirectories in Compile ++=
  (if(scalaVersion.value.startsWith("2.13.")) Seq((sourceDirectory in Compile).value / "scala-2.13") else Seq())

crossScalaVersions := Seq("2.12.4", "2.13.0-pre-c577876")

scalaVersion := crossScalaVersions.value.head

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
