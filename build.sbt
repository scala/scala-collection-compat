organization := "org.scala-lang"

name := "scala-collection-compat"

version := "0.1-SNAPSHOT"

resolvers += "scala-pr" at "https://scala-ci.typesafe.com/artifactory/scala-integration/"

unmanagedSourceDirectories in Compile += (
  if(scalaVersion.value.startsWith("2.13.")) (sourceDirectory in Compile).value / "scala-2.13"
  else (sourceDirectory in Compile).value / "scala-2.11_2.12"
)

crossScalaVersions := Seq("2.12.5", "2.13.0-pre-b11db01", "2.11.12")

scalaVersion := crossScalaVersions.value.head

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
