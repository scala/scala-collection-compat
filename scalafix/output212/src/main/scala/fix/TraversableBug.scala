


package fix

import scala.collection.compat._
object TraversableBug {
  Array(1).to(List)
}
