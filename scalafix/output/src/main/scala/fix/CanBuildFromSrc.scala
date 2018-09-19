


package fix

import scala.language.higherKinds

import collection.immutable
import scala.collection.compat._

object CanBuildFromSrc {

  def f[C0, A, C1[_]](c0: C0)(implicit
      cbf: Factory[Int, C1[Int]],
      cbf2: Factory[String, C1[String]],
      cbf3: BuildFrom[C0, A, C1[A]]): C1[Int] = {

    val b = cbf.newBuilder
    val b2 = cbf.newBuilder
    val b3 = cbf.newBuilder
    val b4 = cbf2.newBuilder
    val b5 = cbf3.newBuilder(c0)
    val b6 = cbf3.newBuilder(c0)

    cbf.fromSpecific(List.empty[Int])
    cbf2.fromSpecific(List.empty[String])
    b.result()
  }

  def kind(implicit cbf: BuildFrom[String, Char, String],
                    cbf2: BuildFrom[String, (Int, Boolean), Map[Int, Boolean]]): Unit = {

    cbf.newBuilder("")
    cbf2.newBuilder("")
    ()
  }

  def f2[T, That](implicit cbf: Factory[T, That]): Foo[T, That] =
    new Foo

  def f3[T, That](implicit cbf: Factory[T, That with immutable.Iterable[_]]): Foo[T, That] =
    new Foo

  class Foo[T, That](implicit cbf: Factory[T, That]) {
    val b = cbf.newBuilder
  }
}
