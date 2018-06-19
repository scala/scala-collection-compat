package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

case class Scalacollectioncompat_newcollections(index: SemanticdbIndex)
  extends SemanticRule(index, "Scalacollectioncompat_newcollections") {

  val naturalNumberTypes = List("Byte", "Char", "Int", "Short")
  val shiffingOperators = List("<<", ">>>", ">>")
  val naturalNumberShiftingSymbols = 
    for {
      tpe <- naturalNumberTypes
      op <- shiffingOperators
    } yield Symbol(s"scala.$tpe#`$op`(Long).")

  val naturalShiffting = SymbolMatcher.exact(naturalNumberShiftingSymbols: _*)  

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
  def foldSymbol(isLeft: Boolean): SymbolMatcher = {
    val op = 
      if (isLeft) "/:"
      else ":\\"

    SymbolMatcher.normalized(Symbol(s"_root_.scala.collection.TraversableOnce.`$op`."))
  }
  val foldLeftSymbol = foldSymbol(isLeft = true)
  val foldRightSymbol = foldSymbol(isLeft = false)

  val retainMap = 
    SymbolMatcher.normalized(
      Symbol("_root_.scala.collection.mutable.MapLike.retain.")
    )

  val retainSet = 
    SymbolMatcher.normalized(
      Symbol("_root_.scala.collection.mutable.SetLike.retain.")
    )

  def replaceMutableSet(ctx: RuleCtx): Patch = 
    ctx.tree.collect {
      case retainSet(n: Name) =>
        ctx.replaceTree(n, "filterInPlace")
    }.asPatch

  def replaceMutableMap(ctx: RuleCtx): Patch = 
    ctx.tree.collect {
      case Term.Apply(Term.Select(_, retainMap(n: Name)), List(_: Term.PartialFunction)) =>
        ctx.replaceTree(n, "filterInPlace")

      case Term.Apply(Term.Select(_, retainMap(n: Name)), List(_: Term.Function)) =>
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

  def replaceSymbolicFold(ctx: RuleCtx): Patch = 
    ctx.tree.collect {
      case Term.Apply(ap @ Term.ApplyInfix(rhs, foldRightSymbol(_), _, List(lhs)), _) => 
        ctx.replaceTree(ap, s"$rhs.foldRight($lhs)")

      case Term.Apply(ap @ Term.ApplyInfix(lhs, foldLeftSymbol(_), _, List(rhs)), _) =>
        ctx.replaceTree(ap, s"$rhs.foldLeft($lhs)")
    }.asPatch

  def replaceToList(ctx: RuleCtx): Patch = 
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

  def replaceTupleZipped(ctx: RuleCtx): Patch = 
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

  def replaceNaturalShiffting(ctx: RuleCtx): Patch =
    ctx.tree.collect {
      case Term.ApplyInfix(lhs, naturalShiffting(_), Nil, List(_)) => 
        ctx.addRight(lhs, ".toLong")
    }.asPatch

  override def fix(ctx: RuleCtx): Patch = {
    replaceToList(ctx) +
      replaceSymbols(ctx) +
      replaceTupleZipped(ctx) +
      replaceCopyToBuffer(ctx) +
      replaceStreamAppend(ctx) +
      replaceMutableMap(ctx) + 
      replaceMutableSet(ctx) +
      replaceSymbolicFold(ctx) + 
      replaceNaturalShiffting(ctx)
  }
}
