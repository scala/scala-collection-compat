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

package scala.fix

import scalafix.v0._
import scala.meta._

package object collection {
  def sym(tree: Tree)(implicit index: SemanticdbIndex): Patch = {
    println(index.symbol(tree))
    Patch.empty
  }

  def normalized(symbols: String*)(implicit index: SemanticdbIndex): SymbolMatcher =
    SymbolMatcher.normalized(symbols.map(Symbol(_)): _*)

  def exact(symbols: String*)(implicit index: SemanticdbIndex): SymbolMatcher =
    SymbolMatcher.exact(symbols.map(Symbol(_)): _*)

  def trailingBrackets(tree: Tree, ctx: RuleCtx): Option[(Token.LeftBracket, Token.RightBracket)] =
    for {
      end <- tree.tokens.lastOption
      open <- ctx.tokenList
        .find(end)(_.is[Token.LeftBracket])
        .map(_.asInstanceOf[Token.LeftBracket])
      close <- ctx.matchingParens.close(open)
    } yield (open, close)

  def trailingParens(tree: Tree, ctx: RuleCtx): Option[(Token.LeftParen, Token.RightParen)] =
    for {
      end   <- tree.tokens.lastOption
      open  <- ctx.tokenList.find(end)(_.is[Token.LeftParen]).map(_.asInstanceOf[Token.LeftParen])
      close <- ctx.matchingParens.close(open)
    } yield (open, close)

  def trailingApply(tree: Tree, ctx: RuleCtx): Option[(Token, Token)] =
    trailingParens(tree, ctx).orElse(trailingBrackets(tree, ctx))

  def startsWithParens(tree: Tree): Boolean =
    tree.tokens.headOption.map(_.is[Token.LeftParen]).getOrElse(false)

  val canBuildFroms = Map(
    "scala/LowPriorityImplicits#fallbackStringCanBuildFrom()." -> "scala.collection.immutable.IndexedSeq",
    "scala/Array.canBuildFrom()."                              -> "scala.Array",
    "scala/collection/BitSet.canBuildFrom."                    -> "scala.collection.BitSet",
    "scala/collection/immutable/HashMap.canBuildFrom()."       -> "scala.collection.immutable.HashMap",
    "scala/collection/immutable/IntMap.canBuildFrom()."        -> "scala.collection.immutable.IntMap",
    "scala/collection/immutable/List.canBuildFrom()."          -> "scala.collection.immutable.List",
    "scala/collection/immutable/ListMap.canBuildFrom()."       -> "scala.collection.immutable.ListMap",
    "scala/collection/immutable/LongMap.canBuildFrom()."       -> "scala.collection.immutable.LongMap",
    "scala/collection/immutable/Map.canBuildFrom()."           -> "scala.collection.immutable.Map",
    "scala/collection/immutable/Set.canBuildFrom()."           -> "scala.collection.immutable.Set",
    "scala/collection/immutable/SortedMap.canBuildFrom()."     -> "scala.collection.immutable.SortedMap",
    "scala/collection/immutable/SortedSet.newCanBuildFrom()."  -> "scala.collection.immutable.SortedSet",
    "scala/collection/immutable/TreeMap.canBuildFrom()."       -> "scala.collection.immutable.TreeMap",
    "scala/collection/immutable/Vector.canBuildFrom()."        -> "scala.collection.immutable.Vector",
    "scala/collection/Iterator.IteratorCanBuildFrom()."        -> "scala.collection.Iterator",
    "scala/collection/mutable/HashMap.canBuildFrom()."         -> "scala.collection.mutable.HashMap",
    "scala/collection/mutable/ListMap.canBuildFrom()."         -> "scala.collection.mutable.ListMap",
    "scala/collection/mutable/LongMap.canBuildFrom()."         -> "scala.collection.mutable.LongMap",
    "scala/collection/mutable/Map.canBuildFrom()."             -> "scala.collection.mutable.Map",
    "scala/collection/mutable/SortedMap.canBuildFrom()."       -> "scala.collection.mutable.SortedMap",
    "scala/collection/mutable/TreeMap.canBuildFrom()."         -> "scala.collection.mutable.TreeMap",
    "scala/collection/SortedSet.newCanBuildFrom()."            -> "scala.collection.SortedSet"
  )

  def extractCollectionFromBreakout(
      breakout: Tree,
      syntheticsByEndPos: Map[Int, Seq[Synthetic]]): Option[String] = {
    syntheticsByEndPos.get(breakout.pos.end) match {
      case Some(Seq(cbf, _)) =>
        var found: Option[String] = None
        cbf.names.map(_.symbol.syntax).exists { name =>
          found = canBuildFroms.get(name)
          found.nonEmpty
        }
        found
      case _ => None
    }
  }

  def pos(tree: Tree): String = s"[${tree.pos.start}..${tree.pos.end}]"
}
