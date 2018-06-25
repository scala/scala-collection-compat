


package fix

class IterableSrc(it: Iterable[Int]) {
  it.iterator.sameElements(it)
}