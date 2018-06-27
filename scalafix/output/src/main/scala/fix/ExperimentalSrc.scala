


package fix

import scala.collection
import scala.collection.immutable
import scala.collection.mutable.{Map, Set} // Challenge to make sure the scoping is correct

class ExperimentalSrc(iset: immutable.Set[Int], 
                cset: collection.Set[Int],
                imap: immutable.Map[Int, Int],
                cmap: collection.Map[Int, Int]) {
  iset + 1
  iset - 2
  cset ++ _root_.scala.collection.Set(1)
  cset -- _root_.scala.collection.Set(2)
  
  cmap ++ _root_.scala.collection.Map(2 -> 3)
  cmap ++ _root_.scala.collection.Map((4, 5))
  imap + (2 -> 3)
  imap + ((4, 5))

  // Map.zip
  imap.zip(List()).toMap
  List().zip(List())
}