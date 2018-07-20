


package fix

import scala.collection.compat._
object TraversableSrc {
  def foo(xs: Iterable[(Int, String)], ys: List[Int]): Unit = {
    xs.to(List)
    xs.to(Set)
    xs.iterator
    ys.iterator
  }

  def m1(xs: IterableOnce[Int]): List[Int] =
    xs.to(scala.collection.immutable.List)

  List[Int]() // unrelated matching brackets
}
