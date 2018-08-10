package fix

import scalafix.v0._
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

    normalized(s"scala/collection/TraversableOnce.`$op`.")
  }

  val foldLeftSymbol  = foldSymbol(isLeft = true)
  val foldRightSymbol = foldSymbol(isLeft = false)

  val toTpe = normalized(
    "scala/collection/TraversableLike#to.",
    "scala/collection/TraversableOnce#to.",
    "scala/collection/GenTraversableOnce#to.",
    "scala/collection/parallel/ParIterableLike#to."
  )
  val copyToBuffer           = normalized("scala/collection/TraversableOnce/copyToBuffer().")
  val arrayBuilderMake       = normalized("scala/collection/mutable/ArrayBuilder.make().")
  val iterableSameElement    = exact("scala/collection/IterableLike#sameElements().")
  val collectionCanBuildFrom = exact("scala/collection/generic/CanBuildFrom#")

  val nothing      = exact("scala/Nothing#")
  val setPlus2     = exact("scala/collection/SetLike#`+`(+1).")
  val mapPlus2     = exact("scala/collection/immutable/MapLike#`+`(+1).")
  val mutSetPlus   = exact("scala/collection/mutable/SetLike#`+`().")
  val mutMapPlus   = exact("scala/collection/mutable/MapLike#`+`().")
  val mutMapUpdate = exact("scala/collection/mutable/MapLike#updated().")

  val `Future.onFailure` = exact("scala/concurrent/Future#onFailure().")
  val `Future.onSuccess` = exact("scala/concurrent/Future#onSuccess().")

  private val sortedFrom = exact(
    "scala/collection/generic/Sorted#from().",
    "scala/collection/immutable/TreeMap#from().",
    "scala/collection/SortedSetLike#from()."
  )
  private val sortedTo = exact(
    "scala/collection/generic/Sorted#to().",
    "scala/collection/immutable/TreeMap#to()."
  )
  private val sortedUntil = exact(
    "scala/collection/generic/Sorted#until().",
    "scala/collection/immutable/TreeMap#until().",
    "scala/collection/SortedSetLike#until()."
  )

  val `TraversableLike.toIterator` = exact("scala/collection/TraversableLike#toIterator().")
  val traversableOnce = exact(
    "scala/collection/TraversableOnce#",
    "scala/package.TraversableOnce#"
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
        case i: Importee if collectionCanBuildFrom.matches(i) =>
          ctx.removeImportee(i)
      }.asPatch

    if (useSites.nonEmpty) {
      val compatImport = addCompatImport(ctx)
      useSites + imports + compatImport
    } else Patch.empty
  }

  def extractCollection(toCol: Tree): String = {
    toCol match {
      // case Term.ApplyType(q"scala.Predef.fallbackStringCanBuildFrom", _) =>
      //   "scala.collection.immutable.IndexedSeq"
      // case Term.ApplyType(Term.Select(coll, _), _) =>
      //   coll.syntax
      // case Term.Apply(Term.ApplyType(Term.Select(coll, _), _), _) =>
      //   coll.syntax
      // case Term.Select(coll, _) =>
      //   coll.syntax
      // case coll: Type.Name =>
      //   coll.syntax
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
    val syntheticsByEndPos: Map[Int, Seq[Synthetic]] =
      ctx.index.synthetics.groupBy(_.position.end)

    val patch =
      ctx.tree.collect {
        case Term.ApplyType(Term.Select(_, t @ toTpe(n: Name)), _) if !handledTo.contains(n) =>
          trailingBrackets(n, ctx).map {
            case (open, close) =>
              ctx.replaceToken(open, "(") + ctx.replaceToken(close, ")")
          }.asPatch

        // https://github.com/scalacenter/scalafix/issues/793
        case t @ Term.Select(_, to @ toTpe(n: Name)) if !handledTo.contains(n) =>
          if (t.parent.map(_.isNot[Term.ApplyType]).getOrElse(false)) {
            val toCollection = extractCollectionFromBreakout(t, syntheticsByEndPos)
            toCollection.map(toCol => ctx.addRight(to, "(" + toCol + ")")).getOrElse(Patch.empty)
          } else Patch.empty

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

  val companionSuffix = "#companion()."
  val companion = {
    val cols =
      Set(
        "scala/collection/" -> List(
          "IndexedSeq",
          "Iterable",
          "LinearSeq",
          "Seq",
          "Set",
          "Traversable"
        ),
        "scala/collection/immutable/" -> List(
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
        "scala/collection/mutable/" -> List(
          "ArrayBuffer",
          "ArraySeq",
          "ArrayStack",
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
          "Stack",
          "Traversable",
        )
      ).flatMap {
        case (prefix, cols) =>
          cols.map(col => prefix + col + companionSuffix)
      }

    exact(cols.toSeq: _*)
  }

  private def replaceCompanion(ctx: RuleCtx): Patch = {
    val replaced =
      ctx.tree.collect {
        case Term.Select(_, t @ companion(_)) => {
          ctx.replaceTree(t, "iterableFactory")
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
        "scala/collection/convert/LowPriorityWrapAsJava#asJavaIterable().",
        "scala/collection/convert/LowPriorityWrapAsJava#asJavaIterator().",
        "scala/collection/convert/LowPriorityWrapAsJava#bufferAsJavaList().",
        "scala/collection/convert/LowPriorityWrapAsJava#mapAsJavaConcurrentMap().",
        "scala/collection/convert/LowPriorityWrapAsJava#mapAsJavaMap().",
        "scala/collection/convert/LowPriorityWrapAsJava#mutableMapAsJavaMap().",
        "scala/collection/convert/LowPriorityWrapAsJava#mutableSeqAsJavaList().",
        "scala/collection/convert/LowPriorityWrapAsJava#mutableSetAsJavaSet().",
        "scala/collection/convert/LowPriorityWrapAsJava#seqAsJavaList().",
        "scala/collection/convert/LowPriorityWrapAsJava#setAsJavaSet().",
        "scala/collection/convert/WrapAsJava#`deprecated asJavaIterable`().",
        "scala/collection/convert/WrapAsJava#`deprecated asJavaIterator`().",
        "scala/collection/convert/WrapAsJava#`deprecated bufferAsJavaList`().",
        "scala/collection/convert/WrapAsJava#`deprecated mapAsJavaConcurrentMap`().",
        "scala/collection/convert/WrapAsJava#`deprecated mapAsJavaMap`().",
        "scala/collection/convert/WrapAsJava#`deprecated mutableMapAsJavaMap`().",
        "scala/collection/convert/WrapAsJava#`deprecated mutableMapAsJavaMap`().",
        "scala/collection/convert/WrapAsJava#`deprecated mutableSeqAsJavaList`().",
        "scala/collection/convert/WrapAsJava#`deprecated mutableSetAsJavaSet`().",
        "scala/collection/convert/WrapAsJava#`deprecated seqAsJavaList`().",
        "scala/collection/convert/WrapAsJava#`deprecated setAsJavaSet`()."
      ),
      "asJavaEnumeration" -> List(
        "scala/collection/convert/LowPriorityWrapAsJava#asJavaEnumeration().",
        "scala/collection/convert/WrapAsJava#`deprecated asJavaEnumeration`().",
      ),
      "asJavaCollection" -> List(
        "scala/collection/convert/LowPriorityWrapAsJava#asJavaCollection().",
        "scala/collection/convert/WrapAsJava#`deprecated asJavaCollection`().",
      ),
      "asJavaDictionary" -> List(
        "scala/collection/convert/LowPriorityWrapAsJava#asJavaDictionary().",
        "scala/collection/convert/WrapAsJava#`deprecated asJavaDictionary`().",
      )
    ).flatMap {
      case (asX, symbols) =>
        symbols.map(s => Symbol(s) -> asX)
    }.toMap

  private val deprecatedAsScalaConversions: Map[Symbol, String] =
    List(
      "scala/collection/convert/LowPriorityWrapAsScala#asScalaBuffer().",
      "scala/collection/convert/LowPriorityWrapAsScala#asScalaIterator().",
      "scala/collection/convert/LowPriorityWrapAsScala#asScalaSet().",
      "scala/collection/convert/LowPriorityWrapAsScala#collectionAsScalaIterable().",
      "scala/collection/convert/LowPriorityWrapAsScala#dictionaryAsScalaMap().",
      "scala/collection/convert/LowPriorityWrapAsScala#enumerationAsScalaIterator().",
      "scala/collection/convert/LowPriorityWrapAsScala#iterableAsScalaIterable().",
      "scala/collection/convert/LowPriorityWrapAsScala#mapAsScalaConcurrentMap().",
      "scala/collection/convert/LowPriorityWrapAsScala#mapAsScalaMap().",
      "scala/collection/convert/LowPriorityWrapAsScala#propertiesAsScalaMap().",
      "scala/collection/convert/WrapAsScala#`deprecated asScalaBuffer`().",
      "scala/collection/convert/WrapAsScala#`deprecated asScalaIterator`().",
      "scala/collection/convert/WrapAsScala#`deprecated asScalaSet`().",
      "scala/collection/convert/WrapAsScala#`deprecated collectionAsScalaIterable`().",
      "scala/collection/convert/WrapAsScala#`deprecated dictionaryAsScalaMap`().",
      "scala/collection/convert/WrapAsScala#`deprecated enumerationAsScalaIterator`().",
      "scala/collection/convert/WrapAsScala#`deprecated iterableAsScalaIterable`().",
      "scala/collection/convert/WrapAsScala#`deprecated mapAsScalaConcurrentMap`().",
      "scala/collection/convert/WrapAsScala#`deprecated mapAsScalaMap`().",
      "scala/collection/convert/WrapAsScala#`deprecated propertiesAsScalaMap`().",
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

  private val JavaConversions = exact("scala/collection/JavaConversions.")

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

        case i @ Importee.Name(JavaConversions(_)) =>
          ctx.removeImportee(i)

      }.asPatch

    val converterImport =
      if (patch.nonEmpty) ctx.addGlobalImport(importer"scala.collection.JavaConverters._")
      else Patch.empty

    patch + converterImport
  }

  override def fix(ctx: RuleCtx): Patch = {
    // println("-----")
    // ctx.index.synthetics.sortBy(_.position.start).foreach(println)

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
