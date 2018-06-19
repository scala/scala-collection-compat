/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

object StreamSrc {
  val s = Stream(1, 2, 3)
  s.append(List(4, 5, 6))
  1 #:: 2 #:: 3 #:: Stream.Empty
  val isEmpty: Stream[_] => Boolean = {
    case Stream.Empty => true
    case x #:: xs     => false
  }
}
