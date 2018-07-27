


package fix

class FoldSrc(xs: List[Int]){
  val f: (Int, Int) => Int = (x, y) => x + y
  val g: (Int, Int) => Int = (x, y) => x * y

  xs.foldLeft(0)(f)
  xs.foldRight(1)(g)
}