/*
rule = "Collection213CrossCompat"
 */
package fix.conversions

import scala.collection.{immutable => i, mutable => m, concurrent => conc}
import java.{ lang => jl, util => ju }, java.util.{ concurrent => juc }
import scala.collection.JavaConversions._

object JavaConversionsExplicitCallSrc extends JavaConversionsDataSrc {

  // ### Scala => Java ###

  // asJavaIterator
  asJavaIterator(Iterator(1)): ju.Iterator[Int]

  // asJavaEnumeration
  asJavaEnumeration(Iterator(1)): ju.Enumeration[Int]

  // asJavaIterable
  asJavaIterable(iterable): jl.Iterable[Int]

  // asJavaCollection
  asJavaCollection(iterable): ju.Collection[Int]

  // bufferAsJavaList
  bufferAsJavaList(m.Buffer(1)): ju.List[Int]

  // mutableSeqAsJavaList
  mutableSeqAsJavaList(m.Seq(1)): ju.List[Int]

  // seqAsJavaList
  seqAsJavaList(Seq(1)): ju.List[Int]

  // mutableSetAsJavaSet
  mutableSetAsJavaSet(m.Set(1)): ju.Set[Int]

  // setAsJavaSet
  setAsJavaSet(Set(1)): ju.Set[Int]

  // mutableMapAsJavaMap
  mutableMapAsJavaMap(m.Map(1 -> 1)): ju.Map[Int, Int]

  // asJavaDictionary
  asJavaDictionary(m.Map(1 -> 1)): ju.Dictionary[Int, Int]

  // mapAsJavaMap
  mapAsJavaMap(m.Map(1 -> 1)): ju.Map[Int, Int]

  // mapAsJavaConcurrentMap
  mapAsJavaConcurrentMap(concMap): juc.ConcurrentMap[Int, Int]


  // ### Java => Scala ###

  // asScalaIterator
  asScalaIterator(juIterator): Iterator[Int]

  // enumerationAsScalaIterator
  enumerationAsScalaIterator(juEnumeration): Iterator[Int]

  // iterableAsScalaIterable
  iterableAsScalaIterable(jlIterable): Iterable[Int]

  // collectionAsScalaIterable
  collectionAsScalaIterable(juCollection): Iterable[Int]

  // asScalaBuffer
  asScalaBuffer(juList): m.Buffer[Int]

  // asScalaSet
  asScalaSet(juSet): m.Set[Int]

  // mapAsScalaMap
  mapAsScalaMap(juMap): m.Map[Int, Int]

  // mapAsScalaConcurrentMap
  mapAsScalaConcurrentMap(jucConcurrentMap): conc.Map[Int, Int]

  // dictionaryAsScalaMap
  dictionaryAsScalaMap(juDictionary): m.Map[Int, Int]

  // propertiesAsScalaMap
  propertiesAsScalaMap(juProperties): m.Map[String, String]
}
