package scala.collection.compat

import scala.collection.mutable

trait AbstractBuilder[-Elem, +To] extends mutable.Builder[Elem, To] {
  def addOne(elem: Elem): this.type

  final def +=(elem: Elem): this.type = addOne(elem)
}
