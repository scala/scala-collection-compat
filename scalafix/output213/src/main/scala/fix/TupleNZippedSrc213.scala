


package fix

import scala.language.postfixOps
object Tuple2ZippedSrc213 {
  def zipped(xs: List[Int], ys: List[Int]): Unit = {
    xs.lazyZip(ys)
    xs.lazyZip(ys)
    (xs.lazyZip(ys) )
    ((xs).lazyZip((ys)))
    xs.lazyZip(// foo
      ys)
    /* a *//* b */ xs /* c */.lazyZip(/* d */ ys /* e */)/* f *//* g *//* h */
    coll(1).lazyZip(coll(2))
    List(1, 2, 3).lazyZip(Array(1))
  }
  def coll(x: Int): List[Int] = ???
}

object Tuple3ZippedSrc213 {
  def zipped(xs: List[Int], ys: List[Int], zs: List[Int]): Unit = {
    xs.lazyZip(ys).lazyZip(zs)
    xs.lazyZip(ys).lazyZip(zs)
    (xs.lazyZip(ys).lazyZip(zs) )
    ((xs).lazyZip((ys)).lazyZip((zs)))
    xs.lazyZip(// foo
      ys).lazyZip(// bar
      zs)
    /* a *//* b */ xs /* c */.lazyZip(/* d */ ys /* e */).lazyZip(/* f */ zs /* g */)/* h *//* i *//* j */
    coll(1).lazyZip(coll(2)).lazyZip(coll(3))
    List(1, 2, 3).lazyZip(Set(1, 2, 3)).lazyZip(Array(1))
  }
  def coll(x: Int): List[Int] = ???
}
