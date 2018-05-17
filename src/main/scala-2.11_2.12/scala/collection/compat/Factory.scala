package scala.collection.compat

import scala.collection.generic.CanBuildFrom
import scala.collection.{TraversableOnce, mutable}

/**
  * A factory that builds a collection of type `C` with elements of type `A`.
  *
  * @tparam A Type of elements (e.g. `Int`, `Boolean`, etc.)
  * @tparam C Type of collection (e.g. `List[Int]`, `TreeMap[Int, String]`, etc.)
  */
trait Factory[-A, +C] extends Any {

  /**
    * @return A collection of type `C` containing the same elements
    *         as the source collection `it`.
    * @param it Source collection
    */
  def fromSpecific(it: TraversableOnce[A]): C

  /** Get a Builder for the collection. For non-strict collection types this will use an intermediate buffer.
    * Building collections with `fromSpecific` is preferred because it can be lazy for lazy collections. */
  def newBuilder(): mutable.Builder[A, C]
}

object Factory {

  implicit def fromCanBuildFrom[A, C](implicit cbf: CanBuildFrom[Nothing, A, C]): Factory[A, C] =
    new Factory[A, C] {
      def fromSpecific(it: TraversableOnce[A]): C = (cbf() ++= it).result()
      def newBuilder(): mutable.Builder[A, C] = cbf()
    }

  implicit def fromCanBuildFromConversion[X, A, C](x: X)(implicit toCanBuildFrom: X => CanBuildFrom[Nothing, A, C]): Factory[A, C] =
    fromCanBuildFrom(toCanBuildFrom(x))

}
