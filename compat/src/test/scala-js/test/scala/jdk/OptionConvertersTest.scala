package test.scala.jdk

import org.junit.Assert.assertEquals
import org.junit.Test

import java.util.Optional
import scala.jdk.OptionConverters._

/**
 * The tests were copied from the Scala 2.13 Standard Library. `scala.jdk.javaapi` stuff has been omitted and
 * everything concerning `OptionalInt`, `OptionalDouble` and `OptionalLong` is only available in the jvm tests.
 *
 * See https://github.com/scala/scala/blob/2.13.x/test/junit/scala/jdk/OptionConvertersTest.scala.
 */
class OptionConvertersTest {
  @Test
  def scalaToEverything(): Unit = {
    val o  = Option("fish")
    val n  = None: Option[String]
    val od = Option(2.7)
    val nd = None: Option[Double]
    val oi = Option(4)
    val ni = None: Option[Int]
    val ol = Option(-1L)
    val nl = None: Option[Long]
    assertEquals(o.toJava, Optional.of(o.get))
    assertEquals(n.toJava, Optional.empty[String])
    assertEquals(od.toJava.get: Double, Optional.of(od.get).get: Double, 0)
    assertEquals(nd.toJava, Optional.empty[Double])
    assertEquals(oi.toJava.get: Int, Optional.of(oi.get).get: Int)
    assertEquals(ni.toJava, Optional.empty[Int])
    assertEquals(ol.toJava.get: Long, Optional.of(ol.get).get: Long)
    assertEquals(nl.toJava, Optional.empty[Long])
  }

  @Test
  def javaGenericToEverything(): Unit = {
    val o  = Optional.of("fish")
    val n  = Optional.empty[String]
    val od = Optional.of(2.7)
    val nd = Optional.empty[Double]
    val oi = Optional.of(4)
    val ni = Optional.empty[Int]
    val ol = Optional.of(-1L)
    val nl = Optional.empty[Long]
    assertEquals(o.toScala, Option(o.get))
    assertEquals(n.toScala, Option.empty[String])
    assertEquals(od.toScala, Option(od.get))
    assertEquals(nd.toScala, Option.empty[Double])
    assertEquals(oi.toScala, Option(oi.get))
    assertEquals(ni.toScala, Option.empty[Int])
    assertEquals(ol.toScala, Option(ol.get))
    assertEquals(nl.toScala, Option.empty[Long])
  }
}
