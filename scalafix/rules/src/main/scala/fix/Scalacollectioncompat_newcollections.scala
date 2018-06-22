package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

case class Scalacollectioncompat_newcollections(index: SemanticdbIndex)
  extends SemanticRule(index, "Scalacollectioncompat_newcollections") {

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
  val setPlus2 = SymbolMatcher.exact(
    Symbol("_root_.scala.collection.SetLike#`+`(Ljava/lang/Object;Ljava/lang/Object;Lscala/collection/Seq;)Lscala/collection/Set;.")
  )
  val mapPlus2 = SymbolMatcher.exact(
    Symbol("_root_.scala.collection.immutable.MapLike#`+`(Lscala/Tuple2;Lscala/Tuple2;Lscala/collection/Seq;)Lscala/collection/immutable/Map;.")
  )
  val mutSetPlus = SymbolMatcher.exact(
    Symbol("_root_.scala.collection.mutable.SetLike#`+`(Ljava/lang/Object;)Lscala/collection/mutable/Set;.")
  )
  val mutMapPlus = SymbolMatcher.exact(
    Symbol("_root_.scala.collection.mutable.MapLike#`+`(Lscala/Tuple2;)Lscala/collection/mutable/Map;.")
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

  def replaceMutableSet(ctx: RuleCtx) =
    ctx.tree.collect {
      case retainSet(n: Name) =>
        ctx.replaceTree(n, "filterInPlace")
    }.asPatch

  def replaceMutableMap(ctx: RuleCtx) =
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

  def replaceSymbolicFold(ctx: RuleCtx) = 
    ctx.tree.collect {
      case Term.Apply(ap @ Term.ApplyInfix(rhs, foldRightSymbol(_), _, List(lhs)), _) => 
        ctx.replaceTree(ap, s"$rhs.foldRight($lhs)")

      case Term.Apply(ap @ Term.ApplyInfix(lhs, foldLeftSymbol(_), _, List(rhs)), _) =>
        ctx.replaceTree(ap, s"$rhs.foldLeft($lhs)")
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

  def replaceSetMapPlus2(ctx: RuleCtx): Patch = {
    def rewritePlus(ap: Term.ApplyInfix, lhs: Term, op: Term.Name, rhs1: Term, rhs2: Term): Patch = {
      val tokensToReplace =
        if(ap.tokens.headOption.map(_.is[Token.LeftParen]).getOrElse(false)) {
          // don't drop surrounding parens
          ap.tokens.slice(1, ap.tokens.size - 1)
        } else ap.tokens

      val newTree = 
        Term.ApplyInfix(
          Term.ApplyInfix(lhs, op, Nil, List(rhs1)),
          op,
          Nil,
          List(rhs2)
        ).syntax

      ctx.removeTokens(tokensToReplace) +
      tokensToReplace.headOption.map(x => ctx.addRight(x, newTree))
    }
    ctx.tree.collect {
      case ap @ Term.ApplyInfix(lhs, op @ mapPlus2(_), _, List(a, b)) =>
        rewritePlus(ap, lhs, op, a, b)

      case ap @ Term.ApplyInfix(lhs, op @ setPlus2(_), _, List(a, b)) =>
        rewritePlus(ap, lhs, op, a, b)
    }.asPatch
  }

  def replaceMutSetMapPlus(ctx: RuleCtx): Patch = {
    def rewriteMutPlus(lhs: Term, op: Term.Name): Patch = {
      ctx.addRight(lhs, ".clone()") +
      ctx.addRight(op, "=")
    }

    ctx.tree.collect {
      case Term.ApplyInfix(lhs, op @ mutSetPlus(_), _, List(_)) =>
        rewriteMutPlus(lhs, op)

      case Term.ApplyInfix(lhs, op @ mutMapPlus(_), _, List(_)) =>
        rewriteMutPlus(lhs, op)
    }.asPatch
  }

  override def fix(ctx: RuleCtx): Patch = {
    // println(ctx.index.database)

    replaceToList(ctx) +
      replaceSymbols(ctx) +
      replaceTupleZipped(ctx) +
      replaceCopyToBuffer(ctx) +
      replaceStreamAppend(ctx) +
      replaceMutableMap(ctx) + 
      replaceMutableSet(ctx) +
      replaceSymbolicFold(ctx) +
      replaceSetMapPlus2(ctx) +
      replaceMutSetMapPlus(ctx)
  }
}
