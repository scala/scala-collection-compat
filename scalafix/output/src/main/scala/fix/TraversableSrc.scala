package fix

import scala.collection.BufferedIterator
object TraversableSrc {
  def foo(xs: Iterable[(Int, String)], ys: List[Int], t: IterableOnce[Int], b: BufferedIterator[Int]): Unit = {
    xs.to(List)
    xs.to(Set)
    xs.iterator
    ys.iterator
  }
}
