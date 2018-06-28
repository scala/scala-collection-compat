/*
rule = "scala:fix.Stable"
 */
package fix

class IterableSrc(it: Iterable[Int]) {
  it.sameElements(it)
}