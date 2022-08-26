package test.scala.jdk.javaapi

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Buffer

/**
 * Scala collection objects defined for easy access in a Java class
 */
object TestObjects {

  val seq: scala.collection.Seq[String]                = ArrayBuffer("A", "B")
  val mutableSeq: scala.collection.mutable.Seq[String] = ArrayBuffer("A", "B")
  val set: scala.collection.Set[String]                = Set("A", "B")
  val map: scala.collection.Map[String, String]        = Map("A" -> "B")

  val iterable: scala.collection.Iterable[String]                    = Iterable("A", "B")
  val iterator: scala.collection.Iterator[String]                    = Iterator("A", "B")
  val buffer: scala.collection.mutable.Buffer[String]                = mutable.Buffer("A", "B")
  val mutableSet: scala.collection.mutable.Set[String]               = mutable.Set("A", "B")
  val mutableMap: scala.collection.mutable.Map[String, String]       = mutable.Map("A" -> "B")
  val concurrentMap: scala.collection.concurrent.Map[String, String] = TrieMap("A" -> "B")
}
