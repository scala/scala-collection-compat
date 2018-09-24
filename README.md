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

The migration rules use scalafix. Please see the [official installation instruction](https://scalacenter.github.io/scalafix/docs/users/installation.html) and, in particular, check that your full Scala version is supported (ex 2.12.6).

```scala
// project/plugins.sbt

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.0-RC1")
```

### Collection213Upgrade

The `Collection213Upgrade` rewrite upgrades to the 2.13 collections without the ability to compile the code-base with 2.12 or later. This rewrite is suitable for applications such as web servers that don't need to cross-compile against multiple Scala versions.

```scala
// build.sbt
scalafixDependencies += "org.scala-lang.modules" ...
scalacOptions ++= List("-Yrangepos", "-P:semanticdb:synthetics:on")
```

```bash
// sbt shell
> ;scalafix Collections213Upgrade;test:scalafix Collections213Upgrade
```

### Collections213CrossCompat


The `Collections213CrossCompat` rewrite upgrades to the 2.13 collections without the ability to compile the code-base with 2.12 or later. This rewrite is suitable for libraries that are cross-published for multiple Scala versions.

To cross-build for 2.12 and 2.11, an additional module `scala-collection-compat` is required to provide missing extensions methods. This enables you to write your library using the latest 2.13 collections API while still supporting users on an older Scala version.

// build.sbt
scalafixDependencies += "org.scala-lang.modules" %% "scala-collection-migrations" % "0.2.1"
libraryDependencies +=  "org.scala-lang.modules" %% "scala-collection-compat" % "0.2.1"
scalacOptions ++= List("-Yrangepos", "-P:semanticdb:synthetics:on")


```bash
// sbt shell
> ;scalafix Collections213CrossCompat;test:scalafix Collections213CrossCompat
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
