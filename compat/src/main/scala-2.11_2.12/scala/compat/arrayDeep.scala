package scala.compat

object arrayDeep {
  implicit class ArrayDeep(val a: Array[_]) extends AnyVal {
    //TODO: Remove hacky code and factor out to scala-collection-compat
    def deep: collection.IndexedSeq[Any]  = a.deep
  }
}
