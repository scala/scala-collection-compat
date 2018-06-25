/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

class IterableSrc(it: Iterable[Int]) {
  it.sameElements(it)
}