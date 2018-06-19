/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

object ValueTypePlusStringSrc {
  0.toByte + "a"
  0.toShort + "a"
  'a' + "a"
  1 + "a"
  0D + "a"
  0F + "a"
  0L + "a"
  1 + 1
}