/*
rule = "scala:fix.CrossCompat"
 */
package fix

object TraversableBug {
  Array(1).to[List]
}
