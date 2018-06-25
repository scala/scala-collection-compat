/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

import scala.collection.mutable.ArrayBuilder

trait TestsSrc {
  ArrayBuilder.make[Int](  )
}
