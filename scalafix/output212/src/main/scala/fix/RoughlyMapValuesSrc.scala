


package fix

class RoughlyMapValuesSrc(map: Map[Int, Int]) {
  map.mapValues(_ + 1).toMap
}
