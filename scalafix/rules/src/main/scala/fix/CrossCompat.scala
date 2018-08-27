package fix

import scalafix.v0._

case class CrossCompat(index: SemanticdbIndex)
    extends SemanticRule(index, "CrossCompat")
    with CrossCompatibility
    with Stable212Base {

  def isCrossCompatible: Boolean = true
}
