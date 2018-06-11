/*
rule = "scala:fix.Scalacollectioncompat_NewCollections"
 */
package fix

import scala.collection.mutable.Map

object MethodRenames {
  Map(1 -> 1).retain{ case (x, y) => true }
}