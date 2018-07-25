


package fix

import scala.collection.compat._
class IterableSrc(it: Iterable[Int]) {
  it.iterator.sameElements(it)
}
