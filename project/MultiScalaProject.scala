import sbt._
import sbt.Keys._

import sbtcrossproject.{Platform, CrossProject}
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._

import java.io.File

trait MultiScala {
  def majorMinor(in: String): String = {
    val Array(major, minor, _) = in.split("\\.")
    major + minor
  }

  def projectIdPerScala(name: String, scalaV: String): String = s"$name${majorMinor(scalaV)}"

  def srcFull(base: String): Seq[Def.Setting[_]] = {
    Seq(
      unmanagedSourceDirectories in Compile +=
        (baseDirectory in ThisBuild).value / base / "src" / "main" / "scala",
      unmanagedSourceDirectories in Compile +=
        (baseDirectory in ThisBuild).value / base / "src" / "main" / ("scala-" + scalaBinaryVersion.value),
      unmanagedSourceDirectories in Test +=
        (baseDirectory in ThisBuild).value / base / "src" / "test" / "scala",
      unmanagedResourceDirectories in Compile +=
        (baseDirectory in ThisBuild).value / base / "src" / "main" / "resources",
      unmanagedResourceDirectories in Test +=
        (baseDirectory in ThisBuild).value / base / "src" / "test" / "resources"
    )
  }
}

object MultiScalaCrossProject {
  def apply(platforms: Platform*)(
      name: String,
      configure: CrossProject => CrossProject): MultiScalaCrossProject =
    new MultiScalaCrossProject(platforms, name, configure)
}

class MultiScalaCrossProject(
    platforms: Seq[Platform],
    name: String,
    configure: CrossProject => CrossProject)
    extends MultiScala {
  def apply(
      scalaV: String,
      configurePerScala: CrossProject => CrossProject = x => x
  ): CrossProject = {
    val projectId = projectIdPerScala(name, scalaV)
    val resultingProject =
      CrossProject(
        id = projectId,
        base = file(s".cross/$projectId")
      )(platforms: _*)
        .crossType(CrossType.Full)
        .withoutSuffixFor(JVMPlatform)
        .settings(
          scalaVersion := scalaV,
          moduleName := name
        )
        .settings(srcFull(name))

    configurePerScala(configure(resultingProject))
  }
}

object MultiScalaProject {
  def apply(name: String, configure: Project => Project): MultiScalaProject =
    new MultiScalaProject(name, s"scalafix-$name", configure)

  def apply(
      name: String,
      base: String,
      configure: Project => Project): MultiScalaProject =
    new MultiScalaProject(name, base, configure)
}

class MultiScalaProject(
    name: String,
    base: String,
    configure: Project => Project)
    extends MultiScala {

  def srcMain: String = s"$base/src/main"

  def apply(
      scalaV: String,
      configurePerScala: Project => Project = x => x): Project = {
    val fullName = s"scalafix-$name"
    val projectId = projectIdPerScala(name, scalaV)
    val resultingProject =
      Project(id = projectId, base = file(s".cross/$projectId"))
        .settings(
          scalaVersion := scalaV,
          moduleName := fullName
        )
        .settings(srcFull(base))

    configurePerScala(configure(resultingProject))
  }
}

object TestProject {
  private def base(sub: String): String =
    s"scalafix/$sub"

  def apply(
      sub: String,
      configure: (Project, String) => Project): MultiScalaProject =
    apply(sub, project => configure(project, s"${base(sub)}/src/main"))

  def apply(sub: String, configure: Project => Project): MultiScalaProject =
    MultiScalaProject(
      s"tests${sub.capitalize}",
      base(sub),
      configure.andThen(_.disablePlugins(scalafix.sbt.ScalafixPlugin))
    )

}
