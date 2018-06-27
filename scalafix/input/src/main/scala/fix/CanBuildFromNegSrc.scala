/*
rule = "scala:fix.Scalacollectioncompat_newcollections"
 */
package fix

import scala.language.higherKinds

import collection.generic.CanBuildFrom

class CanBuildFromNegSrc() {

  // negative test
  def g[C0, A, C1[_]](c0: C0)(implicit cbf3: CanBuildFrom[C0, A, C1[A]]): C1[A] = {
    cbf3().result()
  }
}
