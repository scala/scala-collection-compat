/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.annotation

/** An annotation for local warning suppression added in 2.13.2 and 2.12.13. Note that this
 * annotation has no functionality when used in Scala 2.11, but allows cross-compiling code
 * that uses `@nowarn`.
 *
 * For documentation on how to use the annotation in 2.13 see
 * https://www.scala-lang.org/api/current/scala/annotation/nowarn.html
 */
class nowarn(value: String = "") extends ClassfileAnnotation
