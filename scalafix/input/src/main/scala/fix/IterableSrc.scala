/*
rule = "scala:fix.NewCollections"
 */
package fix

class IterableSrc(it: Iterable[Int]) {
  it.sameElements(it)
}
