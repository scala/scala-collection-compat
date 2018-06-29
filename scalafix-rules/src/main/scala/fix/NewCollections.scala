package fix

import scalafix._
import scalafix.util._
import scala.meta._

// Not 2.12 Cross-Compatible
case class NewCollections(index: SemanticdbIndex) extends SemanticRule(index, "NewCollections") with Stable212Base {
  //  == Symbols ==
  val iterableSameElement = exact("_root_.scala.collection.IterableLike#sameElements(Lscala/collection/GenIterable;)Z.")
  val iterator = normalized("_root_.scala.collection.TraversableLike.toIterator.")
  val tupleZipped = normalized(
    "_root_.scala.runtime.Tuple2Zipped.Ops.zipped.",
    "_root_.scala.runtime.Tuple3Zipped.Ops.zipped."
  )
  val retainMap = normalized("_root_.scala.collection.mutable.MapLike.retain.")
  val retainSet = normalized("_root_.scala.collection.mutable.SetLike.retain.")

  object Breakout {
    implicit class RichSymbol(val symbol: Symbol) {
      def exact(tree: Tree)(implicit index: SemanticdbIndex): Boolean =
        index.symbol(tree).fold(false)(_ == symbol)
    }

    val breakOut = SymbolMatcher.exact(Symbol("_root_.scala.collection.package.breakOut(Lscala/collection/generic/CanBuildFrom;)Lscala/collection/generic/CanBuildFrom;."))

    // infix operators
    val `List ++`             = Symbol("_root_.scala.collection.immutable.List#`++`(Lscala/collection/GenTraversableOnce;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `List +:`             = Symbol("_root_.scala.collection.immutable.List#`+:`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `SeqLike :+`          = Symbol("_root_.scala.collection.SeqLike#`:+`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `TraversableLike ++:` = Symbol("_root_.scala.collection.TraversableLike#`++:`(Lscala/collection/Traversable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")

    val operatorsIteratorSymbols = List(`List ++`)
    val operatorsViewSymbols     = List(`List +:`, `SeqLike :+`, `TraversableLike ++:`)
    val operatorsSymbols         = operatorsViewSymbols ++ operatorsIteratorSymbols

    val operatorsIterator = SymbolMatcher.exact(operatorsIteratorSymbols: _*)
    val operatorsView     = SymbolMatcher.exact(operatorsViewSymbols: _*)
    val operators         = SymbolMatcher.exact(operatorsSymbols: _*)

    // select
    val `List.collect`        = Symbol("_root_.scala.collection.immutable.List#collect(Lscala/PartialFunction;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `List.flatMap`        = Symbol("_root_.scala.collection.immutable.List#flatMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `List.map`            = Symbol("_root_.scala.collection.immutable.List#map(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `IterableLike.zip`    = Symbol("_root_.scala.collection.IterableLike#zip(Lscala/collection/GenIterable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `IterableLike.zipAll` = Symbol("_root_.scala.collection.IterableLike#zipAll(Lscala/collection/GenIterable;Ljava/lang/Object;Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `SeqLike.union`       = Symbol("_root_.scala.collection.SeqLike#union(Lscala/collection/GenSeq;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `SeqLike.updated`     = Symbol("_root_.scala.collection.SeqLike#updated(ILjava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    val `SeqLike.reverseMap`  = Symbol("_root_.scala.collection.SeqLike#reverseMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")

    val functionsIteratorSymbols        = List(`List.collect`, `List.flatMap`, `List.map`, `IterableLike.zip`, `IterableLike.zipAll`, `SeqLike.union`)
    val functionsViewSymbols            = List(`SeqLike.updated`)
    val functionsReverseIteratorSymbols = List(`SeqLike.reverseMap`)
    val functionsSymbols                = functionsIteratorSymbols ++ functionsViewSymbols ++ functionsReverseIteratorSymbols

    val functionsIterator        = SymbolMatcher.exact(functionsIteratorSymbols: _*)
    val functionsReverseIterator = SymbolMatcher.exact(functionsReverseIteratorSymbols: _*)
    val functionsView            = SymbolMatcher.exact(functionsViewSymbols: _*)
    val functions                = SymbolMatcher.exact(functionsSymbols: _*)

    // special select

    // iterator
    val `TraversableLike.scanLeft` = SymbolMatcher.exact(Symbol("_root_.scala.collection.TraversableLike#scanLeft(Ljava/lang/Object;Lscala/Function2;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;."))

    def isLeftAssociative(tree: Tree): Boolean =
      tree match {
        case Term.Name(value) => value.last != ':'
        case _ => false
      }
  }

  // == Rules ==

  def replaceSymbols(ctx: RuleCtx): Patch = {
    ctx.replaceSymbols(
      "scala.TraversableOnce"            -> "scala.IterableOnce",
      "scala.collection.TraversableOnce" -> "scala.collection.IterableOnce"
    )
  }

  def replaceMutableSet(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case retainSet(n: Name) =>
        ctx.replaceTree(n, "filterInPlace")
    }.asPatch
  }

  def replaceMutableMap(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(Term.Select(_, retainMap(n: Name)), List(_: Term.PartialFunction)) =>
        ctx.replaceTree(n, "filterInPlace")

      case Term.Apply(Term.Select(_, retainMap(n: Name)), List(_: Term.Function)) =>
        trailingParens(n, ctx).map { case (open, close) =>
          ctx.replaceToken(open, "{case ") +
          ctx.replaceToken(close, "}") +
          ctx.replaceTree(n, "filterInPlace")
        }.asPatch
    }.asPatch
  }

  def replaceToList(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case iterator(t: Name) =>
        ctx.replaceTree(t, "iterator")

      case t @ toTpe(n: Name) =>
        trailingBrackets(n, ctx).map { case (open, close) =>
          ctx.replaceToken(open, "(") + ctx.replaceToken(close, ")")
        }.asPatch
    }.asPatch
  }

  def replaceTupleZipped(ctx: RuleCtx): Patch = {
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
  }

  def replaceIterableSameElements(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(Term.Select(lhs, iterableSameElement(_)), List(_)) =>
        ctx.addRight(lhs, ".iterator")
    }.asPatch
  }

  def replaceBreakout(ctx: RuleCtx): Patch = {
    import Breakout._

    def fixIt(intermediate: String, lhs: Term, ap: Term, breakout: Tree): Patch = {
      ctx.addRight(lhs, "." + intermediate) +
      ctx.addRight(ap, ".to") +
      ctx.replaceTree(breakout, "implicitly")
    }

    ctx.tree.collect {
      case i: Importee if breakOut.matches(i) =>
        ctx.removeImportee(i)

      case Term.Apply(ap @ Term.ApplyInfix(lhs, operators(op), _, List(rhs)), List(breakOut(bo))) =>
        val subject =
          if(isLeftAssociative(op)) lhs
          else rhs

        val intermediate =
          op match {
            case operatorsIterator(_) => "iterator"
            case operatorsView(_)     => "view"
            // since operators(op) matches iterator and view
            case _                    => throw new Exception("impossible")
          }

        fixIt(intermediate, subject, ap, bo)

      case Term.Apply(ap @ Term.Apply(Term.Select(lhs, functions(op)), _), List(breakOut(bo))) =>
        val intermediate =
          op match {
            case functionsIterator(_)        => "iterator"
            case functionsView(_)            => "view"
            case functionsReverseIterator(_) => "reverseIterator"
            // since functions(op) matches iterator, view and reverseIterator
            case _                           => throw new Exception("impossible")
          }

        val replaceUnion =
          if (`SeqLike.union`.exact(op)) ctx.replaceTree(op, "concat")
          else Patch.empty

        val isReversed = `SeqLike.reverseMap`.exact(op)
        val replaceReverseMap =
          if (isReversed) ctx.replaceTree(op, "map")
          else Patch.empty

        fixIt(intermediate, lhs, ap, bo) + replaceUnion + replaceReverseMap

      case Term.Apply(ap @ Term.Apply(Term.Apply(Term.Select(lhs, `TraversableLike.scanLeft`(op)), _), _), List(breakOut(bo))) =>
        fixIt("iterator", lhs, ap, bo)
    }.asPatch
  }

  override def fix(ctx: RuleCtx): Patch = {
    super.fix(ctx) +
      replaceToList(ctx) +
      replaceSymbols(ctx) +
      replaceTupleZipped(ctx) +
      replaceMutableMap(ctx) +
      replaceMutableSet(ctx) +
      replaceBreakout(ctx) +
      replaceIterableSameElements(ctx)
  }
}
