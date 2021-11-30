package scala.compat

object arryDeep {
  def prettyArray(a: Array[_]): collection.IndexedSeq[Any] =
    new collection.AbstractSeq[Any] with collection.IndexedSeq[Any] {
      def length: Int = a.length

      def apply(idx: Int): Any = a(idx) match {
        case x: AnyRef if x.getClass.isArray => prettyArray(x.asInstanceOf[Array[_]])
        case x => x
      }
      override def className = "Array"
    }

  implicit class ArrayDeep(val a: Array[_]) extends AnyVal {
    //TODO: Remove hacky code and factor out to scala-collection-compat
    def deep: collection.IndexedSeq[Any]  = prettyArray(a)
  }
}
