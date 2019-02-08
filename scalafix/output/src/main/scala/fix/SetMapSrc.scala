package fix

class SetMapSrc(set: Set[Int], map: Map[Int, Int]) {
  set + 1
  set + 2 + 3
  map + (2 -> 3) + (3 -> 4)
  (set + 2 + 3).toString
  set + 2 + 3 - 4
}
