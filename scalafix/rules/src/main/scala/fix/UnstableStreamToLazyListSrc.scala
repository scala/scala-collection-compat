package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

/*
 * This rules is marked unstable since Stream is not strictly equivalent to LazyList.
 * LazyList has a lazy head but not Stream
 */
case class UnstableStreamToLazyList(index: SemanticdbIndex) extends SemanticRule(index, "UnstableStreamToLazyList") {

  val streamAppend = SymbolMatcher.normalized(
    Symbol("_root_.scala.collection.immutable.Stream.append.")
  )

  def replaceStreamAppend(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case streamAppend(t: Name) =>
        ctx.replaceTree(t, "lazyAppendedAll")
    }.asPatch
  }

  def replaceSymbols(ctx: RuleCtx): Patch = {
    ctx.replaceSymbols(
      "scala.Stream" -> "scala.LazyList",
      "scala.collection.immutable.Stream" -> "scala.collection.immutable.LazyList"
    )
  }

  override def fix(ctx: RuleCtx): Patch = {
    replaceStreamAppend(ctx) + replaceSymbols(ctx)
  }  
}
