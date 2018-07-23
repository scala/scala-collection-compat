/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.collection.breakOut
import scala.collection.{immutable, mutable}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BreakoutSrc(ts: Traversable[Int], vec: Vector[Int], list: List[Int], seq: Seq[Int]) {

  // `IndexedSeqOptimized.zip`
  vec.zip(vec)(breakOut): Map[Int, Int]

  // `IterableLike.zip`
  seq.zip(seq)(breakOut): Map[Int, Int]

  // `IterableLike.zipAll`
  seq.zipAll(seq, 0, 0)(breakOut): Array[(Int, Int)]

  // `List ++`
  (list ++ list)(breakOut): Set[Int]

  // `List +:`
  (1 +: list)(breakOut): Set[Int]

  // `List.collect`
  list.collect{ case x => x}(breakOut): Set[Int]

  // `List.flatMap`
  list.flatMap(x => List(x))(breakOut): Set[Int]

  // `List.map`
  list.map(x => x)(breakOut): Set[Int]

  // `SeqLike.reverseMap`
  seq.reverseMap(_ + 1)(breakOut): Set[Int]

  // `SeqLike +:`
  (1 +: seq)(breakOut): List[Int]

  // `SeqLike :+`
  (seq :+ 1)(breakOut): List[Int]

  // `SeqLike.updated`
  (seq.updated(0, 0))(breakOut): List[Int]

  // `SeqLike.union`
  seq.union(seq)(breakOut): List[Int]


  //`SetLike.map`
  Set(1).map(x => x)(breakOut): List[Int]


  // `TraversableLike ++`
  (ts ++ ts )(breakOut): Set[Int]

  // `TraversableLike ++:`
  (ts ++: ts)(breakOut): Set[Int]

  // `TraversableLike.collect`
  ts.collect{ case x => x }(breakOut): Set[Int]

  // `TraversableLike.flatMap`
  ts.flatMap(x => List(x))(breakOut): collection.SortedSet[Int]

  // `TraversableLike.map`
  ts.map(_ + 1)(breakOut): Set[Int]

  // `TraversableLike.scanLeft`
  ts.scanLeft(0)((a, b) => a + b)(breakOut): Set[Int]


  // `Vector ++`
  (vec ++ List(1))(breakOut): List[Int]

  // `Vector +:`
  (1 +: vec)(breakOut): List[Int]

  // `Vector :+`
  (vec :+ 1)(breakOut): List[Int]

  // `Vector.updated`
  (vec.updated(0, 0))(breakOut): List[Int]

  // Future
  Future.sequence(List(Future(1)))(breakOut, global): Future[Seq[Int]]
  Future.traverse(List(1))(x => Future(x))(breakOut, global): Future[Seq[Int]]

  // Iterable
  List(1).map(x => x)(breakOut): Iterator[Int]

  // Specific collections
  List(1 -> "1").map(x => x)(breakOut): immutable.SortedMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): immutable.HashMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): immutable.ListMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): immutable.TreeMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): mutable.SortedMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): mutable.HashMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): mutable.ListMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): mutable.TreeMap[Int, String]
  List(1 -> "1").map(x => x)(breakOut): mutable.Map[Int, String]
  List(1 -> "1").map(x => x)(breakOut): immutable.IntMap[String]
  List(1L -> "1").map(x => x)(breakOut): immutable.LongMap[String]
  List(1L -> "1").map(x => x)(breakOut): mutable.LongMap[String]
}
