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

package fix

import scala.language.higherKinds

import collection.generic.CanBuildFrom

object CanBuildFromNegSrc213Failure {

  // negative test
  def g[C0, A, C1[_]](c0: C0)(implicit cbf3: CanBuildFrom[C0, A, C1[A]]): C1[A] = {
    cbf3().result()
  }
}
