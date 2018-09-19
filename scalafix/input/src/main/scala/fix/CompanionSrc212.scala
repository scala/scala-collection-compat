/*
rule = "Collection213CrossCompat"
 */
package fix

import scala.collection.{immutable => i, mutable => m}
import scala.{collection => c}

object CompanionSrc212 {
  (null: i.Stack[Int]).companion
  (null: m.DoubleLinkedList[Int]).companion
  (null: m.LinkedList[Int]).companion
  (null: m.MutableList[Int]).companion
  (null: m.ResizableArray[Int]).companion
}

