package test.scala.collection

import org.junit.{Assert, Test}

import scala.collection.compat._

import scala.collection.{immutable => i, mutable => m}
import scala.{collection => c}

object Data {
  // scala
  val some   = Some(1)
  val none   = None
  val option = Option(1)
  val array  = Array(1)
  val string = "a"

  // scala.collection
  val cBitSet           = c.BitSet(1)
  val cBufferedIterator = scala.io.Source.fromString("a").buffered
  val cIndexedSeq       = c.IndexedSeq(1)
  val cIndexedSeqVie    = c.IndexedSeq(1).view
  val cIterable         = c.Iterable(1)
  // val cIterableOnce     = (c.Iterable(1): c.IterableOnce[Int])
  val cIterator = c.Iterator(1)
  // val lazyZip2          = (List(1) lazyZip List(2))
  // val lazyZip3          = (List(1) lazyZip List(2) lazyZip List(3))
  // val lazyZip4          = (List(1) lazyZip List(2) lazyZip List(3) lazyZip List(4))
  val cLinearSeq = c.LinearSeq(1)
  val cMap       = c.Map(1 -> 1)
  val cMapView   = c.Map(1 -> 1).view
  val cSeq       = c.Seq(1)
  val cSeqView   = c.Seq(1).view
  val cSet       = c.Set(1)
  val cSortedMap = c.SortedMap(1 -> 1)
  val cSortedSet = c.SortedSet(1)
  // val cView             = (c.Seq(1).view: c.View[Int])

  // scala.collection.immutable
  // val iArraySeq       = i.ArraySeq(1)
  val iBitSet = i.BitSet(1)
  // val iChampHashMap   = i.ChampHashMap(1 -> 1)
  // val iChampHashSet   = i.ChampHashSet(1)
  val iHashMap    = i.HashMap(1 -> 1)
  val iHashSet    = i.HashSet(1)
  val iIndexedSeq = i.IndexedSeq(1)
  val iIntMap     = i.IntMap(1 -> 1)
  val iIterable   = i.Iterable(1)
  // val iLazyList       = i.LazyList(1)
  val iLinearSeq      = i.LinearSeq(1)
  val iList           = i.List(1)
  val iListMap        = i.ListMap(1 -> 1)
  val iListSet        = i.ListSet(1)
  val iLongMap        = i.LongMap(1L -> 1)
  val iMap            = i.Map(1 -> 1)
  val iNil            = i.Nil
  val iNumericRange   = 1L to 1L
  val iQueue          = i.Queue(1)
  val iRange          = 1 to 1
  val iSeq            = i.Seq(1)
  val iSet            = i.Set(1)
  val iSortedMap      = i.SortedMap(1 -> 1)
  val iSortedSet      = i.SortedSet(1)
  val iStream         = i.Stream(1)
  val iTreeMap        = i.TreeMap(1 -> 1)
  val iTreeSet        = i.TreeSet(1)
  val iVector         = i.Vector(1)
  val iVectorIterator = i.Vector(1).iterator
  // val iWrappedString  = i.WrappedString('a')

  // scala.collection.mutable
  val mAnyRefMap       = m.AnyRefMap(Nil -> 1)
  val mArrayBuffer     = m.ArrayBuffer(1)
  val mArrayBufferView = m.ArrayBuffer(1).view
  // val mArrayDeque             = m.ArrayDeque(1)
  // val mArraySeq               = m.ArraySeq(1)
  val mBitSet         = m.BitSet(1)
  val mBuffer         = m.Buffer(1)
  val mHashMap        = m.HashMap(1 -> 1)
  val mHashSet        = m.HashSet(1)
  val mIndexedSeq     = m.IndexedSeq(1)
  val mIterable       = m.Iterable(1)
  val mLinkedHashMap  = m.LinkedHashMap(1 -> 1)
  val mLinkedHashSet  = m.LinkedHashSet(1)
  val mListBuffer     = m.ListBuffer(1)
  val mListMap        = m.ListMap(1 -> 1)
  val mLongMap        = m.LongMap(1L -> 1)
  val mMap            = m.Map(1 -> 1)
  val mMultiMap       = new m.HashMap[Int, m.Set[Int]] with m.MultiMap[Int, Int]
  val mOpenHashMap    = m.OpenHashMap(1 -> 1)
  val mPriorityQueue  = m.PriorityQueue(1)
  val mQueue          = m.Queue(1)
  val mSeq            = m.Seq(1)
  val mSet            = m.Set(1)
  val mSortedMap      = m.SortedMap(1 -> 1)
  val mSortedSet      = m.SortedSet(1)
  val mStack          = m.Stack(1)
  val mStringBuilder  = new m.StringBuilder()
  val mTreeMap        = m.TreeMap(1 -> 1)
  val mTreeSet        = m.TreeSet(1)
  val mUnrolledBuffer = m.UnrolledBuffer(1)
  val mWeakHashMap    = m.WeakHashMap(1 -> 1)

  // scala.io
  val bufferedSource = scala.io.Source.fromFile(new java.io.File("../../../build.sbt"))
  val source         = scala.io.Source.fromString("hello")

  // scala.runtime
  val tuple2Zipped = (List(1), List(2)).zipped
  val tuple3Zipped = (List(1), List(2), List(3)).zipped
  // val zippedIterable2  = (tuple2Zipped: runtime.ZippedIterable2[Int, Int])
  // val zippedIterable3  = (tuple3Zipped: runtime.ZippedIterable3[Int, Int, Int])

  // scala.sys
  val systemProperties = sys.props
}

class ExtensionsTest {
  @Test
  def knownSize(): Unit = {

    import Data._

    val in: List[(String, Boolean)] = List(
      ("some", some.hasDefiniteSize),
      ("none", none.hasDefiniteSize),
      ("option", option.hasDefiniteSize),
      ("array", array.hasDefiniteSize),
      ("string", string.hasDefiniteSize),
      ("cBitSet", cBitSet.hasDefiniteSize),
      ("cBufferedIterator", cBufferedIterator.hasDefiniteSize),
      ("cIndexedSeq", cIndexedSeq.hasDefiniteSize),
      ("cIndexedSeqVie", cIndexedSeqVie.hasDefiniteSize),
      ("cIterable", cIterable.hasDefiniteSize),
      // ("cIterableOnce",            cIterableOnce.hasDefiniteSize),
      ("cIterator", cIterator.hasDefiniteSize),
      // ("lazyZip2",                 lazyZip2.hasDefiniteSize),
      // ("lazyZip3",                 lazyZip3.hasDefiniteSize),
      // ("lazyZip4",                 lazyZip4.hasDefiniteSize),
      ("cLinearSeq", cLinearSeq.hasDefiniteSize),
      ("cMap", cMap.hasDefiniteSize),
      ("cMapView", cMapView.hasDefiniteSize),
      ("cSeq", cSeq.hasDefiniteSize),
      ("cSeqView", cSeqView.hasDefiniteSize),
      ("cSet", cSet.hasDefiniteSize),
      ("cSortedMap", cSortedMap.hasDefiniteSize),
      ("cSortedSet", cSortedSet.hasDefiniteSize),
      // ("cView",                    cView.hasDefiniteSize),
      // ("iArraySeq",                iArraySeq.hasDefiniteSize),
      ("iBitSet", iBitSet.hasDefiniteSize),
      // ("iChampHashMap",            iChampHashMap.hasDefiniteSize),
      // ("iChampHashSet",            iChampHashSet.hasDefiniteSize),
      ("iHashMap", iHashMap.hasDefiniteSize),
      ("iHashSet", iHashSet.hasDefiniteSize),
      ("iIndexedSeq", iIndexedSeq.hasDefiniteSize),
      ("iIntMap", iIntMap.hasDefiniteSize),
      ("iIterable", iIterable.hasDefiniteSize),
      // ("iLazyList",                iLazyList.hasDefiniteSize),
      ("iLinearSeq", iLinearSeq.hasDefiniteSize),
      ("iList", iList.hasDefiniteSize),
      ("iListMap", iListMap.hasDefiniteSize),
      ("iListSet", iListSet.hasDefiniteSize),
      ("iLongMap", iLongMap.hasDefiniteSize),
      ("iMap", iMap.hasDefiniteSize),
      ("iNil", iNil.hasDefiniteSize),
      ("iNumericRange", iNumericRange.hasDefiniteSize),
      ("iQueue", iQueue.hasDefiniteSize),
      ("iRange", iRange.hasDefiniteSize),
      ("iSeq", iSeq.hasDefiniteSize),
      ("iSet", iSet.hasDefiniteSize),
      ("iSortedMap", iSortedMap.hasDefiniteSize),
      ("iSortedSet", iSortedSet.hasDefiniteSize),
      ("iStream", iStream.hasDefiniteSize),
      ("iTreeMap", iTreeMap.hasDefiniteSize),
      ("iTreeSet", iTreeSet.hasDefiniteSize),
      ("iVector", iVector.hasDefiniteSize),
      ("iVectorIterator", iVectorIterator.hasDefiniteSize),
      // ("iWrappedString",           iWrappedString.hasDefiniteSize),
      ("mAnyRefMap", mAnyRefMap.hasDefiniteSize),
      ("mArrayBuffer", mArrayBuffer.hasDefiniteSize),
      ("mArrayBufferView", mArrayBufferView.hasDefiniteSize),
      // ("mArrayDeque",              mArrayDeque.hasDefiniteSize),
      // ("mArraySeq",                mArraySeq.hasDefiniteSize),
      ("mBitSet", mBitSet.hasDefiniteSize),
      ("mBuffer", mBuffer.hasDefiniteSize),
      ("mHashMap", mHashMap.hasDefiniteSize),
      ("mHashSet", mHashSet.hasDefiniteSize),
      ("mIndexedSeq", mIndexedSeq.hasDefiniteSize),
      ("mIterable", mIterable.hasDefiniteSize),
      ("mLinkedHashMap", mLinkedHashMap.hasDefiniteSize),
      ("mLinkedHashSet", mLinkedHashSet.hasDefiniteSize),
      ("mListBuffer", mListBuffer.hasDefiniteSize),
      ("mListMap", mListMap.hasDefiniteSize),
      ("mLongMap", mLongMap.hasDefiniteSize),
      ("mMap", mMap.hasDefiniteSize),
      ("mMultiMap", mMultiMap.hasDefiniteSize),
      ("mOpenHashMap", mOpenHashMap.hasDefiniteSize),
      ("mPriorityQueue", mPriorityQueue.hasDefiniteSize),
      ("mQueue", mQueue.hasDefiniteSize),
      ("mSeq", mSeq.hasDefiniteSize),
      ("mSet", mSet.hasDefiniteSize),
      ("mSortedMap", mSortedMap.hasDefiniteSize),
      ("mSortedSet", mSortedSet.hasDefiniteSize),
      ("mStack", mStack.hasDefiniteSize),
      ("mStringBuilder", mStringBuilder.hasDefiniteSize),
      ("mTreeMap", mTreeMap.hasDefiniteSize),
      ("mTreeSet", mTreeSet.hasDefiniteSize),
      ("mUnrolledBuffer", mUnrolledBuffer.hasDefiniteSize),
      ("mWeakHashMap", mWeakHashMap.hasDefiniteSize),
      ("bufferedSource", bufferedSource.hasDefiniteSize),
      ("source", source.hasDefiniteSize),
      ("tuple2Zipped", tuple2Zipped.hasDefiniteSize),
      ("tuple3Zipped", tuple3Zipped.hasDefiniteSize),
      // ("zippedIterable2",          zippedIterable2.hasDefiniteSize),
      // ("zippedIterable3",          zippedIterable3.hasDefiniteSize),
      // ("systemProperties",         systemProperties.hasDefiniteSizee)
    )

    in.foreach {
      case (name, size) =>
        // val d = if (size == -1) 0 else 1
        println(name.padTo(30, " ").mkString + size)
    }
  }
}
