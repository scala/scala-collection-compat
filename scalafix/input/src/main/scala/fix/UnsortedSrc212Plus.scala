/*
rule = "scala:fix.Experimental"
 */
package fix

object UnsortedSrc212Plus {
  import Data._
  import Data212Plus._

  mSortedMap.map(unorderedMap)
  mSortedMap.map(orderedMap)

  mTreeMap.map(unorderedMap)
  mTreeMap.map(orderedMap)
}
