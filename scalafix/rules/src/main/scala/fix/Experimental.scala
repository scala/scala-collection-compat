package fix

import scalafix.v0._
import scala.meta._

// 2.12 Cross-Compatible
case class Experimental(index: SemanticdbIndex) extends SemanticRule(index, "Experimental") {

  val CollectionMap = TypeMatcher(
    Symbol("_root_.scala.collection.immutable.Map#"),
    Symbol("_root_.scala.collection.mutable.Map#"),
    Symbol("_root_.scala.Predef.Map#")
  )
  val CollectionSet = TypeMatcher(
    Symbol("_root_.scala.collection.Set#")
  )

  // == Symbols ==
  val mapZip   = exact("_root_.scala.collection.IterableLike#zip().")
  val mapPlus  = exact("_root_.scala.collection.MapLike#`+`().")
  val setPlus  = exact("_root_.scala.collection.SetLike#`+`().")
  val setMinus = exact("_root_.scala.collection.SetLike#`-`().")

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
        } else {
          ctx.addLeft(rhs, col + "(") +
            ctx.addRight(rhs, ")")
        }

      ctx.addRight(op, doubleOp) + callSite
    }

    ctx.tree.collect {
      case ap @ Term.ApplyInfix(CollectionSet(), op @ setPlus(_), Nil, List(rhs)) =>
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
