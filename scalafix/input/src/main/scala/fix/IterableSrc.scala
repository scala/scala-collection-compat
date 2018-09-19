/*
rule = "Collection213CrossCompat"
 */
package fix

class IterableSrc(it: Iterable[Int]) {
  it.sameElements(it)
}
