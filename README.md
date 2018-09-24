[![Build Status](https://travis-ci.org/scala/scala-collection-compat.svg?branch=master)](https://travis-ci.org/scala/scala-collection-compat)

# Scala 2.13 Collection Compatibility Library And Migration Tool

## Compatibility Library

This library provides some of the new APIs from Scala 2.13 to Scala 2.11 and 2.12. It can be used to cross-build projects.
To use this library, add the following to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "0.2.1"
```

Version 0.2.1 is compatible with Scala 2.13.0-M5. For Scala 2.13.0-M4 you should use version 0.1.1.

Note that there are multiple ways to cross-build projects, see https://github.com/scala/collection-strawman/wiki/FAQ#how-do-i-cross-build-my-project-against-scala-212-and-scala-213.

Also note that this library has not fully stabilized yet. We expect that new, binary incompatible releases of this library will be published (for 2.11, 2.12) until Scala 2.13 is getting close to its final state. Therefore you might want to avoid adding a dependency on that library to your 2.11 / 2.12 artifacts for the time being.


The 2.13 collections are mostly backwards compatible, but there are some exceptions. For example, the `to` method is used with a type parameter in 2.12:

```scala
  xs.to[List]
```

With this compatibility library you can also use the 2.13 syntax which uses a companion object:

```scala
  import scala.collection.compat._
  xs.to(List)
```

The 2.13 version consists only of an empty `scala.collection.compat` package object that allows you to write `import scala.collection.compat._` in 2.13.
The 2.11/2.12 version has the compatibility extensions in this package.

The library also adds backported versions of new collection types, currently `scala.collection.compat.immutable.ArraySeq`. In 2.11/2.12, this type is a new collection implementation. In 2.13, it is an alias for `scala.collection.immutable.ArraySeq`.

## Migration Tool

We created two migration rules: 

* `Collection213Upgrade` For upgrading applications (like web server, etc) from 2.11/2.12 to 2.13
* `Collection213CrossCompat` For library that wants to cross compile to 2.11, 2.12 and 2.13

The migration rules use scalafix. Please see the [official installation instruction](https://scalacenter.github.io/scalafix/docs/users/installation.html) and, in particular, check that your exact Scala version is supported.

```scala
// project/plugins.sbt

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.0-RC1")
```

```scala
// build.sbt or project/Build.scala

// If you are using project/Build.scala add the following imports:
import scalafix.sbt.ScalafixPlugin.autoImport.{scalafixDependencies, scalafixSemanticdb}

val collectionCompatVersion = "0.2.1"
val collectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion

libraryDependencies += collectionCompat // required for Collection213CrossCompat
addCompilerPlugin(scalafixSemanticdb)
scalacOptions ++= List(
  "-Yrangepos",
  "-P:semanticdb:synthetics:on" // Required by the collection rewrites
)

scalafixDependencies in ThisBuild += "org.scala-lang.modules" %% "scala-collection-migrations" % collectionCompatVersion
```

Then run:

```bash
> ;scalafix Collection213Upgrade ;test:scalafix Collection213Upgrade # For Applications
# or
> ;scalafix Collection213CrossCompat ;test:scalafix Collection213CrossCompat # For Libraries
```

The migration tool is not exhaustive and we will continue to improve
it over time. If you encounter a use case that’s not supported, please
report it as described in the
[contributing documentation](CONTRIBUTING.md#migration-tool).
