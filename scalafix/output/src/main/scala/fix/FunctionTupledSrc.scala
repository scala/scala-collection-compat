


package fix

import Function.tupled

object FunctionTupledSrc {
  def m2(i: Int, j: Int): Int = i + j
  def m3(i: Int, j: Int): Int = i + j
  def m4(i: Int, j: Int): Int = i + j
  def m5(i: Int, j: Int): Int = i + j

  val f2 = m2 _
  val f3 = m3 _
  val f4 = m4 _
  val f5 = m5 _

  (m2 _).tupled
  f2.tupled
  f2.tupled

  f2.tupled
  f3.tupled
  f4.tupled
  f5.tupled
}