package fix

import scalafix._

case class CrossCompat(index: SemanticdbIndex) extends SemanticRule(index, "CrossCompat") with Stable212Base
