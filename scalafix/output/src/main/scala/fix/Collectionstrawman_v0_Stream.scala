package fix

object Collectionstrawman_v0_Stream {
  val s = LazyList(1, 2, 3)
  s.lazyAppendAll(List(4, 5, 6))
  1 #:: 2 #:: 3 #:: LazyList.Empty
  val isEmpty: LazyList[_] => Boolean = {
    case LazyList.Empty => true
    case x #:: xs     => false
  }
}
