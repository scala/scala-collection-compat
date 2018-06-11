/*
rule = "scala:fix.Scalacollectioncompat_NewCollections"
 */
package fix

import scala.collection.mutable.Map

class MethodRenames(xs: Map[Int, Int]) {
  xs.retain((_, _) => true)
  xs.retain{case (x, y) => true}
}