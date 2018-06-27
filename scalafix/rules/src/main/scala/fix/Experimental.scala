package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

case class Experimental(index: SemanticdbIndex) extends SemanticRule(index, "Experimental") {
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

  val CollectionMap: TypeMatcher = TypeMatcher(
    Symbol("_root_.scala.collection.immutable.Map#"),
    Symbol("_root_.scala.collection.mutable.Map#"),
    Symbol("_root_.scala.Predef.Map#")
  )

  val CollectionSet: TypeMatcher = TypeMatcher(Symbol("_root_.scala.collection.Set#"))

  val mapZip = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.IterableLike#zip(Lscala/collection/GenIterable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    )

  val mapPlus = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.MapLike#`+`(Lscala/Tuple2;)Lscala/collection/Map;.")
    )

  val setPlus = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.SetLike#`+`(Ljava/lang/Object;)Lscala/collection/Set;.")
    )

  val setMinus = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.SetLike#`-`(Ljava/lang/Object;)Lscala/collection/Set;.")
    )

  def startsWithParens(tree: Tree): Boolean = 
    tree.tokens.headOption.map(_.is[Token.LeftParen]).getOrElse(false)

  def replaceMapZip(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(Term.Select(CollectionMap(), mapZip(_)), List(_)) =>
        ctx.addRight(ap, ".toMap")
    }.asPatch
  }

  def replaceSetMapPlusMinus(ctx: RuleCtx): Patch = {
    def rewriteOp(op: Tree, rhs: Tree, doubleOp: String, col0: String): Patch = {
      val col = "_root_.scala.collection." + col0
      val callSite =
        if (startsWithParens(rhs)) {
          ctx.addLeft(rhs, col)          
        }
        else {
          ctx.addLeft(rhs, col + "(") +
          ctx.addRight(rhs, ")")
        }

      ctx.addRight(op, doubleOp) + callSite
    }

    ctx.tree.collect {
      case Term.ApplyInfix(CollectionSet(), op @ setPlus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "+", "Set")
        
      case Term.ApplyInfix(CollectionSet(), op @ setMinus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "-", "Set")

      case Term.ApplyInfix(_, op @ mapPlus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "+", "Map")
    }.asPatch
  }

  override def fix(ctx: RuleCtx): Patch =
    replaceSetMapPlusMinus(ctx) +
    replaceMapZip(ctx)
}