import sbt._
import sbt.Keys._

import sbtcrossproject.{Platform, CrossProject}
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
import scalanativecrossproject.ScalaNativeCrossPlugin.autoImport._

import java.io.File

/** MultiScalaCrossProject and MultiScalaProject are an alternative to crossScalaVersion
 * it allows you to create a template for a sbt project you can instanciate with a
 * specific scala version.
 *
 * {{{
 * // create a project template
 * val myProject = MultiScalaProject(
 *   "name",
 *   "path/to/dir",
 *   _.settings(...) // Project => Project (scala version independent configurations)
 * )
 *
 * // instanciate a sbt project
 * lazy val myProject211 = myProject("2.11.12", _.settings(...) /* scala version dependent configurations */)
 * lazy val myProject212 = myProject("2.12.14" , _.settings(...))
 * // ...
 * }}}
 */
trait MultiScala {
  def majorMinor(in: String): String = {
    val Array(major, minor, _) = in.split("\\.")
    major + minor
  }

  def projectIdPerScala(name: String, scalaV: String): String = s"$name${majorMinor(scalaV)}"

  def srcFull(base: String): Seq[Def.Setting[_]] = {
    Seq(
      Compile / unmanagedSourceDirectories +=
        (ThisBuild / baseDirectory).value / base / "src" / "main" / "scala",
      Compile / unmanagedSourceDirectories +=
        (ThisBuild / baseDirectory).value / base / "src" / "main" / ("scala-" + scalaBinaryVersion.value),
      Compile / unmanagedSourceDirectories ++= {
        val sourceDir = (ThisBuild / baseDirectory).value / base / "src" / "main"
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, n)) if n >= 12 => List(sourceDir / "scala-2.12+")
          case Some((3, _))            => List(sourceDir / "scala-2.13")
          case _                       => Nil
        }
      },
      Test / unmanagedSourceDirectories +=
        (ThisBuild / baseDirectory).value / base / "src" / "test" / "scala",
      Compile / unmanagedResourceDirectories +=
        (ThisBuild / baseDirectory).value / base / "src" / "main" / "resources",
      Test / unmanagedResourceDirectories +=
        (ThisBuild / baseDirectory).value / base / "src" / "test" / "resources"
    )
  }
}

class MultiScalaCrossProject(name: String,
                             configureCommonJvm: CrossProject => CrossProject,
                             configureJs: CrossProject => CrossProject,
                             configureNative: CrossProject => CrossProject)
    extends MultiScala {
  def apply(
      platforms: Seq[Platform],
      scalaV: String
  ): CrossProject = {
    val hasJs     = platforms.contains(JSPlatform)
    val hasNative = platforms.contains(NativePlatform)
    val projectId = projectIdPerScala(name, scalaV)
    val res =
      CrossProject(
        id = projectId,
        base = file(s".cross/$projectId")
      )(platforms: _*)
        .crossType(CrossType.Full)
        .withoutSuffixFor(JVMPlatform)
        .settings(moduleName := name)
        .settings(scalaVersion := scalaV)
        .settings(srcFull(name))

    val conf = configureCommonJvm
      .andThen(if (hasJs) configureJs else identity)
      .andThen(if (hasNative) configureNative else identity)
    conf(res)
  }
}

object MultiScalaProject {
  def apply(name: String, configure: Project => Project): MultiScalaProject =
    new MultiScalaProject(name, s"scalafix-$name", configure)

  def apply(name: String, base: String, configure: Project => Project): MultiScalaProject =
    new MultiScalaProject(name, base, configure)
}

class MultiScalaProject(name: String, base: String, configure: Project => Project)
    extends MultiScala {

  def srcMain: String = s"$base/src/main"

  def apply(scalaV: String, configurePerScala: Project => Project = x => x): Project = {
    val fullName  = s"scalafix-$name"
    val projectId = projectIdPerScala(name, scalaV)
    val resultingProject =
      Project(id = projectId, base = file(s".cross/$projectId"))
        .settings(
          moduleName := fullName,
          scalaVersion := scalaV
        )
        .settings(srcFull(base))

    configurePerScala(configure(resultingProject))
  }
}
