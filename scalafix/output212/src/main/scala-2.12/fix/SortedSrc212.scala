


package fix

import scala.collection.{mutable => m}
import scala.collection.compat._

object SortedSrc212 {
  m.SortedMap(1 -> 1).rangeFrom(0)
  m.TreeMap(1 -> 1).rangeFrom(0)
  m.SortedMap(1 -> 1).rangeTo(0)
  m.TreeMap(1 -> 1).rangeTo(0)
  m.SortedMap(1 -> 1).rangeUntil(0)
  m.TreeMap(1 -> 1).rangeUntil(0)
}
