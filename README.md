[![Build Status](https://travis-ci.org/scala/scala-collection-compat.svg?branch=master)](https://travis-ci.org/scala/scala-collection-compat)

# Scala 2.13 Collection Compatibility Library And Migration Tool

## Compatibility Library

This library provides some of the new APIs from Scala 2.13 to Scala 2.11 and 2.12. It can be used to cross-build projects.
To use this library, add the following to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.1"
```

Version 2.0.0+ is compatible with Scala 2.13, 2.12, and 2.11.

Note that there are multiple ways to cross-build projects, see https://github.com/scala/collection-strawman/wiki/FAQ#how-do-i-cross-build-my-project-against-scala-212-and-scala-213.

**Note**: Please do not release any artifacts against version 1.0.0, due to [#195](https://github.com/scala/scala-collection-compat/issues/195).

Backwards binary compatibility will be enforced within each major version (i.e. all 1.x.y releases will be binary compatible).

The 2.13 collections are mostly backwards source-compatible, but there are some exceptions. For example, the `to` method is used with a type parameter in 2.12:

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

The migration rules use scalafix. Please see the [official installation instruction](https://scalacenter.github.io/scalafix/docs/users/installation.html) and, in particular, check that your full Scala version is supported (ex 2.12.8).

```scala
// project/plugins.sbt

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.5")
```

### Collection213Upgrade

The `Collection213Upgrade` rewrite upgrades to the 2.13 collections without the ability to compile the code-base with 2.12 or 2.11. This rewrite is suitable for applications that don't need to cross-compile against multiple Scala versions.

```scala
// build.sbt
scalafixDependencies in ThisBuild += "org.scala-lang.modules" %% "scala-collection-migrations" % "2.1.1"
scalacOptions ++= List("-Yrangepos", "-P:semanticdb:synthetics:on")
```

```bash
// sbt shell
> ;test:scalafix Collection213Upgrade ;scalafix Collection213Upgrade
```

### Collection213CrossCompat


The `Collection213CrossCompat` rewrite upgrades to the 2.13 collections with the ability to compile the code-base with 2.12 or later. This rewrite is suitable for libraries that are cross-published for multiple Scala versions.

To cross-build for 2.12 and 2.11, the rewrite rule introduces a dependency on the scala-collection-compat module, which provides the syntax of 2.13 on 2.12 and 2.11. This enables you to write your library using the latest 2.13 collections API while still supporting users on an older Scala version.

```scala
// build.sbt
scalafixDependencies in ThisBuild += "org.scala-lang.modules" %% "scala-collection-migrations" % "2.1.1"
libraryDependencies +=  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.1"
scalacOptions ++= List("-Yrangepos", "-P:semanticdb:synthetics:on")
```


```bash
// sbt shell
> ;test:scalafix Collection213CrossCompat ;scalafix Collection213CrossCompat
```

### Build.scala

```scala
// If you are using project/Build.scala add the following imports:
import scalafix.sbt.ScalafixPlugin.autoImport.{scalafixDependencies, scalafixSemanticdb}
```

### Contributing

The migration tool is not exhaustive and we will continue to improve
it over time. If you encounter a use case that’s not supported, please
report it as described in the
[contributing documentation](CONTRIBUTING.md#migration-tool).
