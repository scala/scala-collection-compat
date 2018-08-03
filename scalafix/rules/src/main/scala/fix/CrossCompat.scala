package fix

import scalafix._

case class CrossCompat(index: SemanticdbIndex)
    extends SemanticRule(index, "CrossCompat")
    with CrossCompatibility
    with Stable212Base {

  def isCrossCompatible: Boolean = true
}
