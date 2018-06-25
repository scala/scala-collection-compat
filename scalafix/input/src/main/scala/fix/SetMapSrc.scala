/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

import scala.collection
import scala.collection.immutable
import scala.collection.mutable.{Map, Set} // Challenge to make sure the scoping is correct

class SetMapSrc(iset: immutable.Set[Int], 
                cset: collection.Set[Int],
                imap: immutable.Map[Int, Int],
                cmap: collection.Map[Int, Int]) {
  iset + (2, 3)
  imap + (2 -> 3, 3 -> 4)
  (iset + (2, 3)).toString
  iset + (2, 3) - 4
  imap.mapValues(_ + 1)
  iset + 1
  iset - 2
  cset + 1
  cset - 2
  cmap + (2 -> 3)
  cmap + ((4, 5))
  imap + (2 -> 3)
  imap + ((4, 5))
  imap.zip(List())
  List().zip(List())
}