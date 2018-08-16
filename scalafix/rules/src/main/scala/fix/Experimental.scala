package fix

import scalafix._
import scalafix.util._
import scala.meta._

// 2.12 Cross-Compatible
case class Experimental(index: SemanticdbIndex) extends SemanticRule(index, "Experimental") {

  val CollectionMap = TypeMatcher(
    "_root_.scala.collection.immutable.Map#",
    "_root_.scala.collection.mutable.Map#",
    "_root_.scala.Predef.Map#"
  )
  val CollectionSet = TypeMatcher("_root_.scala.collection.Set#")

  // == Symbols ==
  val mapZip = exact(
    "_root_.scala.collection.IterableLike#zip(Lscala/collection/GenIterable;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.")
  val mapPlus = exact("_root_.scala.collection.MapLike#`+`(Lscala/Tuple2;)Lscala/collection/Map;.")
  val setPlus = exact(
    "_root_.scala.collection.SetLike#`+`(Ljava/lang/Object;)Lscala/collection/Set;.")
  val setMinus = exact(
    "_root_.scala.collection.SetLike#`-`(Ljava/lang/Object;)Lscala/collection/Set;.")

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
      case Term.ApplyInfix(CollectionSet(), op @ setPlus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "+", "Set")

      case Term.ApplyInfix(CollectionSet(), op @ setMinus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "-", "Set")

      case Term.ApplyInfix(_, op @ mapPlus(_), Nil, List(rhs)) =>
        rewriteOp(op, rhs, "+", "Map")
    }.asPatch
  }

  val unorderingSetOperation = exact(
    "_root_.scala.collection.SetLike#map(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.",
    "_root_.scala.collection.TraversableLike#flatMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;."
  )
  val unorderingMapOperation = exact(
    "_root_.scala.collection.TraversableLike#map(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;.",
    "_root_.scala.collection.TraversableLike#flatMap(Lscala/Function1;Lscala/collection/generic/CanBuildFrom;)Ljava/lang/Object;."
  )

  val OrderedSetCollection = TypeMatcher(
    "_root_.scala.collection.BitSet#",
    "_root_.scala.collection.SortedSet#",
    "_root_.scala.collection.immutable.BitSet#",
    "_root_.scala.collection.immutable.SortedSet#",
    "_root_.scala.collection.immutable.TreeSet#",
    "_root_.scala.collection.mutable.BitSet#",
    "_root_.scala.collection.mutable.SortedSet#",
    "_root_.scala.collection.mutable.TreeSet#"
  )

  val OrderedMapCollection = TypeMatcher(
    "_root_.scala.collection.SortedMap#",
    "_root_.scala.collection.immutable.SortedMap#",
    "_root_.scala.collection.immutable.TreeMap#",
    "_root_.scala.collection.mutable.SortedMap#",
    "_root_.scala.collection.mutable.TreeMap#"
  )

  val mapCanBuildFroms = Set(
    "_root_.scala.collection.Map.canBuildFrom()Lscala/collection/generic/CanBuildFrom;.",
    "_root_.scala.collection.immutable.Map.canBuildFrom()Lscala/collection/generic/CanBuildFrom;.",
    "_root_.scala.collection.mutable.Map.canBuildFrom()Lscala/collection/generic/CanBuildFrom;."
  )

  val setCanBuildFroms = Set(
    "_root_.scala.collection.Set.canBuildFrom()Lscala/collection/generic/CanBuildFrom;.",
    "_root_.scala.collection.immutable.Set.canBuildFrom()Lscala/collection/generic/CanBuildFrom;.",
    "_root_.scala.collection.mutable.Set.canBuildFrom()Lscala/collection/generic/CanBuildFrom;."
  )

  def replaceUnsorted(ctx: RuleCtx): Patch = {

    val synthPos: Map[Int, Synthetic] =
      ctx.index.synthetics.groupBy(_.position.end).mapValues(_.head).toMap

    def recurse(tree: Tree): Tree = {
      tree match {
        case Term.Select(_, b) => recurse(b)
        case _                 => tree
      }
    }

    def syntheticToSymbol(tree: Tree)(extract: PartialFunction[Tree, Tree]): Option[String] = {
      synthPos
        .get(tree.pos.end)
        .flatMap(synthetic =>
          synthetic.text.parse[Term].toOption.flatMap { syntheticTree =>
            extract.lift(syntheticTree).flatMap { col =>
              val byPos =
                synthetic.names
                  .groupBy(s => (s.position.start, s.position.end))
                  .mapValues(_.head)
                  .toMap
              byPos.get((col.pos.start, col.pos.end)).map(_.symbol.toString)
            }
        })
    }

    val patch =
      ctx.tree.collect {
        case ap @ Term.Apply(Term.Select(OrderedMapCollection(), unorderingMapOperation(op)),
                             List(f)) => {
          val cbf =
            syntheticToSymbol(ap) {
              case Term.Apply(_, List(Term.ApplyType(sel, _))) => recurse(sel)
            }

          if (cbf.map(mapCanBuildFroms.contains).getOrElse(false)) {
            ctx.addLeft(op, "unsorted.")
          } else {
            Patch.empty
          }
        }

        case ap @ Term.Apply(Term.Select(OrderedSetCollection(), unorderingSetOperation(op)),
                             List(f)) => {
          val cbf =
            syntheticToSymbol(ap) {
              case Term.Apply(_, List(Term.ApplyType(sel, _))) => recurse(sel)
            }

          if (cbf.map(setCanBuildFroms.contains).getOrElse(false)) {
            ctx.addLeft(op, "unsorted.")
          } else {
            Patch.empty
          }
        }

        case ap @ Term.Apply(Term.Select(OrderedMapCollection(), op), List(f)) =>
          println(ctx.index.symbol(op))
          Patch.empty

      }.asPatch

    val compatImport =
      if (patch.nonEmpty) ctx.addGlobalImport(importer"scala.collection.compat._")
      else Patch.empty

    patch + compatImport
  }

  override def fix(ctx: RuleCtx): Patch =
    replaceSetMapPlusMinus(ctx) +
      replaceMapZip(ctx) +
      replaceUnsorted(ctx)
}
