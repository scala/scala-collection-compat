package fix

import scalafix._
import scalafix.syntax._
import scalafix.util._
import scala.meta._

// 2.12 Cross-Compatible
trait Stable212Base { self: SemanticRule =>

    //  == Symbols ==
  def foldSymbol(isLeft: Boolean): SymbolMatcher = {
    val op =
      if (isLeft) "/:"
      else ":\\"

    normalized(s"_root_.scala.collection.TraversableOnce.`$op`.")
  }

  val toTpe = normalized("_root_.scala.collection.TraversableLike.to.")
  val copyToBuffer = normalized("_root_.scala.collection.TraversableOnce.copyToBuffer.")
  val arrayBuilderMake = normalized("_root_.scala.collection.mutable.ArrayBuilder.make(Lscala/reflect/ClassTag;)Lscala/collection/mutable/ArrayBuilder;.")
  val collectionCanBuildFrom = exact("_root_.scala.collection.generic.CanBuildFrom#")
  val collectionCanBuildFromImport = exact("_root_.scala.collection.generic.CanBuildFrom.;_root_.scala.collection.generic.CanBuildFrom#")
  val nothing = exact("_root_.scala.Nothing#")
  val setPlus2 = exact("_root_.scala.collection.SetLike#`+`(Ljava/lang/Object;Ljava/lang/Object;Lscala/collection/Seq;)Lscala/collection/Set;.")
  val mapPlus2 = exact("_root_.scala.collection.immutable.MapLike#`+`(Lscala/Tuple2;Lscala/Tuple2;Lscala/collection/Seq;)Lscala/collection/immutable/Map;.")
  val mutSetPlus = exact("_root_.scala.collection.mutable.SetLike#`+`(Ljava/lang/Object;)Lscala/collection/mutable/Set;.")
  val mutMapPlus = exact("_root_.scala.collection.mutable.MapLike#`+`(Lscala/Tuple2;)Lscala/collection/mutable/Map;.")
  val mutMapUpdate = exact("_root_.scala.collection.mutable.MapLike#updated(Ljava/lang/Object;Ljava/lang/Object;)Lscala/collection/mutable/Map;.")
  val foldLeftSymbol = foldSymbol(isLeft = true)
  val foldRightSymbol = foldSymbol(isLeft = false)

  // == Rules ==

  def replaceSymbols0(ctx: RuleCtx): Patch = {
    ctx.replaceSymbols(
      "scala.collection.LinearSeq"   -> "scala.collection.immutable.List",
      "scala.Traversable"            -> "scala.Iterable",
      "scala.collection.Traversable" -> "scala.collection.Iterable"
    )
  }

  def replaceSymbolicFold(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(ap @ Term.ApplyInfix(rhs, foldRightSymbol(_), _, List(lhs)), _) =>
        ctx.replaceTree(ap, s"$rhs.foldRight($lhs)")

      case Term.Apply(ap @ Term.ApplyInfix(lhs, foldLeftSymbol(_), _, List(rhs)), _) =>
        ctx.replaceTree(ap, s"$rhs.foldLeft($lhs)")
    }.asPatch
  }

  def replaceCopyToBuffer(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case t @ q"${copyToBuffer(Term.Select(collection, _))}($buffer)" =>
        ctx.replaceTree(t, q"$buffer ++= $collection".syntax)
    }.asPatch
  }

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

  def replaceArrayBuilderMake(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(at @ Term.ApplyType(Term.Select(lhs, arrayBuilderMake(_)), args), Nil) =>
        val extraParens =
          ap.tokens.slice(at.tokens.size, ap.tokens.size)
        ctx.removeTokens(extraParens)
    }.asPatch
  }

  def replaceCanBuildFrom(ctx: RuleCtx): Patch = {
    val useSites =
      ctx.tree.collect {
        case Defn.Def(_, _, _, paramss, _, body) =>
          CanBuildFromNothing(paramss, body, ctx, collectionCanBuildFrom, nothing, toTpe) +
            CanBuildFrom(paramss, body, ctx, collectionCanBuildFrom, nothing)
      }.asPatch

    val imports =
      ctx.tree.collect {
        case i: Importee if collectionCanBuildFromImport.matches(i) =>
            ctx.removeImportee(i)
      }.asPatch

    val compatImport =
      ctx.addGlobalImport(importer"scala.collection.compat._")

    if (useSites.nonEmpty) useSites + imports + compatImport
    else Patch.empty
  }

  override def fix(ctx: RuleCtx): Patch = {
    replaceSymbols0(ctx) +
      replaceCanBuildFrom(ctx) +
      replaceCopyToBuffer(ctx) +
      replaceSymbolicFold(ctx) +
      replaceSetMapPlus2(ctx) +
      replaceMutSetMapPlus(ctx) +
      replaceMutMapUpdated(ctx) +
      replaceArrayBuilderMake(ctx)
  }

}
