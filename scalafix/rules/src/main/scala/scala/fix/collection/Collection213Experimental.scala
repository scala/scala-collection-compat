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

class Collection213Experimental
    extends LegacySemanticRule("Collection213Experimental",
                               index => new Collection213ExperimentalV0(index)) {
  override def isExperimental: Boolean = true

  override def description: String =
    "Upgrade to 2.13 collection (see https://github.com/scalameta/scalameta/issues/1212)"
}

// 2.12 Cross-Compatible
case class Collection213ExperimentalV0(index: SemanticdbIndex)
    extends SemanticRule(index, "Collection213Experimental") {

  val CollectionMap = TypeMatcher(
    Symbol("scala/collection/immutable/Map#"),
    Symbol("scala/collection/mutable/Map#"),
    Symbol("scala/Predef/Map#")
  )
  val CollectionSet = TypeMatcher(
    Symbol("scala/collection/Set#")
  )

  // == Symbols ==
  val mapZip   = exact("scala/collection/IterableLike#zip().")
  val mapPlus  = exact("scala/collection/MapLike#`+`().")
  val setPlus  = exact("scala/collection/SetLike#`+`().")
  val setMinus = exact("scala/collection/SetLike#`-`().")

  def replaceMapZip(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(Term.Select(CollectionMap(), mapZip(_)), List(_)) =>
        ctx.addRight(ap, ".toMap")
    }.asPatch
  }

  def replaceSetMapPlusMinus(ctx: RuleCtx): Patch = {
    def rewriteOp(op: Tree, rhs: Tree, doubleOp: String, col0: String): Patch = {
      val col = "_root_.scala.collection." + col0
      val callSite =
        if (startsWithParens(rhs)) {
          ctx.addLeft(rhs, col)
        } else {
          ctx.addLeft(rhs, col + "(") +
            ctx.addRight(rhs, ")")
        }

      ctx.addRight(op, doubleOp) + callSite
    }

    ctx.tree.collect {
      case ap @ Term.ApplyInfix(CollectionSet(), op @ setPlus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "+", "Set")

      case Term.ApplyInfix(CollectionSet(), op @ setMinus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "-", "Set")

      case Term.ApplyInfix(_, op @ mapPlus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "+", "Map")
    }.asPatch
  }

  override def fix(ctx: RuleCtx): Patch =
    replaceSetMapPlusMinus(ctx) +
      replaceMapZip(ctx)
}
