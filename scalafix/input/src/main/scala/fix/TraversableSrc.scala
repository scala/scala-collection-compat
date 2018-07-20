/*
rule = "scala:fix.CrossCompat"
 */
package fix

object TraversableSrc {
  def foo(xs: Traversable[(Int, String)], ys: List[Int]): Unit = {
    xs.to[List]
    xs.to[Set]
    xs.toIterator
    ys.iterator
  }

  def m1(xs: TraversableOnce[Int]): List[Int] =
    xs.to

  List[Int]() // unrelated matching brackets
}
