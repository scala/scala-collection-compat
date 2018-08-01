/*
rule = "scala:fix.CrossCompat"
 */
package fix.conversions

import scala.collection.{immutable => i, mutable => m, concurrent => conc}
import java.{ lang => jl, util => ju }, java.util.{ concurrent => juc }
import scala.collection.JavaConversions

object JavaConversionsSpecificImportSrc extends JavaConversionsDataSrc {

  // ### Scala => Java ###

  // asJavaIterator
  {
    import JavaConversions.asJavaIterator
    Iterator(1): ju.Iterator[Int]
  }

  // asJavaEnumeration
  {
    import JavaConversions.asJavaEnumeration
    Iterator(1): ju.Enumeration[Int]
  }

  // asJavaIterable
  {
    import JavaConversions.asJavaIterable
    iterable: jl.Iterable[Int]
  }

  // asJavaCollection
  {
    import JavaConversions.asJavaCollection
    iterable: ju.Collection[Int]
  }

  // bufferAsJavaList
  {
    import JavaConversions.bufferAsJavaList
    m.Buffer(1): ju.List[Int]
  }

  // mutableSeqAsJavaList
  {
    import JavaConversions.mutableSeqAsJavaList
    m.Seq(1): ju.List[Int]
  }

  // seqAsJavaList
  {
    import JavaConversions.seqAsJavaList
    Seq(1): ju.List[Int]
  }

  // mutableSetAsJavaSet
  {
    import JavaConversions.mutableSetAsJavaSet
    m.Set(1): ju.Set[Int]
  }

  // setAsJavaSet
  {
    import JavaConversions.setAsJavaSet
    Set(1): ju.Set[Int]
  }

  // mutableMapAsJavaMap
  {
    import JavaConversions.mutableMapAsJavaMap
    m.Map(1 -> 1): ju.Map[Int, Int]
  }

  // asJavaDictionary
  {
    import JavaConversions.asJavaDictionary
    m.Map(1 -> 1): ju.Dictionary[Int, Int]
  }

  // mapAsJavaMap
  {
    import JavaConversions.mapAsJavaMap
    Map(1 -> 1): ju.Map[Int, Int]
  }

  // mapAsJavaConcurrentMap
  {
    import JavaConversions.mapAsJavaConcurrentMap
    concMap: juc.ConcurrentMap[Int, Int]
  }


  // ### Java => Scala ###

  // asScalaIterator
  {
    import JavaConversions.asScalaIterator
    juIterator: Iterator[Int]
  }

  // enumerationAsScalaIterator
  {
    import JavaConversions.enumerationAsScalaIterator
    juEnumeration: Iterator[Int]
  }

  // iterableAsScalaIterable
  {
    import JavaConversions.iterableAsScalaIterable
    jlIterable: Iterable[Int]
  }

  // collectionAsScalaIterable
  {
    import JavaConversions.collectionAsScalaIterable
    juCollection: Iterable[Int]
  }

  // asScalaBuffer
  {
    import JavaConversions.asScalaBuffer
    juList: m.Buffer[Int]
  }

  // asScalaSet
  {
    import JavaConversions.asScalaSet
    juSet: m.Set[Int]
  }

  // mapAsScalaMap
  {
    import JavaConversions.mapAsScalaMap
    juMap: m.Map[Int, Int]
  }

  // mapAsScalaConcurrentMap
  {
    import JavaConversions.mapAsScalaConcurrentMap
    jucConcurrentMap: conc.Map[Int, Int]
  }

  // dictionaryAsScalaMap
  {
    import JavaConversions.dictionaryAsScalaMap
    juDictionary: m.Map[Int, Int]
  }

  // propertiesAsScalaMap
  {
    import JavaConversions.propertiesAsScalaMap
    juProperties: m.Map[String, String]
  }
}
