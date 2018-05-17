package scala.collection.compat

package object imm {
  type ArraySeq[+T] = scala.collection.immutable.ArraySeq[T]
  val ArraySeq      = scala.collection.immutable.ArraySeq
}
