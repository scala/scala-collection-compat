package test.scala.collection

import org.junit.Test
import org.junit.Assert._
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.collection.compat._

@RunWith(classOf[JUnit4])
class StringParsersJVMTest extends StringParsersTest {

  @Test
  def doubleSpecificTest(): Unit = doubleExamples.foreach(doubleOK)

  @Test
  def floatSpecificTest(): Unit = doubleExamples.foreach(floatOK)

  @Test
  def nullByte(): Unit = assertThrows[NullPointerException](nullstring.toByteOption)

  @Test
  def nullShort(): Unit = assertThrows[NullPointerException](nullstring.toShortOption)

  @Test
  def nullInt(): Unit = assertThrows[NullPointerException](nullstring.toIntOption)

  @Test
  def nullLong(): Unit = assertThrows[NullPointerException](nullstring.toLongOption)

  @Test
  def nullFloat(): Unit = assertThrows[NullPointerException](nullstring.toFloatOption)

  @Test
  def nullDouble(): Unit = assertThrows[NullPointerException](nullstring.toDoubleOption)

}
