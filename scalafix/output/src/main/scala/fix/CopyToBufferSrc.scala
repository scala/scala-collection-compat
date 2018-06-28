


package fix

import scala.collection.mutable

class CopyToBufferSrc(xs: List[Int], b: mutable.Buffer[Int]) {

  b ++= xs
  b ++= xs ++ xs

}
