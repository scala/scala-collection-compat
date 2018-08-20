package fix

import scala.collection.compat._
object UnsortedSrc {
  import Data._

  // map

  cSet.map(ordered)
  cMap.map(orderedMap)

  cBitSet.unsorted.map(unordered)
  cBitSet.map(ordered)
  cSortedMap.unsorted.map(unorderedMap)
  cSortedMap.map(orderedMap)
  cSortedSet.unsorted.map(unordered)
  cSortedSet.map(ordered)

  iBitSet.unsorted.map(unordered)
  iBitSet.map(ordered)
  iSortedMap.unsorted.map(unorderedMap)
  iSortedMap.map(orderedMap)
  iSortedSet.unsorted.map(unordered)
  iSortedSet.map(ordered)
  iTreeMap.unsorted.map(unorderedMap)
  iTreeMap.map(orderedMap)
  iTreeSet.unsorted.map(unordered)
  iTreeSet.map(ordered)

  mBitSet.unsorted.map(unordered)
  mBitSet.map(ordered)
  mSortedSet.unsorted.map(unordered)
  mSortedSet.map(ordered)
  mTreeSet.unsorted.map(unordered)
  mTreeSet.map(ordered)

  // flatMap

  cSet.flatMap(ordered2)
  cMap.flatMap(orderedMap2)

  cBitSet.unsorted.flatMap(unordered2)
  cBitSet.flatMap(ordered2)
  cSortedMap.unsorted.flatMap(unorderedMap2)
  cSortedMap.flatMap(orderedMap2)
  cSortedSet.unsorted.flatMap(unordered2)
  cSortedSet.flatMap(ordered2)

  iBitSet.unsorted.flatMap(unordered2)
  iBitSet.flatMap(ordered2)
  iSortedMap.unsorted.flatMap(unorderedMap2)
  iSortedMap.flatMap(orderedMap2)
  iSortedSet.unsorted.flatMap(unordered2)
  iSortedSet.flatMap(ordered2)
  iTreeMap.unsorted.flatMap(unorderedMap2)
  iTreeMap.flatMap(orderedMap2)
  iTreeSet.unsorted.flatMap(unordered2)
  iTreeSet.flatMap(ordered2)

  mBitSet.unsorted.flatMap(unordered2)
  mBitSet.flatMap(ordered2)
  mSortedSet.unsorted.flatMap(unordered2)
  mSortedSet.flatMap(ordered2)
  mTreeSet.unsorted.flatMap(unordered2)
  mTreeSet.flatMap(ordered2)
}
