package fix

import scalafix._
import scalafix.util._
import scala.meta._

class BreakoutRewrite(addCompatImport: RuleCtx => Patch)(implicit val index: SemanticdbIndex) {
  implicit class RichSymbol(val symbol: Symbol) {
    def exact(tree: Tree): Boolean =
      index.symbol(tree).fold(false)(_ == symbol)
  }

  def isLeftAssociative(tree: Tree): Boolean =
    tree match {
      case Term.Name(value) => value.last != ':'
      case _ => false
    }

  val breakOut = SymbolMatcher.exact(Symbol("_root_.scala.collection.package.breakOut(Lscala/collection/generic/CanBuildFrom;)Lscala/collection/generic/CanBuildFrom;."))

  // == infix operators ==

  val `TraversableLike ++`  = Symbol("_root_.scala.collection.TraversableLike#`++`(Lscala/collection/GenTraversableOnce;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `Vector ++`           = Symbol("_root_.scala.collection.immutable.Vector#`++`(Lscala/collection/GenTraversableOnce;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `List ++`             = Symbol("_root_.scala.collection.immutable.List#`++`(Lscala/collection/GenTraversableOnce;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `SeqLike +:`          = Symbol("_root_.scala.collection.SeqLike#`+:`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `Vector +:`           = Symbol("_root_.scala.collection.immutable.Vector#`+:`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `List +:`             = Symbol("_root_.scala.collection.immutable.List#`+:`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `SeqLike :+`          = Symbol("_root_.scala.collection.SeqLike#`:+`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `Vector :+`           = Symbol("_root_.scala.collection.immutable.Vector#`:+`(Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `TraversableLike ++:` = Symbol("_root_.scala.collection.TraversableLike#`++:`(Lscala/collection/Traversable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")

  val operatorsIteratorSymbols = List(`TraversableLike ++`, `List ++`, `Vector ++`)
  val operatorsViewSymbols     = List(
    `SeqLike +:`, `Vector +:`, `List +:`,
    `SeqLike :+`, `Vector :+`,
    `TraversableLike ++:`
  )

  val operatorsSymbols         = operatorsViewSymbols ++ operatorsIteratorSymbols

  val operatorsIterator = SymbolMatcher.exact(operatorsIteratorSymbols: _*)
  val operatorsView     = SymbolMatcher.exact(operatorsViewSymbols: _*)
  val operators         = SymbolMatcher.exact(operatorsSymbols: _*)

  // == select ==

  val `List.collect`            = Symbol("_root_.scala.collection.immutable.List#collect(Lscala/PartialFunction;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `TraversableLike.collect` = Symbol("_root_.scala.collection.TraversableLike#collect(Lscala/PartialFunction;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `List.flatMap`            = Symbol("_root_.scala.collection.immutable.List#flatMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `TraversableLike.flatMap` = Symbol("_root_.scala.collection.TraversableLike#flatMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `List.map`                = Symbol("_root_.scala.collection.immutable.List#map(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `SetLike.map`             = Symbol("_root_.scala.collection.SetLike#map(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `TraversableLike.map`     = Symbol("_root_.scala.collection.TraversableLike#map(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `IterableLike.zip`        = Symbol("_root_.scala.collection.IterableLike#zip(Lscala/collection/GenIterable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `IndexedSeqOptimized.zip` = Symbol("_root_.scala.collection.IndexedSeqOptimized#zip(Lscala/collection/GenIterable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `IterableLike.zipAll`     = Symbol("_root_.scala.collection.IterableLike#zipAll(Lscala/collection/GenIterable;Ljava/lang/Object;Ljava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `SeqLike.union`           = Symbol("_root_.scala.collection.SeqLike#union(Lscala/collection/GenSeq;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `SeqLike.updated`         = Symbol("_root_.scala.collection.SeqLike#updated(ILjava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `Vector.updated`          = Symbol("_root_.scala.collection.immutable.Vector#updated(ILjava/lang/Object;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val `SeqLike.reverseMap`      = Symbol("_root_.scala.collection.SeqLike#reverseMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")

  val functionsZipSymbols = List(
    `IterableLike.zip`,
    `IndexedSeqOptimized.zip`,
    `IterableLike.zipAll`
  )
  val functionsIteratorSymbols        = List(
    `List.collect`, `TraversableLike.collect`,
    `List.flatMap`, `TraversableLike.flatMap`,
    `List.map`, `SetLike.map`, `TraversableLike.map`,
    `SeqLike.union`
  ) ++ functionsZipSymbols
  val functionsViewSymbols            = List(`SeqLike.updated`, `Vector.updated`)
  val functionsReverseIteratorSymbols = List(`SeqLike.reverseMap`)
  val functionsSymbols                = functionsIteratorSymbols ++ functionsViewSymbols ++ functionsReverseIteratorSymbols

  val functionsIterator        = SymbolMatcher.exact(functionsIteratorSymbols: _*)
  val functionsReverseIterator = SymbolMatcher.exact(functionsReverseIteratorSymbols: _*)
  val functionsView            = SymbolMatcher.exact(functionsViewSymbols: _*)
  val functions                = SymbolMatcher.exact(functionsSymbols: _*)

  val functionsZip = SymbolMatcher.exact(functionsZipSymbols: _*)

  // == special select ==

  val `TraversableLike.scanLeft` = SymbolMatcher.exact(Symbol("_root_.scala.collection.TraversableLike#scanLeft(Ljava/lang/Object;Lscala/Function2;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;."))
  val `Future.sequence` = SymbolMatcher.exact(Symbol("_root_.scala.concurrent.Future.sequence(Lscala/collection/TraversableOnce;Lscala/collection/generic/CanBuildFrom;Lscala/concurrent/ExecutionContext;)Lscala/concurrent/Future;."))
  val `Future.traverse` = SymbolMatcher.exact(Symbol("_root_.scala.concurrent.Future.traverse(Lscala/collection/TraversableOnce;Lscala/Function1;Lscala/collection/generic/CanBuildFrom;Lscala/concurrent/ExecutionContext;)Lscala/concurrent/Future;."))

  val toSpecificCollectionBuiltIn = Map(
    "scala.collection.immutable.Map" -> "toMap"
  )

  val toSpecificCollectionFrom = Set(
    "scala.collection.Map",
    "scala.collection.immutable.SortedMap",
    "scala.collection.immutable.HashMap",
    "scala.collection.immutable.ListMap",
    "scala.collection.immutable.TreeMap",
    "scala.collection.mutable.SortedMap",
    "scala.collection.mutable.HashMap",
    "scala.collection.mutable.ListMap",
    "scala.collection.mutable.TreeMap",
    "scala.collection.mutable.Map",
    "scala.collection.immutable.IntMap",
    "scala.collection.immutable.LongMap",
    "scala.collection.mutable.LongMap"
  )

  // == rule ==
  def apply(ctx: RuleCtx): Patch = {

    var requiresCompatImport = false

    def covertToCollection(intermediateLhs: String,
                           lhs: Term,
                           ap: Term,
                           breakout: Tree,
                           ap0: Term,
                           intermediateRhs: Option[String] = None,
                           rhs: Option[Term] = None): Patch = {

      val toCollection = extractCollectionFromBreakout(breakout)

      val patchRhs =
        (intermediateRhs, rhs) match {
          case (Some(i), Some(r)) => ctx.addRight(r, "." + i)
          case _ => Patch.empty
        }

      val patchSpecificCollection =
        toSpecificCollectionBuiltIn.get(toCollection) match {
          case Some(toX) => ctx.addRight(ap0, '.' + toX)
          case None =>
            if (toSpecificCollectionFrom.contains(toCollection)) {
              requiresCompatImport = true
              ctx.addLeft(ap0, toCollection + ".from(") +
              ctx.addRight(ap0, ")")
            } else {
              Patch.empty
            }
        }

      val isIterator = toCollection == "scala.collection.Iterator"

      val sharedPatch =
        ctx.addRight(lhs, "." + intermediateLhs) +
        patchRhs

      def removeBreakout: Patch = {
        val breakoutWithParens = ap0.tokens.slice(ap.tokens.size, ap0.tokens.size)
        ctx.removeTokens(breakoutWithParens)
      }

      val toColl =
        if (patchSpecificCollection.isEmpty && !isIterator) {
          requiresCompatImport = true
          ctx.addRight(ap, ".to") +
          ctx.replaceTree(breakout, toCollection)
        } else {
          patchSpecificCollection +
          removeBreakout
        }

      sharedPatch + toColl
    }

    def replaceBreakoutWithCollection(breakout: Tree): Patch = {
      requiresCompatImport = true

      val toCollection = extractCollectionFromBreakout(breakout)
      ctx.replaceTree(breakout, toCollection)
    }

    def extractCollectionFromBreakout(breakout: Tree): String = {
      val synth = ctx.index.synthetics.find(_.position.end == breakout.pos.end).get
      val Term.Apply(_, List(implicitCbf)) = synth.text.parse[Term].get

      implicitCbf match {
        case Term.ApplyType(q"scala.Predef.fallbackStringCanBuildFrom", _) =>
          "scala.collection.immutable.IndexedSeq"

        case Term.ApplyType(Term.Select(coll,_), _) =>
          coll.syntax

        case Term.Apply(Term.ApplyType(Term.Select(coll, _), _), _) =>
          coll.syntax

        case Term.Select(coll,_) =>
          coll.syntax

        case _ => {
          throw new Exception(
            s"""|cannot extract breakout collection:
                |
                |---------------------------------------------
                |syntax:
                |${implicitCbf.syntax}
                |
                |---------------------------------------------
                |structure:
                |${implicitCbf.structure}""".stripMargin
          )
        }
      }
    }

    val rewriteBreakout =
      ctx.tree.collect {
        // (xs ++ ys)(breakOut)
        case ap0 @ Term.Apply(ap @ Term.ApplyInfix(lhs, operators(op), _, List(rhs)), List(breakOut(bo))) =>
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

          covertToCollection(intermediate, subject, ap, bo, ap0)

        // xs.map(f)(breakOut)
        case ap0 @ Term.Apply(ap @ Term.Apply(Term.Select(lhs, functions(op)), rhs :: _), List(breakOut(bo))) =>
          val intermediateLhs =
            op match {
              case functionsIterator(_)        => "iterator"
              case functionsView(_)            => "view"
              case functionsReverseIterator(_) => "reverseIterator"
              // since functions(op) matches iterator, view and reverseIterator
              case _                           => throw new Exception("impossible")
            }

          val intermediateRhs =
            op match {
              case functionsZip(_) => Some("iterator")
              case _               => None
            }

          val replaceUnion =
            if (`SeqLike.union`.exact(op)) ctx.replaceTree(op, "concat")
            else Patch.empty

          val isReversed = `SeqLike.reverseMap`.exact(op)
          val replaceReverseMap =
            if (isReversed) ctx.replaceTree(op, "map")
            else Patch.empty

          covertToCollection(intermediateLhs, lhs, ap, bo, ap0, intermediateRhs, Some(rhs)) + replaceUnion + replaceReverseMap

        // ts.scanLeft(d)(f)(breakOut)
        case ap0 @ Term.Apply(ap @ Term.Apply(Term.Apply(Term.Select(lhs, `TraversableLike.scanLeft`(op)), _), _), List(breakOut(bo))) =>
          covertToCollection("iterator", lhs, ap, bo, ap0)

        // sequence(xs)(breakOut, ec)
        case Term.Apply(Term.Apply(`Future.sequence`(_), _), List(breakOut(bo), _)) =>
          replaceBreakoutWithCollection(bo)

        // traverse(xs)(f)(breakOut, ec)
        case Term.Apply(Term.Apply(Term.Apply(`Future.traverse`(_),_), _), List(breakOut(bo), _)) =>
          replaceBreakoutWithCollection(bo)

        // import scala.collection.breakOut
        case i: Importee if breakOut.matches(i) =>
          ctx.removeImportee(i)

      }.asPatch

    val compatImport =
      if (requiresCompatImport) addCompatImport(ctx)
      else Patch.empty

    rewriteBreakout + compatImport
  }
}
