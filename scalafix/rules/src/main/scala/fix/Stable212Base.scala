package fix

import scalafix._
import scalafix.util._
import scala.meta._

import scala.collection.mutable

import System.{lineSeparator => nl}

trait CrossCompatibility {
  def isCrossCompatible: Boolean
}

// 2.12 Cross-Compatible
trait Stable212Base extends CrossCompatibility { self: SemanticRule =>

  // Two rules triggers the same rewrite TraversableLike.to and CanBuildFrom
  // we keep track of what is handled in CanBuildFrom and guard against TraversableLike.to
  val handledTo = mutable.Set[Tree]()

  //  == Symbols ==
  def foldSymbol(isLeft: Boolean): SymbolMatcher = {
    val op =
      if (isLeft) "/:"
      else ":\\"

    normalized(s"_root_.scala.collection.TraversableOnce.`$op`.")
  }

  val foldLeftSymbol  = foldSymbol(isLeft = true)
  val foldRightSymbol = foldSymbol(isLeft = false)

  val toTpe = normalized(
    "_root_.scala.collection.TraversableLike.to.",
    "_root_.scala.collection.TraversableOnce.to.",
    "_root_.scala.collection.GenTraversableOnce.to.",
    "_root_.scala.collection.parallel.ParIterableLike.to."
  )
  val copyToBuffer = normalized("_root_.scala.collection.TraversableOnce.copyToBuffer.")
  val arrayBuilderMake = normalized(
    "_root_.scala.collection.mutable.ArrayBuilder.make(Lscala/reflect/ClassTag;)Lscala/collection/mutable/ArrayBuilder;.")
  val iterableSameElement = exact(
    "_root_.scala.collection.IterableLike#sameElements(Lscala/collection/GenIterable;)Z.")
  val collectionCanBuildFrom = exact("_root_.scala.collection.generic.CanBuildFrom#")
  val collectionCanBuildFromImport = exact(
    "_root_.scala.collection.generic.CanBuildFrom.;_root_.scala.collection.generic.CanBuildFrom#")
  val nothing = exact("_root_.scala.Nothing#")
  val setPlus2 = exact(
    "_root_.scala.collection.SetLike#`+`(Ljava/lang/Object;Ljava/lang/Object;Lscala/collection/Seq;)Lscala/collection/Set;.")
  val mapPlus2 = exact(
    "_root_.scala.collection.immutable.MapLike#`+`(Lscala/Tuple2;Lscala/Tuple2;Lscala/collection/Seq;)Lscala/collection/immutable/Map;.")
  val mutSetPlus = exact(
    "_root_.scala.collection.mutable.SetLike#`+`(Ljava/lang/Object;)Lscala/collection/mutable/Set;.")
  val mutMapPlus = exact(
    "_root_.scala.collection.mutable.MapLike#`+`(Lscala/Tuple2;)Lscala/collection/mutable/Map;.")
  val mutMapUpdate = exact(
    "_root_.scala.collection.mutable.MapLike#updated(Ljava/lang/Object;Ljava/lang/Object;)Lscala/collection/mutable/Map;.")
  val `Future.onFailure` = exact(
    "_root_.scala.concurrent.Future#onFailure(Lscala/PartialFunction;Lscala/concurrent/ExecutionContext;)V.")
  val `Future.onSuccess` = exact(
    "_root_.scala.concurrent.Future#onSuccess(Lscala/PartialFunction;Lscala/concurrent/ExecutionContext;)V.")

  private val sortedFrom = exact(
    "_root_.scala.collection.generic.Sorted#from(Ljava/lang/Object;)Lscala/collection/generic/Sorted;.",
    "_root_.scala.collection.immutable.TreeMap#from(Ljava/lang/Object;)Lscala/collection/immutable/TreeMap;.",
    "_root_.scala.collection.immutable.TreeSet#from(Ljava/lang/Object;)Lscala/collection/immutable/TreeSet;.",
    "_root_.scala.collection.SortedSetLike#from(Ljava/lang/Object;)Lscala/collection/SortedSet;."
  )

  private val sortedTo = exact(
    "_root_.scala.collection.generic.Sorted#to(Ljava/lang/Object;)Lscala/collection/generic/Sorted;.",
    "_root_.scala.collection.immutable.TreeMap#to(Ljava/lang/Object;)Lscala/collection/immutable/TreeMap;.",
    "_root_.scala.collection.immutable.TreeSet#to(Ljava/lang/Object;)Lscala/collection/immutable/TreeSet;."
  )

  private val sortedUntil = exact(
    "_root_.scala.collection.SortedSetLike#until(Ljava/lang/Object;)Lscala/collection/SortedSet;.",
    "_root_.scala.collection.generic.Sorted#until(Ljava/lang/Object;)Lscala/collection/generic/Sorted;.",
    "_root_.scala.collection.immutable.TreeMap#until(Ljava/lang/Object;)Lscala/collection/immutable/TreeMap;.",
    "_root_.scala.collection.immutable.TreeSet#until(Ljava/lang/Object;)Lscala/collection/immutable/TreeSet;."
  )

  val `TraversableLike.toIterator` = normalized(
    "_root_.scala.collection.TraversableLike.toIterator.")
  val traversableOnce = exact(
    "_root_.scala.collection.TraversableOnce#",
    "_root_.scala.package.TraversableOnce#"
  )

  // == Rules ==

  val breakoutRewrite                      = new BreakoutRewrite(addCompatImport)
  def replaceBreakout(ctx: RuleCtx): Patch = breakoutRewrite(ctx)

  def replaceIterableSameElements(ctx: RuleCtx): Patch = {
    val sameElements =
      ctx.tree.collect {
        case Term.Apply(Term.Select(lhs, iterableSameElement(_)), List(_)) =>
          ctx.addRight(lhs, ".iterator")
      }.asPatch

    val compatImport =
      if (sameElements.nonEmpty) addCompatImport(ctx)
      else Patch.empty

    sameElements + compatImport
  }

  def replaceTraversable(ctx: RuleCtx): Patch = {

    val traversableIterator =
      ctx.tree.collect {
        case `TraversableLike.toIterator`(t: Name) =>
          ctx.replaceTree(t, "iterator")
      }.asPatch

    val traversableToIterable =
      ctx.replaceSymbols(
        "scala.Traversable"                      -> "scala.Iterable",
        "scala.collection.Traversable"           -> "scala.collection.Iterable",
        "scala.collection.immutable.Traversable" -> "scala.collection.immutable.Iterable",
        "scala.collection.mutable.Traversable"   -> "scala.collection.mutable.Iterable",
      )

    val traversableOnceToIterableOnce =
      ctx.tree.collect {
        case Type.Apply(sel @ Type.Select(chain, traversableOnce(n: Name)), _) =>
          val dot = chain.tokens.toList.reverse.drop(1)

          ctx.removeTokens(chain.tokens) +
            ctx.removeTokens(dot) +
            ctx.replaceTree(sel, "IterableOnce")

        case Type.Apply(traversableOnce(n: Name), _) =>
          ctx.replaceTree(n, "IterableOnce")

      }.asPatch

    val compatImport =
      if (traversableOnceToIterableOnce.nonEmpty || traversableIterator.nonEmpty)
        addCompatImport(ctx)
      else Patch.empty

    traversableOnceToIterableOnce + traversableToIterable + traversableIterator + compatImport
  }

  def replaceSymbolicFold(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(ap @ Term.ApplyInfix(rhs, foldRightSymbol(_), _, List(lhs)), _) =>
        ctx.replaceTree(ap, s"$rhs.foldRight($lhs)")

      case Term.Apply(ap @ Term.ApplyInfix(lhs, foldLeftSymbol(_), _, List(rhs)), _) =>
        ctx.replaceTree(ap, s"$rhs.foldLeft($lhs)")
    }.asPatch
  }

  def replaceCopyToBuffer(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case t @ q"${copyToBuffer(Term.Select(collection, _))}($buffer)" =>
        ctx.replaceTree(t, q"$buffer ++= $collection".syntax)
    }.asPatch
  }

  def replaceSetMapPlus2(ctx: RuleCtx): Patch = {
    def rewritePlus(ap: Term.ApplyInfix,
                    lhs: Term,
                    op: Term.Name,
                    rhs1: Term,
                    rhs2: Term): Patch = {
      val tokensToReplace =
        if (startsWithParens(ap)) {
          // don't drop surrounding parens
          ap.tokens.slice(1, ap.tokens.size - 1)
        } else ap.tokens

      val newTree =
        Term
          .ApplyInfix(
            Term.ApplyInfix(lhs, op, Nil, List(rhs1)),
            op,
            Nil,
            List(rhs2)
          )
          .syntax

      ctx.removeTokens(tokensToReplace) +
        tokensToReplace.headOption.map(x => ctx.addRight(x, newTree))
    }
    ctx.tree.collect {
      case ap @ Term.ApplyInfix(lhs, op @ mapPlus2(_), _, List(a, b)) =>
        rewritePlus(ap, lhs, op, a, b)

      case ap @ Term.ApplyInfix(lhs, op @ setPlus2(_), _, List(a, b)) =>
        rewritePlus(ap, lhs, op, a, b)
    }.asPatch
  }

  def replaceMutSetMapPlus(ctx: RuleCtx): Patch = {
    def rewriteMutPlus(lhs: Term, op: Term.Name): Patch = {
      ctx.addRight(lhs, ".clone()") +
        ctx.addRight(op, "=")
    }

    ctx.tree.collect {
      case Term.ApplyInfix(lhs, op @ mutSetPlus(_), _, List(_)) =>
        rewriteMutPlus(lhs, op)

      case Term.ApplyInfix(lhs, op @ mutMapPlus(_), _, List(_)) =>
        rewriteMutPlus(lhs, op)
    }.asPatch
  }

  def replaceMutMapUpdated(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case Term.Apply(Term.Select(a, up @ mutMapUpdate(_)), List(k, v)) => {
        ctx.addRight(up, "clone() += (") +
          ctx.removeTokens(up.tokens) +
          ctx.addRight(v, ")")
      }
    }.asPatch
  }

  def replaceArrayBuilderMake(ctx: RuleCtx): Patch = {
    ctx.tree.collect {
      case ap @ Term.Apply(at @ Term.ApplyType(Term.Select(lhs, arrayBuilderMake(_)), args), Nil) =>
        val extraParens =
          ap.tokens.slice(at.tokens.size, ap.tokens.size)
        ctx.removeTokens(extraParens)
    }.asPatch
  }

  def replaceCanBuildFrom(ctx: RuleCtx): Patch = {
    val useSites =
      ctx.tree.collect {
        case Defn.Def(_, _, _, paramss, _, body) =>
          CanBuildFromNothing(paramss,
                              List(body),
                              ctx,
                              collectionCanBuildFrom,
                              nothing,
                              toTpe,
                              handledTo) +
            CanBuildFrom(paramss, List(body), ctx, collectionCanBuildFrom, nothing)
        case Defn.Class(_, _, _, Ctor.Primary(_, _, paramss), Template(_, _, _, stats)) =>
          CanBuildFromNothing(paramss,
                              stats,
                              ctx,
                              collectionCanBuildFrom,
                              nothing,
                              toTpe,
                              handledTo) +
            CanBuildFrom(paramss, stats, ctx, collectionCanBuildFrom, nothing)
      }.asPatch

    val imports =
      ctx.tree.collect {
        case i: Importee if collectionCanBuildFromImport.matches(i) =>
          ctx.removeImportee(i)
      }.asPatch

    if (useSites.nonEmpty) {
      val compatImport = addCompatImport(ctx)
      useSites + imports + compatImport
    } else Patch.empty
  }

  def extractCollection(toCol: Tree): String = {
    toCol match {
      case Term.ApplyType(q"scala.Predef.fallbackStringCanBuildFrom", _) =>
        "scala.collection.immutable.IndexedSeq"
      case Term.ApplyType(Term.Select(coll, _), _) =>
        coll.syntax
      case Term.Apply(Term.ApplyType(Term.Select(coll, _), _), _) =>
        coll.syntax
      case Term.Select(coll, _) =>
        coll.syntax
      case coll: Type.Name =>
        coll.syntax
      case _ => {
        throw new Exception(
          s"""|cannot extract collection from .to
              |
              |---------------------------------------------
              |syntax:
              |${toCol.syntax}
              |
              |---------------------------------------------
              |structure:
              |${toCol.structure}""".stripMargin
        )
      }
    }
  }

  def replaceTo(ctx: RuleCtx): Patch = {
    val patch =
      ctx.tree.collect {
        case Term.ApplyType(Term.Select(_, t @ toTpe(n: Name)), _) if !handledTo.contains(n) =>
          trailingBrackets(n, ctx).map {
            case (open, close) =>
              ctx.replaceToken(open, "(") + ctx.replaceToken(close, ")")
          }.asPatch

        case t @ Term.Select(_, to @ toTpe(n: Name)) if !handledTo.contains(n) =>
          val synth = ctx.index.synthetics.find(_.position.end == to.pos.end)
          synth
            .map { s =>
              s.text.parse[Term].get match {
                // we only want f.to, not f.to(X)
                case Term.Apply(_, List(toCol)) =>
                  val col = extractCollection(toCol)
                  ctx.addRight(to, "(" + col + ")")
                case _ => Patch.empty
              }
            }
            .getOrElse(Patch.empty)

      }.asPatch

    val compatImport =
      if (patch.nonEmpty) addCompatImport(ctx)
      else Patch.empty

    compatImport + patch
  }

  def replaceFuture(ctx: RuleCtx): Patch = {

    def toOnCompletePF(f: Tree, cases: List[Tree], tryApply: String): Patch = {
      val indent = " " * cases.head.pos.startColumn

      ctx.replaceTree(f, "onComplete") +
        cases.map {
          case Case(c, _, _) =>
            ctx.addLeft(c, tryApply + "(") +
              ctx.addRight(c, ")")
        }.asPatch +
        ctx.addRight(cases.last, nl + indent + "case _ => ()")
    }

    val toOnCompelete =
      ctx.tree.collect {
        case Term.Apply(Term.Select(_, f @ `Future.onFailure`(_)),
                        List(Term.PartialFunction(cases))) =>
          toOnCompletePF(f, cases, "scala.util.Failure")

        case Term.Apply(Term.Select(_, f @ `Future.onSuccess`(_)),
                        List(Term.PartialFunction(cases))) =>
          toOnCompletePF(f, cases, "scala.util.Success")
      }.asPatch

    val toFuture = ctx.replaceSymbols(
      "scala.concurrent.future" -> "scala.concurrent.Future"
    )

    toOnCompelete + toFuture
  }

  private def replaceSorted(ctx: RuleCtx): Patch = {
    val replaced =
      ctx.tree.collect {
        case Term.Apply(Term.Select(_, op @ sortedFrom(_)), _)  => ctx.replaceTree(op, "rangeFrom")
        case Term.Apply(Term.Select(_, op @ sortedTo(_)), _)    => ctx.replaceTree(op, "rangeTo")
        case Term.Apply(Term.Select(_, op @ sortedUntil(_)), _) => ctx.replaceTree(op, "rangeUntil")
      }.asPatch

    val compatImport =
      if (replaced.nonEmpty) addCompatImport(ctx)
      else Patch.empty

    replaced + compatImport
  }

  val companionSuffix = "#companion()Lscala/collection/generic/GenericCompanion;."
  val companion = {
    val cols =
      Set(
        "_root_.scala.collection." -> List(
          "IndexedSeq",
          "Iterable",
          "LinearSeq",
          "Seq",
          "Set",
          "Traversable"
        ),
        "_root_.scala.collection.immutable." -> List(
          "HashSet",
          "IndexedSeq",
          "Iterable",
          "LinearSeq",
          "List",
          "ListSet",
          "Queue",
          "Seq",
          "Set",
          "Stack",
          "Stream",
          "Traversable",
          "Vector"
        ),
        "_root_.scala.collection.mutable." -> List(
          "ArrayBuffer",
          "ArraySeq",
          "Buffer",
          "DoubleLinkedList",
          "HashSet",
          "IndexedSeq",
          "Iterable",
          "LinearSeq",
          "LinkedHashSet",
          "LinkedList",
          "ListBuffer",
          "MutableList",
          "Queue",
          "ResizableArray",
          "Seq",
          "Set",
          "Traversable",
        )
      ).flatMap {
        case (prefix, cols) =>
          cols.map(col => prefix + col + companionSuffix)
      }

    val specific =
      Set(
        "_root_.scala.collection.mutable.Stack#companion()Lscala/collection/mutable/Stack;.",
        "_root_.scala.collection.mutable.ArrayStack#companion()Lscala/collection/mutable/ArrayStack;."
      )

    exact((cols ++ specific).toSeq: _*)
  }

  val classManifestCompanion = exact(
    "_root_.scala.collection.generic.GenericClassTagTraversableTemplate#classManifestCompanion()Lscala/collection/generic/GenericClassTagCompanion;."
  )

  private def replaceCompanion(ctx: RuleCtx): Patch = {
    val replaced =
      ctx.tree.collect {
        case Term.Select(_, t @ companion(_)) => {
          ctx.replaceTree(t, "iterableFactory")
        }
        case Term.Select(_, t @ classManifestCompanion(_)) => {
          ctx.replaceTree(t, "classTagCompanion")
        }
      }.asPatch

    val compatImport =
      if (replaced.nonEmpty) addCompatImport(ctx)
      else Patch.empty

    replaced + compatImport
  }

  private val compatImportAdded = mutable.Set[Input]()

  def addCompatImport(ctx: RuleCtx): Patch = {
    if (isCrossCompatible && !compatImportAdded.contains(ctx.input)) {
      compatImportAdded += ctx.input
      ctx.addGlobalImport(importer"scala.collection.compat._")
    } else {
      Patch.empty
    }
  }

  private val deprecatedAsJavaConversions: Map[Symbol, String] =
    List(
      "asJava" -> List(
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#asJavaIterable(Lscala/collection/Iterable;)Ljava/lang/Iterable;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#asJavaIterator(Lscala/collection/Iterator;)Ljava/util/Iterator;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#bufferAsJavaList(Lscala/collection/mutable/Buffer;)Ljava/util/List;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#mapAsJavaConcurrentMap(Lscala/collection/concurrent/Map;)Ljava/util/concurrent/ConcurrentMap;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#mapAsJavaMap(Lscala/collection/Map;)Ljava/util/Map;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#mutableMapAsJavaMap(Lscala/collection/mutable/Map;)Ljava/util/Map;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#mutableSeqAsJavaList(Lscala/collection/mutable/Seq;)Ljava/util/List;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#mutableSetAsJavaSet(Lscala/collection/mutable/Set;)Ljava/util/Set;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#seqAsJavaList(Lscala/collection/Seq;)Ljava/util/List;.",
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#setAsJavaSet(Lscala/collection/Set;)Ljava/util/Set;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated asJavaIterable`(Lscala/collection/Iterable;)Ljava/lang/Iterable;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated asJavaIterator`(Lscala/collection/Iterator;)Ljava/util/Iterator;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated bufferAsJavaList`(Lscala/collection/mutable/Buffer;)Ljava/util/List;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated mapAsJavaConcurrentMap`(Lscala/collection/concurrent/Map;)Ljava/util/concurrent/ConcurrentMap;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated mapAsJavaMap`(Lscala/collection/Map;)Ljava/util/Map;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated mutableMapAsJavaMap`(Lscala/collection/mutable/Map;)Ljava/util/Map;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated mutableMapAsJavaMap`(Lscala/collection/mutable/Map;)Ljava/util/Map;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated mutableSeqAsJavaList`(Lscala/collection/mutable/Seq;)Ljava/util/List;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated mutableSetAsJavaSet`(Lscala/collection/mutable/Set;)Ljava/util/Set;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated seqAsJavaList`(Lscala/collection/Seq;)Ljava/util/List;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated setAsJavaSet`(Lscala/collection/Set;)Ljava/util/Set;."
      ),
      "asJavaEnumeration" -> List(
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#asJavaEnumeration(Lscala/collection/Iterator;)Ljava/util/Enumeration;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated asJavaEnumeration`(Lscala/collection/Iterator;)Ljava/util/Enumeration;.",
      ),
      "asJavaCollection" -> List(
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#asJavaCollection(Lscala/collection/Iterable;)Ljava/util/Collection;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated asJavaCollection`(Lscala/collection/Iterable;)Ljava/util/Collection;.",
      ),
      "asJavaDictionary" -> List(
        "_root_.scala.collection.convert.LowPriorityWrapAsJava#asJavaDictionary(Lscala/collection/mutable/Map;)Ljava/util/Dictionary;.",
        "_root_.scala.collection.convert.WrapAsJava#`deprecated asJavaDictionary`(Lscala/collection/mutable/Map;)Ljava/util/Dictionary;.",
      )
    ).flatMap {
      case (asX, symbols) =>
        symbols.map(s => Symbol(s) -> asX)
    }.toMap

  private val deprecatedAsScalaConversions: Map[Symbol, String] =
    List(
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#asScalaBuffer(Ljava/util/List;)Lscala/collection/mutable/Buffer;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#asScalaIterator(Ljava/util/Iterator;)Lscala/collection/Iterator;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#asScalaSet(Ljava/util/Set;)Lscala/collection/mutable/Set;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#collectionAsScalaIterable(Ljava/util/Collection;)Lscala/collection/Iterable;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#dictionaryAsScalaMap(Ljava/util/Dictionary;)Lscala/collection/mutable/Map;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#enumerationAsScalaIterator(Ljava/util/Enumeration;)Lscala/collection/Iterator;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#iterableAsScalaIterable(Ljava/lang/Iterable;)Lscala/collection/Iterable;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#mapAsScalaConcurrentMap(Ljava/util/concurrent/ConcurrentMap;)Lscala/collection/concurrent/Map;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#mapAsScalaMap(Ljava/util/Map;)Lscala/collection/mutable/Map;.",
      "_root_.scala.collection.convert.LowPriorityWrapAsScala#propertiesAsScalaMap(Ljava/util/Properties;)Lscala/collection/mutable/Map;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated asScalaBuffer`(Ljava/util/List;)Lscala/collection/mutable/Buffer;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated asScalaIterator`(Ljava/util/Iterator;)Lscala/collection/Iterator;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated asScalaSet`(Ljava/util/Set;)Lscala/collection/mutable/Set;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated collectionAsScalaIterable`(Ljava/util/Collection;)Lscala/collection/Iterable;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated dictionaryAsScalaMap`(Ljava/util/Dictionary;)Lscala/collection/mutable/Map;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated enumerationAsScalaIterator`(Ljava/util/Enumeration;)Lscala/collection/Iterator;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated iterableAsScalaIterable`(Ljava/lang/Iterable;)Lscala/collection/Iterable;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated mapAsScalaConcurrentMap`(Ljava/util/concurrent/ConcurrentMap;)Lscala/collection/concurrent/Map;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated mapAsScalaMap`(Ljava/util/Map;)Lscala/collection/mutable/Map;.",
      "_root_.scala.collection.convert.WrapAsScala#`deprecated propertiesAsScalaMap`(Ljava/util/Properties;)Lscala/collection/mutable/Map;.",
    ).map(s => Symbol(s) -> "asScala").toMap

  private class DeprecatedImplicitConversion(ctx: RuleCtx) {
    private def collect(deprecatedSymbols: Map[Symbol, String]): Map[Position, String] = {
      def deprecatedConversion(synthetic: Synthetic): Option[String] = {
        var found: Option[String] = None
        synthetic.names.find { name =>
          found = deprecatedSymbols.get(name.symbol)
          found.nonEmpty
        }
        found
      }

      ctx.index.synthetics
        .flatMap(s => deprecatedConversion(s).map(asX => s.position -> asX))
        .toMap
    }

    val asScalaConvertions = collect(deprecatedAsScalaConversions)
    val asJavaConvertions  = collect(deprecatedAsJavaConversions)

    def unapply(tree: Tree): Option[String] = {
      val pos = tree.pos
      asJavaConvertions
        .get(pos)
        .orElse(
          asScalaConvertions.get(pos)
        )
    }
  }

  private class DeprecatedExplicitConversion(ctx: RuleCtx) {
    def unapply(tree: Tree): Option[String] = {
      ctx.index
        .symbol(tree)
        .flatMap(
          symbol =>
            deprecatedAsJavaConversions
              .get(symbol)
              .orElse(
                deprecatedAsScalaConversions.get(symbol)
            ))
    }
  }

  private val JavaConversions = exact("_root_.scala.collection.JavaConversions.")
  private val JavaConversionsImport = exact(
    "_root_.scala.collection.JavaConversions.;_root_.scala.collection.JavaConversions#")

  def replaceJavaConversions(ctx: RuleCtx): Patch = {
    val ImplicitConversion = new DeprecatedImplicitConversion(ctx)
    val ExplicitConversion = new DeprecatedExplicitConversion(ctx)

    def explicitToAsX(ap: Term.Apply, rhs: Term, asX: String): Patch = {
      // ap: f(x), rhs: x, left: f(, right: )
      val left  = ap.tokens.slice(0, rhs.tokens.start - ap.tokens.start)
      val right = ap.tokens.slice(rhs.tokens.end - ap.tokens.start, ap.tokens.size)
      ctx.removeTokens(left) +
        ctx.addRight(rhs, "." + asX) +
        ctx.removeTokens(right)
    }

    val patch =
      ctx.tree.collect {
        // ex: juSet: Set[Int]
        case tree @ ImplicitConversion(asX) =>
          ctx.addRight(tree, "." + asX)

        // ex: mapAsScalaMap(juMap)
        case ap @ Term.Apply(ExplicitConversion(asX), List(rhs)) =>
          explicitToAsX(ap, rhs, asX)

        case Importer(JavaConversions(_), importees) =>
          importees.map(ctx.removeImportee).asPatch

        case i @ Importee.Name(JavaConversionsImport(_)) =>
          ctx.removeImportee(i)

      }.asPatch

    val converterImport =
      if (patch.nonEmpty) ctx.addGlobalImport(importer"scala.collection.JavaConverters._")
      else Patch.empty

    patch + converterImport
  }

  override def fix(ctx: RuleCtx): Patch = {
    replaceTraversable(ctx) +
      replaceCanBuildFrom(ctx) +
      replaceTo(ctx) +
      replaceCopyToBuffer(ctx) +
      replaceSymbolicFold(ctx) +
      replaceSetMapPlus2(ctx) +
      replaceMutSetMapPlus(ctx) +
      replaceMutMapUpdated(ctx) +
      replaceArrayBuilderMake(ctx) +
      replaceIterableSameElements(ctx) +
      replaceBreakout(ctx) +
      replaceFuture(ctx) +
      replaceSorted(ctx) +
      replaceJavaConversions(ctx) +
      replaceCompanion(ctx)
  }
}
