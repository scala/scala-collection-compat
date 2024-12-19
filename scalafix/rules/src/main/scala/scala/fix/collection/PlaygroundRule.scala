/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.fix.collection

import scalafix.v0._
import scalafix.util._
import scala.meta._

case class PlaygroundRule(index: SemanticdbIndex) extends SemanticRule(index, "PlaygroundRule") {
  override def fix(ctx: RuleCtx): Patch = {
    // println("yolo")
    // ctx.tree.collect{
    //   case tree =>
    //     ctx.index.denotation(tree).foreach{ denot =>
    //       println("---")
    //       println(tree)
    //       println(denot)
    //     }
    // }

    Patch.empty
  }
}
