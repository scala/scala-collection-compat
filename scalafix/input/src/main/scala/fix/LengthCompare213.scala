/*
rule = "Collection213Upgrade"
 */
package fix

import scala.collection.Seq

class LengthCompare213(xs: Seq[Int]) {
  xs.lengthCompare(2) < 0
  xs lengthCompare 4 match {
    case 0          => "same"
    case i if i < 0 => "less"
    case i if i > 0 => "more"
  }
}
