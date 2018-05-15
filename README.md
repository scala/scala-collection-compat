[![Build Status](https://travis-ci.org/scala/scala-collection-compat.svg?branch=master)](https://travis-ci.org/scala/scala-collection-compat)

# Scala 2.13 Collection Compatibility Library And Migration Tool

To use this library, add the following to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "0.1.0"
```

## Compatibility Library

This library provides some of the new APIs from Scala 2.13 to Scala 2.11 and 2.12. It can be used to cross-build projects.

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
It also adds backported version of some new collection types to other `scala.collection` subpackages.

## Migration Tool

A tool is being developed to automatically migrate code that uses the standard
collection to use the strawman.

To use it, add the [scalafix](https://scalacenter.github.io/scalafix/) sbt plugin
to your build, as explained in
[its documentation](https://scalacenter.github.io/scalafix/#Installation).

The migration tool is not exhaustive and we will continue to improve
it over time. If you encounter a use case that’s not supported, please
report it as described in the
[contributing documentation](CONTRIBUTING.md#migration-tool).

### Migrating a 2.12 code base to 2.13

Run the following sbt task on your project:

~~~
> scalafix github:scala/scala-collection-compat/NewCollections
~~~
