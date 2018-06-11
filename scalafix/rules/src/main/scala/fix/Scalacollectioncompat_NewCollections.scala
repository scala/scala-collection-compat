package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

case class Scalacollectioncompat_NewCollections(index: SemanticdbIndex)
  extends SemanticRule(index, "Scalacollectioncompat_NewCollections") {

  def replaceSymbols(ctx: RuleCtx): Patch = {
    ctx.replaceSymbols(
      "scala.Stream" -> "scala.LazyList",
      "scala.collection.immutable.Stream" -> "scala.collection.immutable.LazyList",
      "scala.Traversable" -> "scala.Iterable",
      "scala.collection.Traversable" -> "scala.collection.Iterable",
      "scala.TraversableOnce" -> "scala.IterableOnce",
      "scala.collection.TraversableOnce" -> "scala.collection.IterableOnce"
    )
  }

  val toTpe = SymbolMatcher.normalized(
    Symbol("_root_.scala.collection.TraversableLike.to.")
  )
  val iterator = SymbolMatcher.normalized(
    Symbol("_root_.scala.collection.TraversableLike.toIterator.")
  )
  val tupleZipped = SymbolMatcher.normalized(
    Symbol("_root_.scala.runtime.Tuple2Zipped.Ops.zipped."),
    Symbol("_root_.scala.runtime.Tuple3Zipped.Ops.zipped.")
  )

  val retain = 
    SymbolMatcher.normalized(
      Symbol("_root_.scala.collection.mutable.MapLike.retain.")
    )

  object CompanionMatcher {
    val companionSymbol = 
      SymbolMatcher.normalized(
        Symbol("_root_.scala.collection.generic.GenericCompanion.")
      )

    def isCompanion(denot: Denotation): Boolean = 
      denot.name == "companion" && 
        denot.names.exists(name =>
          companionSymbol.matches(name.symbol)
        )

    def unapply(tree: Tree): Option[Tree] = {
      for {
        sym <- index.symbol(tree)
        denot <- sym.denotation
        if (isCompanion(denot))
      } yield tree
    }
  }

  def replaceCompanion(ctx: RuleCtx) =
    ctx.tree.collect {
      case CompanionMatcher(t: Name) =>
        ctx.replaceTree(t, "iterableFactory")
    }.asPatch

  def replaceMutableMap(ctx: RuleCtx) =
    ctx.tree.collect {
      case Term.Apply(Term.Select(_, retain(n: Name)), List(_: Term.PartialFunction)) =>
        ctx.replaceTree(n, "filterInPlace")

      case Term.Apply(Term.Select(_, retain(n: Name)), List(_: Term.Function)) =>
        (for {
          name <- n.tokens.lastOption
          open <- ctx.tokenList.find(name)(t => t.is[Token.LeftParen])
          close <- ctx.matchingParens.close(open.asInstanceOf[Token.LeftParen])
        } yield
          ctx.replaceToken(open, "{case ") +
          ctx.replaceToken(close, "}") +
          ctx.replaceTree(n, "filterInPlace")  
        ).asPatch   
    }.asPatch

  def replaceToList(ctx: RuleCtx) =
    ctx.tree.collect {
      case iterator(t: Name) =>
        ctx.replaceTree(t, "iterator")
      case toTpe(n: Name) =>
        (for {
          name <- n.tokens.lastOption
          open <- ctx.tokenList.find(name)(t => t.is[Token.LeftBracket])
          close <- ctx.matchingParens.close(open.asInstanceOf[Token.LeftBracket])
        } yield
          ctx.replaceToken(open, "(") +
            ctx.replaceToken(close, ")")
        ).asPatch
    }.asPatch

  def replaceTupleZipped(ctx: RuleCtx) =
    ctx.tree.collect {
      case tupleZipped(Term.Select(Term.Tuple(args), name)) =>
        val removeTokensPatch =
          (for {
            zipped <- name.tokens.headOption
            closeTuple <- ctx.tokenList.leading(zipped).find(_.is[Token.RightParen])
            openTuple <- ctx.matchingParens.open(closeTuple.asInstanceOf[Token.RightParen])
            maybeDot = ctx.tokenList.slice(closeTuple, zipped).find(_.is[Token.Dot])
          } yield {
            ctx.removeToken(openTuple) +
              maybeDot.map(ctx.removeToken).asPatch +
              ctx.removeToken(zipped)
          }).asPatch

        def removeSurroundingWhiteSpaces(tk: Token) =
          (ctx.tokenList.trailing(tk).takeWhile(_.is[Token.Space]).map(ctx.removeToken) ++
            ctx.tokenList.leading(tk).takeWhile(_.is[Token.Space]).map(ctx.removeToken)).asPatch

        val commas =
          for {
            (prev, next) <- args.zip(args.tail)
            tokensBetweenArgs = ctx.tokenList.slice(prev.tokens.last, next.tokens.head)
            comma <- tokensBetweenArgs.find(_.is[Token.Comma])
          } yield comma

        val replaceCommasPatch = commas match {
          case head :: tail =>
            ctx.replaceToken(head, ".lazyZip(") +
              removeSurroundingWhiteSpaces(head) ++
              tail.map { comma =>
                ctx.replaceToken(comma, ").lazyZip(") +
                  removeSurroundingWhiteSpaces(comma)
              }
          case _ => Patch.empty
        }

        removeTokensPatch + replaceCommasPatch
    }.asPatch

  val copyToBuffer = SymbolMatcher.normalized(
    Symbol("_root_.scala.collection.TraversableOnce.copyToBuffer.")
  )

  def replaceCopyToBuffer(ctx: RuleCtx): Patch =
    ctx.tree.collect {
      case t @ q"${copyToBuffer(Term.Select(collection, _))}($buffer)" =>
        ctx.replaceTree(t, q"$buffer ++= $collection".syntax)
    }.asPatch

  val streamAppend = SymbolMatcher.normalized(
    Symbol("_root_.scala.collection.immutable.Stream.append.")
  )

  def replaceStreamAppend(ctx: RuleCtx): Patch =
    ctx.tree.collect {
      case streamAppend(t: Name) =>
        ctx.replaceTree(t, "lazyAppendedAll")
    }.asPatch

  override def fix(ctx: RuleCtx): Patch = {
    replaceToList(ctx) +
      replaceSymbols(ctx) +
      replaceTupleZipped(ctx) +
      replaceCopyToBuffer(ctx) +
      replaceStreamAppend(ctx) +
      replaceMutableMap(ctx) +
      replaceCompanion(ctx)
  }
}
