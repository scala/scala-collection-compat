/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.language.higherKinds

import collection.generic.CanBuildFrom

object CanBuildFromSrc {

  def f[C0, A, C1[_]](c0: C0)(implicit
      cbf: CanBuildFrom[Nothing, Int, C1[Int]],
      cbf2: CanBuildFrom[Nothing, String, C1[String]],
      cbf3: CanBuildFrom[C0, A, C1[A]]): C1[Int] = {

    val b = cbf()
    val b2 = cbf.apply
    val b3 = cbf.apply()
    val b4 = cbf2.apply()
    val b5 = cbf3(c0)
    val b6 = cbf3.apply(c0)

    List.empty[Int].to[C1]
    List.empty[String].to[C1]
    b.result()
  }

  def kind(implicit cbf: CanBuildFrom[String, Char, String],
                    cbf2: CanBuildFrom[String, (Int, Boolean), Map[Int, Boolean]]): Unit = {

    cbf("")
    cbf2("")
    ()
  }

  def f2[T, That](implicit cbf: CanBuildFrom[Nothing, T, That]): Foo[T, That] =
    new Foo

  class Foo[T, That](implicit cbf: CanBuildFrom[Nothing, T, That]) {
    val b = cbf()
  }
}
