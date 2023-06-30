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

import metaconfig.{ConfDecoder, Conf, Configured}
import metaconfig.annotation.Description
import metaconfig.annotation.ExampleValue
import metaconfig.generic
import metaconfig.generic.Surface

import scalafix.internal.v0.LegacySemanticRule

class Collection213Roughly
    extends LegacySemanticRule("Collection213Roughly", index => new Collection213RoughlyV0(index)) {

  override def description: String =
    "Upgrade to 2.13 collection (Runtime semantic are different)"

  override def isExperimental: Boolean = true
}

/* 2.12 Cross-Compatible
 *
 * This rules is *roughly* correct, they compile but might have a different runtime semantic
 *
 * Map.{mapValues, filterKeys}, Seq.reverseMap were lazy but with a strict interface
 * (ex returning Map where they should return MapView).
 *
 * LazyList has a lazy head, were Stream has a strict head
 *
 */
final case class Collection213RoughlyV0(index: SemanticdbIndex, config: RoughlyConfig)
    extends SemanticRule(index, "Collection213Roughly") {
  def this(index: SemanticdbIndex) = this(index, RoughlyConfig.default)

  val mapValues =
    exact(
      "scala/collection/immutable/MapLike#mapValues().",
      "scala/collection/MapLike#filterKeys()."
    )

  val filterKeys =
    exact(
      "scala/collection/immutable/MapLike#filterKeys().",
      "scala/collection/MapLike#mapValues()."
    )

  // Not supported: SortedMap
  // Symbol("_root_.scala.collection.immutable.SortedMap#mapValues(Lscala/Function1;)Lscala/collection/immutable/SortedMap;."),
  // Symbol("_root_.scala.collection.SortedMapLike#mapValues(Lscala/Function1;)Lscala/collection/SortedMap;.")
  // Symbol("_root_.scala.collection.immutable.SortedMap#filterKeys(Lscala/Function1;)Lscala/collection/immutable/SortedMap;.")
  // Symbol("_root_.scala.collection.SortedMapLike#filterKeys(Lscala/Function1;)Lscala/collection/SortedMap;.")

  val streamAppend = exact(
    "scala/collection/immutable/Stream#append()."
  )

  val streamEmpty = SymbolMatcher.exact(
    Symbol("scala/collection/immutable/Stream.Empty.")
  )

  def replaceSymbols(ctx: RuleCtx): Patch = {
    if (config.withLazyList) {
      ctx.replaceSymbols(
        "scala.Stream" -> "scala.LazyList",
        "scala.collection.immutable.Stream" -> "scala.collection.immutable.LazyList"
      )
    } else Patch.empty
  }

  override def description: String = ""

  override def init(config: Conf): Configured[Rule] =
    config
      .getOrElse("Collection213Roughly")(RoughlyConfig.default)
      .map(Collection213RoughlyV0(index, _))

  override def fix(ctx: RuleCtx): Patch = {
    import config._

    val collectFixes =
      ctx.tree.collect {
        case ap @ Term.ApplyInfix(_, mapValues(_), _, _) if strictMapValues =>
          ctx.addLeft(ap, "(") +
            ctx.addRight(ap, ").toMap")

        case ap @ Term.Apply(Term.Select(_, mapValues(_)), List(_)) if strictMapValues =>
          ctx.addRight(ap, ".toMap")

        case ap @ Term.ApplyInfix(_, filterKeys(_), _, _) if strictFilterKeys =>
          ctx.addLeft(ap, "(") +
            ctx.addRight(ap, ").toMap")

        case ap @ Term.Apply(Term.Select(_, filterKeys(_)), List(_)) if strictFilterKeys =>
          ctx.addRight(ap, ".toMap")

        case streamAppend(t: Name) if withLazyAppendedAll =>
          ctx.replaceTree(t, "lazyAppendedAll")

        case streamEmpty(t: Name) if withLazyList =>
          ctx.replaceTree(t, "empty")

      }.asPatch

    collectFixes + replaceSymbols(ctx)
  }
}

case class RoughlyConfig(
    strictMapValues: Boolean = false,
    strictFilterKeys: Boolean = false,
    withLazyAppendedAll: Boolean = false,
    withLazyList: Boolean = false
)

object RoughlyConfig {
  val default: RoughlyConfig = RoughlyConfig()
  implicit val surface: Surface[RoughlyConfig] = generic.deriveSurface[RoughlyConfig]
  implicit val decoder: ConfDecoder[RoughlyConfig] = generic.deriveDecoder[RoughlyConfig](default)
}
