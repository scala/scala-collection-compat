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

package fix.conversions

import scala.collection.{immutable => i, mutable => m, concurrent => conc}
import java.{lang => jl, util => ju}, java.util.{concurrent => juc}

trait JavaConversionsDataSrc {
  val iterable = List(1).toIterable
  val concMap: conc.Map[Int, Int] = ???
  val juIterator: ju.Iterator[Int] = ???
  val juEnumeration: ju.Enumeration[Int] = ???
  val jlIterable: jl.Iterable[Int] = ???
  val juCollection: ju.Collection[Int] = ???
  val juList: ju.List[Int] = ???
  val juSet: ju.Set[Int] = ???
  val juMap: ju.Map[Int, Int] = ???
  val jucConcurrentMap: juc.ConcurrentMap[Int, Int] = ???
  val juDictionary: ju.Dictionary[Int, Int] = ???
  val juProperties: ju.Properties = ???
}
