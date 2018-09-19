package scala.fix.collection

import scalafix._
import scalafix.util._
import scala.meta._

import scala.collection.mutable

object CanBuildFrom {
  def apply(paramss: List[List[Term.Param]],
            stats: List[Tree],
            ctx: RuleCtx,
            collectionCanBuildFrom: SymbolMatcher,
            nothing: SymbolMatcher)(implicit index: SemanticdbIndex): Patch = {
    // CanBuildFrom has def apply() but not CanBuild
    def emptyApply(param: Name): Boolean = {
      import scala.meta.contrib._
      val matchCbf = SymbolMatcher.exact(ctx.index.symbol(param).get)
      stats.exists(
        _.exists {
          case Term.Apply(Term.Select(matchCbf(_), _), Nil) => true
          case Term.Apply(matchCbf(_), Nil)                 => true
          case _                                            => false
        }
      )
    }

    paramss.flatten
      .collect {
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
            ) if !nothing.matches(p1) && !emptyApply(param) =>
          new CanBuildFrom(param, cbf)
      }
      .map(_.toBuildFrom(stats, ctx))
      .asPatch
  }
}

// example:
// implicit cbf: collection.generic.CanBuildFrom[C0, Int, CC[Int]]
// param: cbf
// cbf  : collection.generic.CanBuildFrom
case class CanBuildFrom(param: Name, cbf: Type) {
  def toBuildFrom(stats: List[Tree], ctx: RuleCtx)(implicit index: SemanticdbIndex): Patch = {

    val matchCbf = SymbolMatcher.exact(ctx.index.symbol(param).get)

    // cbf(x) / cbf.apply(x) => cbf.newBuilder(x)
    def replaceNewBuilder(tree: Tree, cbf2: Term, x: Term): Patch =
      ctx.replaceTree(
        tree,
        Term.Apply(Term.Select(cbf2, Term.Name("newBuilder")), List(x)).syntax
      )

    val cbfCalls =
      stats
        .map(_.collect {
          // cbf.apply(x)
          case ap @ Term.Apply(sel @ Term.Select(cbf2 @ matchCbf(_), apply), List(x)) =>
            replaceNewBuilder(ap, cbf2, x)

          // cbf(x)
          case ap @ Term.Apply(cbf2 @ matchCbf(_), List(x)) =>
            replaceNewBuilder(ap, cbf2, x)
        }.asPatch)
        .asPatch

    val parameterType =
      ctx.replaceTree(cbf, "BuildFrom")

    parameterType + cbfCalls
  }
}

object CanBuildFromNothing {
  def apply(paramss: List[List[Term.Param]],
            stats: List[Tree],
            ctx: RuleCtx,
            collectionCanBuildFrom: SymbolMatcher,
            nothing: SymbolMatcher,
            toTpe: SymbolMatcher,
            handledTo: mutable.Set[Tree])(implicit index: SemanticdbIndex): Patch = {

    paramss.flatten
      .collect {
        case Term.Param(
            List(Mod.Implicit()),
            param,
            Some(
              tpe @ Type.Apply(
                collectionCanBuildFrom(_),
                List(
                  nothing(_),
                  t,
                  toT
                )
              )
            ),
            _
            ) =>
          new CanBuildFromNothing(param, tpe, t, toT, stats, ctx, toTpe, handledTo)
      }
      .map(_.toFactory)
      .asPatch
  }
}

// example:
// implicit cbf: collection.generic.CanBuildFrom[Nothing, Int, CC[Int]]
//
// param: cbf
// tpe  : collection.generic.CanBuildFrom[Nothing, Int, CC[Int]]
// cbf  : CanBuildFrom
//   v  : Int
// toT  : CC[Int]
case class CanBuildFromNothing(param: Name,
                               tpe: Type.Apply,
                               t: Type,
                               toT: Type,
                               stats: List[Tree],
                               ctx: RuleCtx,
                               toTpe: SymbolMatcher,
                               handledTo: mutable.Set[Tree]) {
  def toFactory(implicit index: SemanticdbIndex): Patch = {
    val matchCbf = SymbolMatcher.exact(ctx.index.symbol(param).get)

    // cbf() / cbf.apply => cbf.newBuilder
    def replaceNewBuilder(tree: Tree, cbf2: Term): Patch =
      ctx.replaceTree(tree, Term.Select(cbf2, Term.Name("newBuilder")).syntax)

    // don't patch cbf.apply twice (cbf.apply and cbf.apply())
    val visitedCbfCalls = mutable.Set[Tree]()

    val cbfCalls =
      stats
        .map(_.collect {
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
        }.asPatch)
        .asPatch

    // e.to[CC] => cbf.fromSpecific(e)
    val toCalls = toT match {
      case cct @ Type.Apply(cc, _) => {
        val matchCC = SymbolMatcher.exact(ctx.index.symbol(cc).get)

        stats
          .map(_.collect {
            case ap @ Term.ApplyType(Term.Select(e, to @ toTpe(_)), List(cc2 @ matchCC(_))) =>
              // e.to[CC](*cbf*) extract implicit parameter
              ctx.index.synthetics
                .find(_.position.end == ap.pos.end)
                .map {
                  synth =>
                    handledTo += to

                    val Term.Apply(_, List(implicitCbf)) = synth.text.parse[Term].get

                    // This is a bit unsafe
                    // https://github.com/scalameta/scalameta/issues/1636
                    if (implicitCbf.syntax == param.syntax) {

                      // .to[CC]
                      val apToRemove =
                        ap.tokens.slice(e.tokens.end - ap.tokens.start, ap.tokens.size)

                      ctx.removeTokens(apToRemove) +
                        ctx.addLeft(e, implicitCbf.syntax + ".fromSpecific(") +
                        ctx.addRight(e, ")")
                    } else Patch.empty
                }
                .getOrElse(Patch.empty)
          }.asPatch)
          .asPatch
      }
      case _ => Patch.empty
    }

    // implicit cbf: collection.generic.CanBuildFrom[Nothing, Int, CC[Int]] =>
    // implicit cbf: Factory[Int, CC[Int]]
    val parameterType =
      ctx.replaceTree(
        tpe,
        Type.Apply(Type.Name("Factory"), List(t, toT)).syntax
      )

    parameterType + cbfCalls + toCalls
  }
}
