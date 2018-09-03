


package fix

object RoughlySrc213 {
  val s = LazyList(1, 2, 3)
  s.lazyAppendedAll(List(4, 5, 6))
  1 #:: 2 #:: 3 #:: LazyList.empty
  val isEmpty: LazyList[_] => Boolean = {
    case x #:: xs => false
    case _        => true
  }
}
