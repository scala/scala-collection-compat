package fix

import scalafix._
import scalafix.util._
import scala.meta._

import metaconfig.{ConfDecoder, Conf, Configured}
import metaconfig.annotation.Description
import metaconfig.annotation.ExampleValue
import metaconfig.generic
import metaconfig.generic.Surface

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
final case class Roughly(index: SemanticdbIndex, config: RoughlyConfig)
    extends SemanticRule(index, "Roughly") {
  def this(index: SemanticdbIndex) = this(index, RoughlyConfig.default)

  val mapValues =
    SymbolMatcher.exact(
      Symbol(
        "_root_.scala.collection.immutable.MapLike#mapValues(Lscala/Function1;)Lscala/collection/immutable/Map;."),
      Symbol("_root_.scala.collection.MapLike#filterKeys(Lscala/Function1;)Lscala/collection/Map;.")
    )

  val filterKeys =
    SymbolMatcher.exact(
      Symbol(
        "_root_.scala.collection.immutable.MapLike#filterKeys(Lscala/Function1;)Lscala/collection/immutable/Map;."),
      Symbol("_root_.scala.collection.MapLike#mapValues(Lscala/Function1;)Lscala/collection/Map;.")
    )

  // Not supported: SortedMap
  // Symbol("_root_.scala.collection.immutable.SortedMap#mapValues(Lscala/Function1;)Lscala/collection/immutable/SortedMap;."),
  // Symbol("_root_.scala.collection.SortedMapLike#mapValues(Lscala/Function1;)Lscala/collection/SortedMap;.")
  // Symbol("_root_.scala.collection.immutable.SortedMap#filterKeys(Lscala/Function1;)Lscala/collection/immutable/SortedMap;.")
  // Symbol("_root_.scala.collection.SortedMapLike#filterKeys(Lscala/Function1;)Lscala/collection/SortedMap;.")

  val streamAppend = SymbolMatcher.normalized(
    Symbol("_root_.scala.collection.immutable.Stream.append.")
  )

  def replaceSymbols(ctx: RuleCtx): Patch = {
    if (config.withLazyList) {
      ctx.replaceSymbols(
        "scala.Stream"                      -> "scala.LazyList",
        "scala.collection.immutable.Stream" -> "scala.collection.immutable.LazyList"
      )
    } else Patch.empty
  }

  override def description: String = ""

  override def init(config: Conf): Configured[Rule] =
    config
      .getOrElse("roughly", "Roughly")(RoughlyConfig.default)
      .map(Roughly(index, _))

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
  val default: RoughlyConfig                       = RoughlyConfig()
  implicit val surface: Surface[RoughlyConfig]     = generic.deriveSurface[RoughlyConfig]
  implicit val decoder: ConfDecoder[RoughlyConfig] = generic.deriveDecoder[RoughlyConfig](default)
}
