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

import scalafix.internal.v0.LegacySemanticRule

class Collection213Upgrade
    extends LegacySemanticRule("Collection213Upgrade", index => new Collection213UpgradeV0(index))
    with Stable212BaseCheck {
  override def description: String =
    "Upgrade to 2.13’s collections (for applications)"
}

// Not 2.12 Cross-Compatible
case class Collection213UpgradeV0(index: SemanticdbIndex)
    extends SemanticRule(index, "Upgrade213")
    with Stable212Base {

  def isCrossCompatible: Boolean = false

  //  == Symbols ==

  val tupleZipped = normalized(
    "scala/runtime/Tuple2Zipped.Ops#zipped().",
    "scala/runtime/Tuple3Zipped.Ops#zipped()."
  )
  val retainMap = normalized(
    "scala/collection/mutable/MapLike#retain()."
  )
  val retainSet = normalized(
    "scala/collection/mutable/SetLike#retain()."
  )

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
        trailingParens(n, ctx).map {
          case (open, close) =>
            ctx.replaceToken(open, "{case ") +
              ctx.replaceToken(close, "}") +
              ctx.replaceTree(n, "filterInPlace")
        }.asPatch
    }.asPatch
  }

  def replaceTupleZipped(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case tupleZipped(Term.Select(Term.Tuple(args), name)) =>
        val removeTokensPatch =
          (for {
            zipped     <- name.tokens.headOption
            closeTuple <- ctx.tokenList.leading(zipped).find(_.is[Token.RightParen])
            openTuple  <- ctx.matchingParens.open(closeTuple.asInstanceOf[Token.RightParen])
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

  override def fix(ctx: RuleCtx): Patch = {
    super.fix(ctx) +
      replaceSymbols(ctx) +
      replaceTupleZipped(ctx) +
      replaceMutableMap(ctx) +
      replaceMutableSet(ctx)
  }
}
