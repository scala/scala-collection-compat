/*
rule = "scala:fix.CrossCompat"
 */
package fix

class IterableSrc(it: Iterable[Int]) {
  it.sameElements(it)
}
