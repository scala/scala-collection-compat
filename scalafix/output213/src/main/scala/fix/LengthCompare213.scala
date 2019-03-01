package fix

import scala.collection.Seq

class LengthCompare213(xs: Seq[Int]) {
  xs.sizeCompare(2) < 0
  xs sizeCompare 4 match {
    case 0          => "same"
    case i if i < 0 => "less"
    case i if i > 0 => "more"
  }
}
