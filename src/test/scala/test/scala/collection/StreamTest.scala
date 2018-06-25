package test.scala.collection

import utest._

import scala.collection.compat._

object StreamTest extends TestSuite{

  val tests = Tests{
    'lazyAppendedAll - {
      val s = 1 #:: 2 #:: 3 #:: Stream.Empty
      s.lazyAppendedAll(List(4, 5, 6))
    }
  }
}
