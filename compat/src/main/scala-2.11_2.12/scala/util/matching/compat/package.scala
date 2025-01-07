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

package scala.util.matching

package object compat {
  final implicit class RegexOps(val regex: Regex) extends AnyVal {

    /** Returns whether this `Regex` matches the given character sequence.
     *
     * Like the extractor, this method takes anchoring into account.
     *
     * @param source The text to match against
     * @return true if and only if `source` matches this `Regex`.
     * @see [[Regex#unanchored]]
     * @example {{{"""\d+""".r matches "123" // returns true}}}
     */
    def matches(source: CharSequence): Boolean = regex.pattern.matcher(source).matches()
  }

}
