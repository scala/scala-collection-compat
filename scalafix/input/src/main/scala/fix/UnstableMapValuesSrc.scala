/*
rule = "scala:fix.UnstableMapValues"
 */
package fix

class UnstableMapValuesSrc(map: Map[Int, Int]) {
  map.mapValues(_ + 1)
}
