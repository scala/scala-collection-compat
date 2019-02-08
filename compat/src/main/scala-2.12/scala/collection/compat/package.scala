/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.collection

import scala.collection.{mutable => m}

package object compat extends compat.PackageShared {
  implicit class MutableTreeMapExtensions2(private val fact: m.TreeMap.type) extends AnyVal {
    def from[K: Ordering, V](source: TraversableOnce[(K, V)]): m.TreeMap[K, V] =
      build(m.TreeMap.newBuilder[K, V], source)
  }

  implicit class MutableSortedMapExtensions(private val fact: m.SortedMap.type) extends AnyVal {
    def from[K: Ordering, V](source: TraversableOnce[(K, V)]): m.SortedMap[K, V] =
      build(m.SortedMap.newBuilder[K, V], source)
  }
}
