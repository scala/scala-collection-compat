


package fix

import scala.collection.compat._
object UnsortedSrc {
  import Data._

  // map

  cSet.map(ordered)
  cMap.map(orderedMap)

  cBitSet.unsortedSpecific.map(unordered)
  cBitSet.map(ordered)
  cSortedMap.unsortedSpecific.map(unorderedMap)
  cSortedMap.map(orderedMap)
  cSortedSet.unsortedSpecific.map(unordered)
  cSortedSet.map(ordered)

  iBitSet.unsortedSpecific.map(unordered)
  iBitSet.map(ordered)
  iSortedMap.unsortedSpecific.map(unorderedMap)
  iSortedMap.map(orderedMap)
  iSortedSet.unsortedSpecific.map(unordered)
  iSortedSet.map(ordered)
  iTreeMap.unsortedSpecific.map(unorderedMap)
  iTreeMap.map(orderedMap)
  iTreeSet.unsortedSpecific.map(unordered)
  iTreeSet.map(ordered)

  mBitSet.unsortedSpecific.map(unordered)
  mBitSet.map(ordered)
  mSortedSet.unsortedSpecific.map(unordered)
  mSortedSet.map(ordered)
  mTreeSet.unsortedSpecific.map(unordered)
  mTreeSet.map(ordered)

  // flatMap

  cSet.flatMap(ordered2)
  cMap.flatMap(orderedMap2)

  cBitSet.unsortedSpecific.flatMap(unordered2)
  cBitSet.flatMap(ordered2)
  cSortedMap.unsortedSpecific.flatMap(unorderedMap2)
  cSortedMap.flatMap(orderedMap2)
  cSortedSet.unsortedSpecific.flatMap(unordered2)
  cSortedSet.flatMap(ordered2)

  iBitSet.unsortedSpecific.flatMap(unordered2)
  iBitSet.flatMap(ordered2)
  iSortedMap.unsortedSpecific.flatMap(unorderedMap2)
  iSortedMap.flatMap(orderedMap2)
  iSortedSet.unsortedSpecific.flatMap(unordered2)
  iSortedSet.flatMap(ordered2)
  iTreeMap.unsortedSpecific.flatMap(unorderedMap2)
  iTreeMap.flatMap(orderedMap2)
  iTreeSet.unsortedSpecific.flatMap(unordered2)
  iTreeSet.flatMap(ordered2)

  mBitSet.unsortedSpecific.flatMap(unordered2)
  mBitSet.flatMap(ordered2)
  mSortedSet.unsortedSpecific.flatMap(unordered2)
  mSortedSet.flatMap(ordered2)
  mTreeSet.unsortedSpecific.flatMap(unordered2)
  mTreeSet.flatMap(ordered2)
}
