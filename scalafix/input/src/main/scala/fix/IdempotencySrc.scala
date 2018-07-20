/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.collection.compat._
object IdempotencySrc {
  List(1).to(Set)
}
