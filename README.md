[![Build Status](https://travis-ci.org/scala/scala-collection-compat.svg?branch=master)](https://travis-ci.org/scala/scala-collection-compat)

## Purpose and scope

This library makes some Scala 2.13 APIs available on Scala 2.11 and 2.12.

The idea is to facilitate
[cross-building](https://github.com/scala/collection-strawman/wiki/FAQ#how-do-i-cross-build-my-project-against-scala-212-and-scala-213)
Scala 2.13 code on the older versions.

Although the name of the library is scala-"collection"-compat, we have now widened the scope to include other parts of the Scala 2.13 standard library besides just collections.

Only the most commonly used APIs are supported; many are missing. Contributions are welcome.

## Usage

To use this library, add the following to your `build.sbt`:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.3.2"
```

All future versions will remain backwards binary compatible with 2.0.0. (The 1.0.0 release was withdrawn and should not be used.)

## How it works

The 2.13 collections redesign did not break source compatibility for most ordinary code, but there are some exceptions.

For example, the `to` method is used with a type parameter in 2.12:

```scala
xs.to[List]
```

With this compatibility library you can instead use the 2.13 syntax, which takes a value parameter:

```scala
import scala.collection.compat._
xs.to(List)
```

The 2.13 version consists only of an empty `scala.collection.compat` package object that allows you to write `import scala.collection.compat._` in 2.13.

The 2.11 and 2.12 versions have the needed compatibility code in this package.

The library also adds backported versions of new collection types, such as `immutable.ArraySeq` and `immutable.LazyList`. (On 2.13, these types are just aliases to standard library types.)

And it adds backported versions of some 2.13 collections methods such as `maxOption`.

And, it includes support for some non-collections classes such as the `@nowarn` annotation added in 2.13.2.

## Migration rules

The migration rules use scalafix. Please see the [official installation instructions](https://scalacenter.github.io/scalafix/docs/users/installation.html) and, in particular, check that your full Scala version is supported (ex 2.12.12).

```scala
// project/plugins.sbt
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.19")
```

### Collection213Upgrade

The `Collection213Upgrade` rewrite upgrades to the 2.13 collections without the ability to compile the code-base with 2.12 or 2.11. This rewrite is suitable for applications that don't need to cross-compile against multiple Scala versions.

```scala
// build.sbt
scalafixDependencies in ThisBuild += "org.scala-lang.modules" %% "scala-collection-migrations" % "2.3.2"
addCompilerPlugin(scalafixSemanticdb)
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
scalafixDependencies in ThisBuild += "org.scala-lang.modules" %% "scala-collection-migrations" % "2.3.1"
libraryDependencies +=  "org.scala-lang.modules" %% "scala-collection-compat" % "2.3.1"
addCompilerPlugin(scalafixSemanticdb)
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

The migration tool is not exhaustive. Contributions of additional rewrites are welcome.  If you encounter a use case that’s not supported, please report it as described in the [contributing
documentation](CONTRIBUTING.md#migration-tool).
