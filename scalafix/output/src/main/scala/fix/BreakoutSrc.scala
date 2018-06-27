


package fix


object BreakoutSrc {
  val xs = List(1, 2, 3)

  xs.iterator.collect{ case x => x }.to(implicitly): Set[Int]
  xs.iterator.flatMap(x => List(x)).to(implicitly): Set[Int]
  xs.iterator.map(_ + 1).to(implicitly): Set[Int]
  xs.reverseIterator.map(_ + 1).to(implicitly): Set[Int]
  xs.iterator.scanLeft(0)((a, b) => a + b).to(implicitly): Set[Int]
  xs.iterator.concat(xs).to(implicitly): Set[Int]
  xs.view.updated(0, 1).to(implicitly): Set[Int]
  xs.iterator.zip(xs).to(implicitly): Array[(Int, Int)]
  xs.iterator.zipAll(xs, 0, 0).to(implicitly): Array[(Int, Int)]

  (xs.iterator ++ xs).to(implicitly): Set[Int]
  (1 +: xs.view).to(implicitly): Set[Int]
  (xs.view :+ 1).to(implicitly): Set[Int]
  (xs ++: xs.view).to(implicitly): Set[Int]
}