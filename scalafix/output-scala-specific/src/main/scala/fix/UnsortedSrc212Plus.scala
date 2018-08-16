package fix

import scala.collection.compat._
object UnsortedSrc212Plus {
  import Data._
  import Data212Plus._

  mSortedMap.unsorted.map(unorderedMap)
  mSortedMap.map(orderedMap)

  mTreeMap.unsorted.map(unorderedMap)
  mTreeMap.map(orderedMap)
}
