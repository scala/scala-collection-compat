


package fix.conversions

import scala.collection.{immutable => i, mutable => m, concurrent => conc}
import java.{ lang => jl, util => ju }, java.util.{ concurrent => juc }
import scala.collection.JavaConverters._

object JavaConversionsExplicitCallSrc extends JavaConversionsDataSrc {

  // ### Scala => Java ###

  // asJavaIterator
  Iterator(1).asJava: ju.Iterator[Int]

  // asJavaEnumeration
  Iterator(1).asJavaEnumeration: ju.Enumeration[Int]

  // asJavaIterable
  iterable.asJava: jl.Iterable[Int]

  // asJavaCollection
  iterable.asJavaCollection: ju.Collection[Int]

  // bufferAsJavaList
  m.Buffer(1).asJava: ju.List[Int]

  // mutableSeqAsJavaList
  m.Seq(1).asJava: ju.List[Int]

  // seqAsJavaList
  Seq(1).asJava: ju.List[Int]

  // mutableSetAsJavaSet
  m.Set(1).asJava: ju.Set[Int]

  // setAsJavaSet
  Set(1).asJava: ju.Set[Int]

  // mutableMapAsJavaMap
  m.Map(1 -> 1).asJava: ju.Map[Int, Int]

  // asJavaDictionary
  m.Map(1 -> 1).asJavaDictionary: ju.Dictionary[Int, Int]

  // mapAsJavaMap
  m.Map(1 -> 1).asJava: ju.Map[Int, Int]

  // mapAsJavaConcurrentMap
  concMap.asJava: juc.ConcurrentMap[Int, Int]


  // ### Java => Scala ###

  // asScalaIterator
  juIterator.asScala: Iterator[Int]

  // enumerationAsScalaIterator
  juEnumeration.asScala: Iterator[Int]

  // iterableAsScalaIterable
  jlIterable.asScala: Iterable[Int]

  // collectionAsScalaIterable
  juCollection.asScala: Iterable[Int]

  // asScalaBuffer
  juList.asScala: m.Buffer[Int]

  // asScalaSet
  juSet.asScala: m.Set[Int]

  // mapAsScalaMap
  juMap.asScala: m.Map[Int, Int]

  // mapAsScalaConcurrentMap
  jucConcurrentMap.asScala: conc.Map[Int, Int]

  // dictionaryAsScalaMap
  juDictionary.asScala: m.Map[Int, Int]

  // propertiesAsScalaMap
  juProperties.asScala: m.Map[String, String]
}
