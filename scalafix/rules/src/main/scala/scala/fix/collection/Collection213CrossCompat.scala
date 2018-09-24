package scala.fix.collection

import scalafix.v0._

import scalafix.internal.v0.LegacySemanticRule

class Collection213CrossCompat
    extends LegacySemanticRule("Collection213CrossCompat",
                               index => new Collection213CrossCompatV0(index))
    with Stable212BaseCheck {
  override def description: String =
    "Upgrade to 2.13’s collections with cross compatibility for 2.11 and 2.12 (for libraries)"
}

case class Collection213CrossCompatV0(index: SemanticdbIndex)
    extends SemanticRule(index, "CrossCompat")
    with CrossCompatibility
    with Stable212Base {

  def isCrossCompatible: Boolean = true
}
