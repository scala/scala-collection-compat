/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.fix.collection

import scalafix.v0._
import scala.meta._

class BreakoutRewrite(addCompatImport: RuleCtx => Patch)(implicit val index: SemanticdbIndex) {
  implicit class RichSymbol(val symbol: Symbol) {
    def exact(tree: Tree): Boolean =
      index.symbol(tree).fold(false)(_ == symbol)
  }

  def isLeftAssociative(tree: Tree): Boolean =
    tree match {
      case Term.Name(value) => value.last != ':'
      case _                => false
    }

  val breakOut = exact("scala/collection/package.breakOut().")

  // == infix operators ==

  val `TraversableLike ++`  = Symbol("scala/collection/TraversableLike#`++`().")
  val `Vector ++`           = Symbol("scala/collection/immutable/Vector#`++`().")
  val `List ++`             = Symbol("scala/collection/immutable/List#`++`().")
  val `SeqLike +:`          = Symbol("scala/collection/SeqLike#`+:`().")
  val `Vector +:`           = Symbol("scala/collection/immutable/Vector#`+:`().")
  val `List +:`             = Symbol("scala/collection/immutable/List#`+:`().")
  val `SeqLike :+`          = Symbol("scala/collection/SeqLike#`:+`().")
  val `Vector :+`           = Symbol("scala/collection/immutable/Vector#`:+`().")
  val `TraversableLike ++:` = Symbol("scala/collection/TraversableLike#`++:`().")

  val operatorsIteratorSymbols = List(`TraversableLike ++`, `List ++`, `Vector ++`)
  val operatorsViewSymbols = List(
    `SeqLike +:`,
    `Vector +:`,
    `List +:`,
    `SeqLike :+`,
    `Vector :+`,
    `TraversableLike ++:`
  )

  val operatorsSymbols = operatorsViewSymbols ++ operatorsIteratorSymbols

  val operatorsIterator = SymbolMatcher.exact(operatorsIteratorSymbols: _*)
  val operatorsView     = SymbolMatcher.exact(operatorsViewSymbols: _*)
  val operators         = SymbolMatcher.exact(operatorsSymbols: _*)

  // == select ==

  val `List.collect`            = Symbol("scala/collection/immutable/List#collect().")
  val `TraversableLike.collect` = Symbol("scala/collection/TraversableLike#collect().")
  val `List.flatMap`            = Symbol("scala/collection/immutable/List#flatMap().")
  val `TraversableLike.flatMap` = Symbol("scala/collection/TraversableLike#flatMap().")
  val `List.map`                = Symbol("scala/collection/immutable/List#map().")
  val `SetLike.map`             = Symbol("scala/collection/SetLike#map().")
  val `TraversableLike.map`     = Symbol("scala/collection/TraversableLike#map().")
  val `IterableLike.zip`        = Symbol("scala/collection/IterableLike#zip().")
  val `IndexedSeqOptimized.zip` = Symbol("scala/collection/IndexedSeqOptimized#zip().")
  val `IterableLike.zipAll`     = Symbol("scala/collection/IterableLike#zipAll().")
  val `SeqLike.union`           = Symbol("scala/collection/SeqLike#union().")
  val `SeqLike.updated`         = Symbol("scala/collection/SeqLike#updated().")
  val `Vector.updated`          = Symbol("scala/collection/immutable/Vector#updated().")
  val `SeqLike.reverseMap`      = Symbol("scala/collection/SeqLike#reverseMap().")

  val functionsZipSymbols = List(
    `IterableLike.zip`,
    `IndexedSeqOptimized.zip`,
    `IterableLike.zipAll`
  )
  val functionsIteratorSymbols = List(
    `List.collect`,
    `TraversableLike.collect`,
    `List.flatMap`,
    `TraversableLike.flatMap`,
    `List.map`,
    `SetLike.map`,
    `TraversableLike.map`,
    `SeqLike.union`
  ) ++ functionsZipSymbols
  val functionsViewSymbols            = List(`SeqLike.updated`, `Vector.updated`)
  val functionsReverseIteratorSymbols = List(`SeqLike.reverseMap`)
  val functionsSymbols =
    functionsIteratorSymbols ++ functionsViewSymbols ++ functionsReverseIteratorSymbols

  val functionsIterator        = SymbolMatcher.exact(functionsIteratorSymbols: _*)
  val functionsReverseIterator = SymbolMatcher.exact(functionsReverseIteratorSymbols: _*)
  val functionsView            = SymbolMatcher.exact(functionsViewSymbols: _*)
  val functions                = SymbolMatcher.exact(functionsSymbols: _*)

  val functionsZip = SymbolMatcher.exact(functionsZipSymbols: _*)

  // == special select ==

  val `TraversableLike.scanLeft` = exact("scala/collection/TraversableLike#scanLeft().")
  val `Future.sequence`          = exact("scala/concurrent/Future.sequence().")
  val `Future.traverse`          = exact("scala/concurrent/Future.traverse().")

  val toSpecificCollectionBuiltIn = Map(
    "scala.collection.immutable.Map" -> "toMap"
  )

  val toSpecificCollectionFromSpecific = Set(
    "scala.collection.BitSet"
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
  ) ++ toSpecificCollectionFromSpecific

  // == rule ==
  def apply(ctx: RuleCtx): Patch = {

    val syntheticsByEndPos: Map[Int, Seq[Synthetic]] = ctx.index.synthetics.groupBy(_.position.end)

    var requiresCompatImport = false

    def covertToCollection(intermediateLhs: String,
                           lhs: Term,
                           ap: Term,
                           breakout: Tree,
                           ap0: Term,
                           intermediateRhs: Option[String] = None,
                           rhs: Option[Term] = None): Patch = {

      val toCollection = extractCollectionFromBreakout(breakout, syntheticsByEndPos)

      val patchRhs =
        (intermediateRhs, rhs) match {
          case (Some(i), Some(r)) => ctx.addRight(r, "." + i)
          case _                  => Patch.empty
        }

      val patchSpecificCollection =
        toCollection match {
          case Some(col) =>
            toSpecificCollectionBuiltIn.get(col) match {
              case Some(toX) => ctx.addRight(ap0, '.' + toX)
              case None =>
                if (toSpecificCollectionFrom.contains(col)) {
                  requiresCompatImport = true

                  val convertMethod =
                    if (toSpecificCollectionFromSpecific.contains(col)) "fromSpecific"
                    else "from"

                  ctx.addLeft(ap0, col + "." + convertMethod + "(") +
                    ctx.addRight(ap0, ")")
                } else {
                  Patch.empty
                }
            }

          case None => Patch.empty
        }

      val isIterator = toCollection == Some("scala.collection.Iterator")

      val sharedPatch =
        ctx.addRight(lhs, "." + intermediateLhs) +
          patchRhs

      def removeBreakout: Patch = {
        val breakoutWithParens = ap0.tokens.slice(ap.tokens.size, ap0.tokens.size)
        ctx.removeTokens(breakoutWithParens)
      }

      val toColl =
        if (patchSpecificCollection.isEmpty && !isIterator) {
          toCollection match {
            case Some(col) =>
              requiresCompatImport = true
              ctx.addRight(ap, ".to") +
                ctx.replaceTree(breakout, col)
            case None => Patch.empty
          }
        } else {
          patchSpecificCollection +
            removeBreakout
        }

      sharedPatch + toColl
    }

    def replaceBreakoutWithCollection(breakout: Tree): Patch = {
      extractCollectionFromBreakout(breakout, syntheticsByEndPos) match {
        case Some(toCollection) =>
          requiresCompatImport = true
          ctx.replaceTree(breakout, toCollection)
        case None => Patch.empty
      }
    }

    val rewriteBreakout =
      ctx.tree.collect {
        // (xs ++ ys)(breakOut)
        case ap0 @ Term.Apply(
              ap @ Term.ApplyInfix(lhs, operators(op), _, List(rhs)),
              List(breakOut(bo))) =>
          val subject =
            if (isLeftAssociative(op)) lhs
            else rhs

          val intermediate =
            op match {
              case operatorsIterator(_) => "iterator"
              case operatorsView(_)     => "view"
              // since operators(op) matches iterator and view
              case _ => throw new Exception("impossible")
            }

          covertToCollection(intermediate, subject, ap, bo, ap0)

        // xs.map(f)(breakOut)
        case ap0 @ Term.Apply(
              ap @ Term.Apply(Term.Select(lhs, functions(op)), rhs :: _),
              List(breakOut(bo))) =>
          val intermediateLhs =
            op match {
              case functionsIterator(_)        => "iterator"
              case functionsView(_)            => "view"
              case functionsReverseIterator(_) => "reverseIterator"
              // since functions(op) matches iterator, view and reverseIterator
              case _ => throw new Exception("impossible")
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

          covertToCollection(
            intermediateLhs,
            lhs,
            ap,
            bo,
            ap0,
            intermediateRhs,
            Some(rhs)) + replaceUnion + replaceReverseMap

        // ts.scanLeft(d)(f)(breakOut)
        case ap0 @ Term.Apply(
              ap @ Term.Apply(Term.Apply(Term.Select(lhs, `TraversableLike.scanLeft`(op)), _), _),
              List(breakOut(bo))) =>
          covertToCollection("iterator", lhs, ap, bo, ap0)

        // sequence(xs)(breakOut, ec)
        case Term.Apply(Term.Apply(`Future.sequence`(_), _), List(breakOut(bo), _)) =>
          replaceBreakoutWithCollection(bo)

        // traverse(xs)(f)(breakOut, ec)
        case Term.Apply(
              Term.Apply(Term.Apply(`Future.traverse`(_), _), _),
              List(breakOut(bo), _)) =>
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
