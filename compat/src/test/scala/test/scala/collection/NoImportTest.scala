package test.scala.collection

// Don't import scala.collection.compat._
import scala.collection.compat.Factory
import scala.collection.{mutable, immutable}

class NoImportTest {

  implicitly[Factory[Int, List[Int]]]
  implicitly[Factory[Char, String]]
  implicitly[Factory[Char, Array[Char]]]
  implicitly[Factory[Int, collection.BitSet]]
  implicitly[Factory[Int, mutable.BitSet]]
  implicitly[Factory[Int, immutable.BitSet]]

}
