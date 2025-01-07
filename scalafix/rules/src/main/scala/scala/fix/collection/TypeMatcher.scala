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

package scala.fix.collection

import scalafix.v0._
import scala.meta._

// WARNING: TOTAL HACK
// this is only to unblock us until Term.tpe is available: https://github.com/scalameta/scalameta/issues/1212
// if we have a simple identifier, we can look at his definition at query it's type
// this should be improved in future version of scalameta
object TypeMatcher {
  def apply(symbols: Symbol*)(implicit index: SemanticdbIndex): TypeMatcher =
    new TypeMatcher(symbols: _*)(index)
}

// Does not work anymore: https://github.com/scalacenter/scalafix/issues/792
final class TypeMatcher(symbols: Symbol*)(implicit index: SemanticdbIndex) {
  def unapply(tree: Tree): Boolean = {
    index
      .denotation(tree)
      .exists(_.names.headOption.exists(n => symbols.exists(_ == n.symbol)))
  }
}
