/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

object TraversableSrc {
  def foo(xs: Traversable[(Int, String)], ys: List[Int], t: TraversableOnce[Int], b: BufferedIterator[Int]): Unit = {
    xs.to[List]
    xs.to[Set]
    xs.toIterator
    ys.iterator
  }
}
