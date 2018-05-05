package fix

object StreamSrc {
  val s = LazyList(1, 2, 3)
  s.lazyAppendedAll(List(4, 5, 6))
  1 #:: 2 #:: 3 #:: LazyList.Empty
  val isEmpty: LazyList[_] => Boolean = {
    case LazyList.Empty => true
    case x #:: xs     => false
  }
}
