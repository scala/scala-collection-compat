


package fix

object ShiftingSrc {
  val b = 1.toByte
  val c = 'c'
  val i = 1
  val s = 1.toShort
  val l = 1L

  b.toLong << l
  b.toLong >>> l
  b.toLong >> l

  c.toLong << l
  c.toLong >>> l
  c.toLong >> l

  i.toLong << l
  i.toLong >>> l
  i.toLong >> l

  s.toLong << l
  s.toLong >>> l
  s.toLong >> l

  l << l
  l >>> l
  l >> l
}
