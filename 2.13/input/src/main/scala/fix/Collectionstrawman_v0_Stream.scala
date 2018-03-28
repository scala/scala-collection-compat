/*
rule = "scala:fix.Collectionstrawman_v0"
 */
package fix

object Collectionstrawman_v0_Stream {
  val s = Stream(1, 2, 3)
  s.append(List(4, 5, 6))
  1 #:: 2 #:: 3 #:: Stream.Empty
  val isEmpty: Stream[_] => Boolean = {
    case Stream.Empty => true
    case x #:: xs     => false
  }
}
