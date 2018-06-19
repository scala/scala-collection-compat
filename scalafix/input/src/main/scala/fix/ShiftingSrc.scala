/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

object ShiftingSrc {
  val b = 1.toByte
  val c = 'c'
  val i = 1
  val s = 1.toShort
  val l = 1L

  b << l
  b >>> l
  b >> l

  c << l
  c >>> l
  c >> l

  i << l
  i >>> l
  i >> l

  s << l
  s >>> l
  s >> l

  l << l
  l >>> l
  l >> l
}
