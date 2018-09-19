/*
rule = "Collection213CrossCompat"
 */
package fix

import scala.collection.mutable

class CopyToBufferSrc(xs: List[Int], b: mutable.Buffer[Int]) {
  xs.copyToBuffer(b)
  (xs ++ xs).copyToBuffer(b)
}
