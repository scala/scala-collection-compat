/*
rule = "scala:fix.Scalacollectioncompat_NewCollections"
 */
package fix

object TraversableSrc {
  def foo(xs: Traversable[(Int, String)], ys: List[Int]): Unit = {
    xs.to[List]
    xs.to[Set]
    xs.toIterator
    ys.iterator
  }
}
