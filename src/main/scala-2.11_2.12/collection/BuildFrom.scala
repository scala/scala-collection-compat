package scala.collection

import scala.collection.generic.CanBuildFrom

/** Builds a collection of type `C` from elements of type `A` when a source collection of type `From` is available.
  * Implicit instances of `BuildFrom` are available for all collection types.
  *
  * @tparam From Type of source collection
  * @tparam A Type of elements (e.g. `Int`, `Boolean`, etc.)
  * @tparam C Type of collection (e.g. `List[Int]`, `TreeMap[Int, String]`, etc.)
  */
trait BuildFrom[-From, -A, +C] extends Any {

  def fromSpecificIterable(from: From)(it: Iterable[A]): C

  /** Get a Builder for the collection. For non-strict collection types this will use an intermediate buffer.
    * Building collections with `fromSpecificIterable` is preferred because it can be lazy for lazy collections. */
  def newBuilder(from: From): mutable.Builder[A, C]

  @deprecated("Use newBuilder() instead of apply()", "2.13.0")
  @`inline` def apply(from: From): mutable.Builder[A, C] = newBuilder(from)

}

object BuildFrom {

  // Implicit instance derived from an implicit CanBuildFrom instance
  implicit def fromCanBuildFrom[From, A, C](implicit cbf: CanBuildFrom[From, A, C]): BuildFrom[From, A, C] =
    new BuildFrom[From, A, C] {
      def fromSpecificIterable(from: From)(it: Iterable[A]): C = (cbf(from) ++= it).result()
      def newBuilder(from: From): mutable.Builder[A, C] = cbf(from)
    }

  // Implicit conversion derived from an implicit conversion to CanBuildFrom
  implicit def fromCanBuildFromConversion[X, From, A, C](x: X)(implicit toCanBuildFrom: X => CanBuildFrom[From, A, C]): BuildFrom[From, A, C] =
    fromCanBuildFrom(toCanBuildFrom(x))

}
