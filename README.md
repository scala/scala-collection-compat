[![scala-collection-compat Scala version support](https://index.scala-lang.org/scala/scala-collection-compat/scala-collection-compat/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/scala/scala-collection-compat/scala-collection-compat)

## Purpose and scope

This library makes some Scala 2.13 APIs available on Scala 2.11 and 2.12.

The idea is to facilitate
[cross-building](https://github.com/scala/collection-strawman/wiki/FAQ#how-do-i-cross-build-my-project-against-scala-212-and-scala-213)
Scala 2.13 and 3.0 code on the older versions.

Although the name of the library is scala-"collection"-compat, we have now widened the scope to include other parts of the Scala 2.13/3.0 standard library besides just collections.

Only the most commonly used APIs are supported; many are missing. Contributions are welcome.

## Usage

To use this library, add the following to your `build.sbt`:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "<version>"
```

All future versions will remain backwards binary compatible with 2.0.0. (The 1.0.0 release was withdrawn and should not be used.)

## How it works

The 2.13 and 3.0 versions consist only of an empty `scala.collection.compat` package object, so `import scala.collection.compat._` won't cause an error in cross-compiled code.

The 2.11 and 2.12 versions have the needed compatibility code in this package.

### Changed methods

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

### New collections

The library also adds backported versions of new collection types, such as `immutable.ArraySeq` and `immutable.LazyList`. (On 2.13, these types are just aliases to standard library types.)

### New collection methods

Support is included for some 2.13 collections methods such as `maxOption`.

### Other new classes

Support is included for some non-collections classes, such as:

* `@nowarn` annotation, added in 2.13.2 and 2.12.13. (The 2.11 `@nowarn` doesn't do anything, but its presence facilitates cross-compilation.)

### Other new methods

Support is included for some other methods, such as:

* `toIntOption` (and `Long`, et al) on `String`

## Migration rules

The migration rules use scalafix. Please see the [official installation instructions](https://scalacenter.github.io/scalafix/docs/users/installation.html) if you are using an old version of sbt (<1.3) or in case the commands below do not work.

```scala
// project/plugins.sbt
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.1")
```

### Collection213Upgrade

The `Collection213Upgrade` rewrite upgrades to the 2.13 collections without the ability to compile the code-base with 2.12 or 2.11. This rewrite is suitable for applications that don't need to cross-compile against multiple Scala versions.

```scala
// build.sbt
scalacOptions += "-P:semanticdb:synthetics:on"
```

```bash
// sbt shell
> scalafixEnable
> scalafixAll dependency:Collection213Upgrade@org.scala-lang.modules:scala-collection-migrations:<version>
```

### Collection213CrossCompat

The `Collection213CrossCompat` rewrite upgrades to the 2.13 collections with the ability to compile the code-base with 2.12 or later. This rewrite is suitable for libraries that are cross-published for multiple Scala versions.

To cross-build for 2.12 and 2.11, the rewrite rule introduces a dependency on the scala-collection-compat module, which provides some APIs of 2.13 on 2.12 and 2.11. This enables you to write your library using the latest 2.13 collections API while still supporting users on an older Scala version.

```scala
// build.sbt
libraryDependencies +=  "org.scala-lang.modules" %% "scala-collection-compat" % "<version>"
scalacOptions += "-P:semanticdb:synthetics:on"
```


```bash
// sbt shell
> scalafixEnable
> scalafixAll dependency:Collection213CrossCompat@org.scala-lang.modules:scala-collection-migrations:<version>
```

### Fixing unused import warnings
In Scala 2.13 the `import scala.collection.compat._` sometimes is not needed (e.g. `.to(SeqType)` is natively available).
This leads to a `unused import` warning under Scala 2.13 even though the import is required for Scala 2.12.
In order to work around this, you can pass a compiler option to ignore this specific issue, e.g. in SBT:
```scala
scalacOptions += "-Wconf:origin=scala.collection.compat.*:s"
```

### Contributing

The migration tool is not exhaustive. Contributions of additional rewrites are welcome.  If you encounter a use case that’s not supported, please report it as described in the [contributing
documentation](CONTRIBUTING.md#migration-tool).
