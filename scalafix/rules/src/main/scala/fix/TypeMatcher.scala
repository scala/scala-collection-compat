package fix

import scalafix._
import scalafix.util._
import scala.meta._

// WARNING: TOTAL HACK
// this is only to unblock us until Term.tpe is available: https://github.com/scalameta/scalameta/issues/1212
// if we have a simple identifier, we can look at his definition at query it's type
// this should be improved in future version of scalameta
object TypeMatcher {
  def apply(symbols: Symbol*)(implicit index: SemanticdbIndex): TypeMatcher =
    new TypeMatcher(symbols: _*)(index)
}

final class TypeMatcher(symbols: Symbol*)(implicit index: SemanticdbIndex) {
  def unapply(tree: Tree): Boolean = {
    index.denotation(tree)
         .exists(_.names.headOption.exists(n => symbols.exists(_ == n.symbol)))
  }
}
