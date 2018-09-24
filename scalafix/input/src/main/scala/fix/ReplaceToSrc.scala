/*
rule = "Collection213CrossCompat"
 */
package fix

object ReplaceToSrc {
  List(1).to[Set]
  Set(1).to[List]

  def m1(xs: Set[Int]): List[Int] =
    xs.to

  List[Int]() // unrelated matching brackets

  def m2(xs: List[Int]): Iterable[Int] =
    xs.to

  def f(xs: List[Int]): Unit = ()
  f(Set(1).to)
}
