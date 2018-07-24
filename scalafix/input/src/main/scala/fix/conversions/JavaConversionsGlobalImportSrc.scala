/*
rule = "scala:fix.CrossCompat"
 */
package fix.conversions

import scala.collection.{immutable => i, mutable => m, concurrent => conc}
import java.{ lang => jl, util => ju }, java.util.{ concurrent => juc }
import scala.collection.JavaConversions._

object JavaConversionsGlobalImportSrc extends JavaConversionsDataSrc {

  // ### Scala => Java ###

  // asJavaIterator
  Iterator(1): ju.Iterator[Int]

  // asJavaEnumeration
  Iterator(1): ju.Enumeration[Int]

  // ambigous: asJavaIterable
  // iterable: jl.Iterable[Int]

  // asJavaCollection
  iterable: ju.Collection[Int]

  // bufferAsJavaList
  m.Buffer(1): ju.List[Int]

  // mutableSeqAsJavaList
  m.Seq(1): ju.List[Int]

  // seqAsJavaList
  Seq(1): ju.List[Int]

  // mutableSetAsJavaSet
  m.Set(1): ju.Set[Int]

  // setAsJavaSet
  Set(1): ju.Set[Int]

  // mutableMapAsJavaMap
  m.Map(1 -> 1): ju.Map[Int, Int]

  // asJavaDictionary
  m.Map(1 -> 1): ju.Dictionary[Int, Int]

  // mapAsJavaMap
  m.Map(1 -> 1): ju.Map[Int, Int]

  // mapAsJavaConcurrentMap
  concMap: juc.ConcurrentMap[Int, Int]


  // ### Java => Scala ###

  // asScalaIterator
  juIterator: Iterator[Int]

  // enumerationAsScalaIterator
  juEnumeration: Iterator[Int]

  // iterableAsScalaIterable
  jlIterable: Iterable[Int]

  // collectionAsScalaIterable
  juCollection: Iterable[Int]

  // asScalaBuffer
  juList: m.Buffer[Int]

  // asScalaSet
  juSet: m.Set[Int]

  // mapAsScalaMap
  juMap: m.Map[Int, Int]

  // mapAsScalaConcurrentMap
  jucConcurrentMap: conc.Map[Int, Int]

  // dictionaryAsScalaMap
  juDictionary: m.Map[Int, Int]

  // propertiesAsScalaMap
  juProperties: m.Map[String, String]
}
