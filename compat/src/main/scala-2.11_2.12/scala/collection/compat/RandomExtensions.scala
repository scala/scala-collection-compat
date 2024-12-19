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

package scala.collection.compat

import scala.util.Random

final class RandomExtensions(private val self: Random) extends AnyVal {
  def nextLong(n: Long): Long = {
    require(n > 0, "n must be positive")

    var offset = 0L
    var _n = n

    while (_n >= Integer.MAX_VALUE) {
      val bits = self.nextInt(2)
      val halfn = _n >>> 1
      val nextn =
        if ((bits & 2) == 0) halfn
        else _n - halfn
      if ((bits & 1) == 0)
        offset += _n - nextn
      _n = nextn
    }
    offset + self.nextInt(_n.toInt)
  }
}
