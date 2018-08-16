/*
rule = "scala:fix.Experimental"
 */
package fix

object UnsortedSrc {
  import Data._

  // map

  cSet.map(ordered)
  cMap.map(orderedMap)

  cBitSet.map(unordered)
  cBitSet.map(ordered)
  cSortedMap.map(unorderedMap)
  cSortedMap.map(orderedMap)
  cSortedSet.map(unordered)
  cSortedSet.map(ordered)

  iBitSet.map(unordered)
  iBitSet.map(ordered)
  iSortedMap.map(unorderedMap)
  iSortedMap.map(orderedMap)
  iSortedSet.map(unordered)
  iSortedSet.map(ordered)
  iTreeMap.map(unorderedMap)
  iTreeMap.map(orderedMap)
  iTreeSet.map(unordered)
  iTreeSet.map(ordered)

  mBitSet.map(unordered)
  mBitSet.map(ordered)
  mSortedSet.map(unordered)
  mSortedSet.map(ordered)
  mTreeSet.map(unordered)
  mTreeSet.map(ordered)

  // flatMap

  cSet.flatMap(ordered2)
  cMap.flatMap(orderedMap2)

  cBitSet.flatMap(unordered2)
  cBitSet.flatMap(ordered2)
  cSortedMap.flatMap(unorderedMap2)
  cSortedMap.flatMap(orderedMap2)
  cSortedSet.flatMap(unordered2)
  cSortedSet.flatMap(ordered2)

  iBitSet.flatMap(unordered2)
  iBitSet.flatMap(ordered2)
  iSortedMap.flatMap(unorderedMap2)
  iSortedMap.flatMap(orderedMap2)
  iSortedSet.flatMap(unordered2)
  iSortedSet.flatMap(ordered2)
  iTreeMap.flatMap(unorderedMap2)
  iTreeMap.flatMap(orderedMap2)
  iTreeSet.flatMap(unordered2)
  iTreeSet.flatMap(ordered2)

  mBitSet.flatMap(unordered2)
  mBitSet.flatMap(ordered2)
  mSortedSet.flatMap(unordered2)
  mSortedSet.flatMap(ordered2)
  mTreeSet.flatMap(unordered2)
  mTreeSet.flatMap(ordered2)
}
