Scala 2.13 Collection Compatibility Library
===========================================

This library for Scala 2.12 provides limited compatibility with the new collection library in 2.13. We try to keep the
2.13 collections as backward compatible as possible but that is not always possible. For some of these cases this
library allows you to compile sources written for a subset of the new semantics of 2.13 on 2.12.

For example, the `to` method is used with a type parameter in 2.12:

```scala
  xs.to[List]
```

With this compatibility library you can also use the 2.13 syntax which uses a companion object:

```scala
  import scala.collection.compat._
  xs.to(List)
```

This project can be cross-built on 2.13 (with new collections) and 2.12. The 2.13 version consists only of an
empty `scala.collection.compat` package object that allows you to write `import scala.collection.compat._` in 2.13. 
The 2.13 version has the compatibility extensions in this package. It also adds backported version of some new collection
types to other `scala.collection` subpackages.
