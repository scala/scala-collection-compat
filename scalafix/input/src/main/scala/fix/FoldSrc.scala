/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

class FoldSrc(xs: List[Int]){
  val f: (Int, Int) => Int = (x, y) => x + y
  val g: (Int, Int) => Int = (x, y) => x * y

  (0 /: xs)(f)
  (xs :\ 1)(g)
}
