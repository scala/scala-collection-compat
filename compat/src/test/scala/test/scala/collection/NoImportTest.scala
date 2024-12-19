/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package test.scala.collection

// Don't import scala.collection.compat._
import scala.collection.compat.Factory
import scala.collection.{mutable, immutable}

class NoImportTest {

  implicitly[Factory[Int, List[Int]]]
  implicitly[Factory[Char, String]]
  implicitly[Factory[Char, Array[Char]]]
  implicitly[Factory[Int, collection.BitSet]]
  implicitly[Factory[Int, mutable.BitSet]]
  implicitly[Factory[Int, immutable.BitSet]]

}
