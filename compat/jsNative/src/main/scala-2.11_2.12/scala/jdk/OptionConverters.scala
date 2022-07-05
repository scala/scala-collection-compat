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

package scala.jdk

import java.util.Optional

/** This object provides extension methods that convert between Scala `Option` and Java `Optional`
 * types.
 *
 * It differs from the JVM version as in it does not provide any conversions for the Optional primitive type
 * wrappers which are available in the JDK but not in Scala-JS or Scala-Native.
 *
 * Scala `Option` is extended with a `toJava` method that creates a corresponding `Optional`.
 *
 * Java `Optional` is extended with a `toScala` method.
 *
 *
 * Example usage:
 *
 * {{{
 *   import scala.jdk.OptionConverters._
 *   val a = Option("example").toJava      // Creates java.util.Optional[String] containing "example"
 *   val b = (None: Option[String]).toJava // Creates an empty java.util.Optional[String]
 *   val c = a.toScala                     // Back to Option("example")
 *   val d = b.toScala                     // Back to None typed as Option[String]
 * }}}
 */
object OptionConverters {

  /** Provides conversions from Java `Optional` to Scala `Option` and specialized `Optional` types */
  implicit class RichOptional[A](private val o: java.util.Optional[A]) extends AnyVal {

    /** Convert a Java `Optional` to a Scala `Option` */
    def toScala: Option[A] = if (o.isPresent) Some(o.get) else None

    /** Convert a Java `Optional` to a Scala `Option` */
    @deprecated("Use `toScala` instead", "2.13.0")
    def asScala: Option[A] = if (o.isPresent) Some(o.get) else None
  }

  /** Provides conversions from Scala `Option` to Java `Optional` types */
  implicit class RichOption[A](private val o: Option[A]) extends AnyVal {

    /** Convert a Scala `Option` to a generic Java `Optional` */
    def toJava: Optional[A] = o match {
      case Some(a) => Optional.ofNullable(a); case _ => Optional.empty[A]
    }

    /** Convert a Scala `Option` to a generic Java `Optional` */
    @deprecated("Use `toJava` instead", "2.13.0")
    def asJava: Optional[A] = o match {
      case Some(a) => Optional.ofNullable(a); case _ => Optional.empty[A]
    }
  }
}
