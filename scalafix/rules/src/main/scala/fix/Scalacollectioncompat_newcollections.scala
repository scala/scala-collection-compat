package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

case class Scalacollectioncompat_newcollections(index: SemanticdbIndex)
  extends SemanticRule(index, "Scalacollectioncompat_newcollections") {

  // Two rules triggers the same rewrite TraversableLike.to and CanBuildFrom
  // we keep track of what is handled in CanBuildFrom and guard against TraversableLike.to
  val handledTo = scala.collection.mutable.Set[Tree]()

  def trailingParens(tree: Tree, ctx: RuleCtx): Option[(Token.LeftParen, Token.RightParen)] =
    for {
      end <- tree.tokens.lastOption
      open <- ctx.tokenList.find(end)(_.is[Token.LeftParen]).map(_.asInstanceOf[Token.LeftParen])
      close <- ctx.matchingParens.close(open)
    } yield (open, close)

  def trailingBrackets(tree: Tree, ctx: RuleCtx): Option[(Token.LeftBracket, Token.RightBracket)] =
    for {
      end <- tree.tokens.lastOption
      open <- ctx.tokenList.find(end)(_.is[Token.LeftBracket]).map(_.asInstanceOf[Token.LeftBracket])
      close <- ctx.matchingParens.close(open)
    } yield (open, close)

  // terms dont give us terms https://github.com/scalameta/scalameta/issues/1212
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

  def replaceSymbols(ctx: RuleCtx): Patch = {
    ctx.replaceSymbols(
      "scala.collection.LinearSeq" -> "scala.collection.immutable.List",
      "scala.Stream" -> "scala.LazyList",
      "scala.collection.immutable.Stream" -> "scala.collection.immutable.LazyList",
      "scala.Traversable" -> "scala.Iterable",
      "scala.collection.Traversable" -> "scala.collection.Iterable",
      "scala.TraversableOnce" -> "scala.IterableOnce",
      "scala.collection.TraversableOnce" -> "scala.collection.IterableOnce"
    )
  }

  val collectionCanBuildFrom = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.generic.CanBuildFrom#")
    )

  val collectionCanBuildFromImport = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.generic.CanBuildFrom.;_root_.scala.collection.generic.CanBuildFrom#")
    )
  
  val nothing = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.Nothing#")
    )

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
  val setPlus = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.SetLike#`+`(Ljava/lang/Object;)Lscala/collection/Set;.")
    )
  val setMinus = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.SetLike#`-`(Ljava/lang/Object;)Lscala/collection/Set;.")
    )
  val mapPlus = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.MapLike#`+`(Lscala/Tuple2;)Lscala/collection/Map;.")
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
  val mutMapUpdate = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.mutable.MapLike#updated(Ljava/lang/Object;Ljava/lang/Object;)Lscala/collection/mutable/Map;.")
    )

  val iterableSameElement = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.IterableLike#sameElements(Lscala/collection/GenIterable;)Z.")
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

  val mapMapValues = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.immutable.MapLike#mapValues(Lscala/Function1;)Lscala/collection/immutable/Map;.")
    )

  val retainSet = 
    SymbolMatcher.normalized(
      Symbol("_root_.scala.collection.mutable.SetLike.retain.")
    )


  val arrayBuilderMake = 
    SymbolMatcher.normalized(
      Symbol("_root_.scala.collection.mutable.ArrayBuilder.make(Lscala/reflect/ClassTag;)Lscala/collection/mutable/ArrayBuilder;.")
    )

  val mapZip = 
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.IterableLike#zip(Lscala/collection/GenIterable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
    )

  def startsWithParens(tree: Tree): Boolean = 
    tree.tokens.headOption.map(_.is[Token.LeftParen]).getOrElse(false)

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
        trailingParens(n, ctx).map { case (open, close) =>
          ctx.replaceToken(open, "{case ") +
          ctx.replaceToken(close, "}") +
          ctx.replaceTree(n, "filterInPlace")
        }.asPatch
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
      case t @ toTpe(n: Name) if !handledTo.contains(n) =>
        trailingBrackets(n, ctx).map { case (open, close) =>
          ctx.replaceToken(open, "(") + ctx.replaceToken(close, ")")
        }.asPatch
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
        if(startsWithParens(ap)) {
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

  def replaceMutMapUpdated(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(Term.Select(a, up @ mutMapUpdate(_)), List(k, v)) => {
        ctx.addRight(up, "clone() += (") +
        ctx.removeTokens(up.tokens) +
        ctx.addRight(v, ")")
      }
    }.asPatch
  }

  def replaceIterableSameElements(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(Term.Select(lhs, iterableSameElement(_)), List(_)) =>
        ctx.addRight(lhs, ".iterator")
    }.asPatch
  }

  def replaceArrayBuilderMake(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(at @ Term.ApplyType(Term.Select(lhs, arrayBuilderMake(_)), args), Nil) =>
        val extraParens =
          ap.tokens.slice(at.tokens.size, ap.tokens.size)
        ctx.removeTokens(extraParens)
    }.asPatch
  }
  
  def replaceMapZip(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(Term.Select(CollectionMap(), mapZip(_)), List(_)) =>
        ctx.addRight(ap, ".toMap")
    }.asPatch
  }

  def replaceMapMapValues(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(Term.Select(_, mapMapValues(_)), List(_)) =>
        ctx.addRight(ap, ".toMap")
    }.asPatch
  }

  object CanBuildFromNothing {
    def apply(paramss: List[List[Term.Param]], body: Term, ctx: RuleCtx): Patch = {
      paramss.flatten.collect{
        case
          Term.Param(
            List(Mod.Implicit()),
            param, 
            Some(
              tpe @ Type.Apply(
                collectionCanBuildFrom(_), 
                List(
                  nothing(_), 
                  t,
                  cct @ Type.Apply(
                    cc,
                    _
                  )
                )
              )
            ),
            _
          ) => new CanBuildFromNothing(param, tpe, t, cct, cc)
      }.map(_.toFactory(body, ctx)).asPatch
    }
  }

  // example:
  // implicit cbf: collection.generic.CanBuildFrom[Nothing, Int, CC[Int]]
  //
  // param: cbf
  // tpe  : collection.generic.CanBuildFrom[Nothing, Int, CC[Int]]
  // cbf  : CanBuildFrom
  //   v  : Int
  // cct  : CC[Int]
  //  cc  : CC

  case class CanBuildFromNothing(param: Name, tpe: Type.Apply, t: Type, cct: Type.Apply, cc: Type) {
    def toFactory(body: Term, ctx: RuleCtx): Patch = {
        val matchCbf = SymbolMatcher.exact(ctx.index.symbol(param).get)

        // cbf() / cbf.apply => cbf.newBuilder
        def replaceNewBuilder(tree: Tree, cbf2: Term): Patch =
          ctx.replaceTree(tree, Term.Select(cbf2, Term.Name("newBuilder")).syntax)

        // don't patch cbf.apply twice (cbf.apply and cbf.apply())
        val visitedCbfCalls = scala.collection.mutable.Set[Tree]()

        val cbfCalls = 
          body.collect {
            // cbf.apply()
            case ap @ Term.Apply(sel @ Term.Select(cbf2 @ matchCbf(_), apply), Nil) =>
              visitedCbfCalls += sel
              replaceNewBuilder(ap, cbf2)
              
            // cbf.apply
            case sel @ Term.Select(cbf2 @ matchCbf(_), ap) if (!visitedCbfCalls.contains(sel)) =>
              replaceNewBuilder(sel, cbf2)

            // cbf()
            case ap @ Term.Apply(cbf2 @ matchCbf(_), Nil) =>
              replaceNewBuilder(ap, cbf2)
          }.asPatch


        val matchCC = SymbolMatcher.exact(ctx.index.symbol(cc).get)

        // e.to[CC] => e.to(cbf)
        val toCalls =
          body.collect {
            case ap @ Term.ApplyType(Term.Select(_, to @ toTpe(_)), List(cc2 @ matchCC(_))) =>
              handledTo += to

              // e.to[CC](*cbf*) extract implicit parameter
              val synth = ctx.index.synthetics.find(_.position.end == ap.pos.end).get
              val Term.Apply(_, List(implicitCbf)) = synth.text.parse[Term].get

              // This is a bit unsafe
              // https://github.com/scalameta/scalameta/issues/1636
              if (implicitCbf.syntax == param.syntax) {
                trailingBrackets(to, ctx).map { case (open, close) =>
                  ctx.replaceTree(cc2, implicitCbf.syntax) +
                  ctx.replaceToken(open, "(") +
                  ctx.replaceToken(close, ")")
                }.asPatch
              } else Patch.empty

          }.asPatch

        // implicit cbf: collection.generic.CanBuildFrom[Nothing, Int, CC[Int]] =>
        // implicit cbf: collection.Factory[Int, CC[Int]]
        val parameterType = 
          ctx.replaceTree(
            tpe,
            Type.Apply(Type.Name("collection.Factory"), List(t, cct)).syntax
          ) 

        parameterType + cbfCalls + toCalls
    }
  }

  object CanBuildFrom {
    def apply(paramss: List[List[Term.Param]], body: Term, ctx: RuleCtx): Patch = {
      // CanBuildFrom has def apply() but not CanBuild
      def emptyApply(param: Name): Boolean = {
        import scala.meta.contrib._
        val matchCbf = SymbolMatcher.exact(ctx.index.symbol(param).get)
        body.exists{
          case Term.Apply(Term.Select(matchCbf(_), _), Nil) => true
          case Term.Apply(matchCbf(_), Nil) => true
          case _ => false
        }
      }

      paramss.flatten.collect{
        case Term.Param(
            List(Mod.Implicit()),
            param,
            Some(
              Type.Apply(
                cbf @ collectionCanBuildFrom(_), 
                List(p1, _, _)
              )
            ),
            _
          ) if !nothing.matches(p1) && !emptyApply(param) => new CanBuildFrom(param, cbf)
      }.map(_.toBuildFrom(body, ctx)).asPatch
    }
  }

  // example:
  // implicit cbf: collection.generic.CanBuildFrom[C0, Int, CC[Int]]
  // param: cbf
  // cbf  : collection.generic.CanBuildFrom
  case class CanBuildFrom(param: Name, cbf: Type) {
    def toBuildFrom(body: Term, ctx: RuleCtx): Patch = {

      val matchCbf = SymbolMatcher.exact(ctx.index.symbol(param).get)

      // cbf(x) / cbf.apply(x) => cbf.newBuilder(x)
      def replaceNewBuilder(tree: Tree, cbf2: Term, x: Term): Patch =
        ctx.replaceTree(
          tree,
          Term.Apply(Term.Select(cbf2, Term.Name("newBuilder")), List(x)).syntax
        )

      val cbfCalls = 
        body.collect {
          // cbf.apply(x)
          case ap @ Term.Apply(sel @ Term.Select(cbf2 @ matchCbf(_), apply), List(x)) =>
            replaceNewBuilder(ap, cbf2, x)

          // cbf(x)
          case ap @ Term.Apply(cbf2 @ matchCbf(_), List(x)) =>
            replaceNewBuilder(ap, cbf2, x)
        }.asPatch

      val parameterType = 
        ctx.replaceTree(cbf, "collection.BuildFrom")

      parameterType + cbfCalls
    }
  }

  def replaceCanBuildFrom(ctx: RuleCtx): Patch = {
    val useSites =
      ctx.tree.collect {
        case Defn.Def(_, _, _, paramss, _, body) =>
          CanBuildFromNothing(paramss, body, ctx) +
          CanBuildFrom(paramss, body, ctx)
      }.asPatch

    val imports =
      ctx.tree.collect {
        case i: Importee if collectionCanBuildFromImport.matches(i) =>
            ctx.removeImportee(i)
      }.asPatch

    if (useSites.nonEmpty) useSites + imports
    else Patch.empty
  }
  
  override def fix(ctx: RuleCtx): Patch = {
    replaceCanBuildFrom(ctx) +
      replaceToList(ctx) +
      replaceSymbols(ctx) +
      replaceTupleZipped(ctx) +
      replaceCopyToBuffer(ctx) +
      replaceStreamAppend(ctx) +
      replaceMutableMap(ctx) + 
      replaceMutableSet(ctx) +
      replaceSymbolicFold(ctx) +
      replaceSetMapPlus2(ctx) +
      replaceSetMapPlusMinus(ctx) +
      replaceMutSetMapPlus(ctx) +
      replaceMutMapUpdated(ctx) +
      replaceArrayBuilderMake(ctx) +
      replaceIterableSameElements(ctx) +
      replaceMapZip(ctx) +
      replaceMapMapValues(ctx)
  }
}
