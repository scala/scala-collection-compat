


package fix

import scala.collection.compat._
object UnsortedSrc212Plus {
  import Data._
  import Data212Plus._

  mSortedMap.unsortedSpecific.map(unorderedMap)
  mSortedMap.map(orderedMap)

  mTreeMap.unsortedSpecific.map(unorderedMap)
  mTreeMap.map(orderedMap)
}
