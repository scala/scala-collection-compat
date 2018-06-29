package fix

import scalafix._
import scalafix.util._
import scala.meta._


/* 2.12 Cross-Compatible
 *
 * This rules is marked unstable since Map.mapValues was lazy
 */
case class RoughlyMapValues(index: SemanticdbIndex) extends SemanticRule(index, "RoughlyMapValues") {
  val mapMapValues =
    SymbolMatcher.exact(
      Symbol("_root_.scala.collection.immutable.MapLike#mapValues(Lscala/Function1;)Lscala/collection/immutable/Map;.")
    )

  override def fix(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(Term.Select(_, mapMapValues(_)), List(_)) =>
        ctx.addRight(ap, ".toMap")
    }.asPatch
  }
}
