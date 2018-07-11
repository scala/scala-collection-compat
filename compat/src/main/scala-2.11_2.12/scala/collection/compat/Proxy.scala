package scala.collection.compat

import scala.collection.GenTraversableLike
import scala.collection.generic
import scala.collection.{immutable => cimmutable}

// Provide 2.13 syntax for 2.12 collections

trait GrowableProxy[-A] { _: generic.Growable[A] =>
  def addOne(elem: A): this.type
  @`inline` override final def += (elem: A): this.type = addOne(elem)
}

trait ShrinkableProxy[-A] { _: generic.Shrinkable[A] =>
  def subtractOne(elem: A): this.type
  @`inline` override final def -= (elem: A): this.type = subtractOne(elem)
}

trait ClassNameProxy { _: GenTraversableLike[_, _] =>
  def className: String
  override def stringPrefix: String = className
}

trait ImmutableSetProxy[A] { _: cimmutable.Set[A] =>
  def excl(elem: A): cimmutable.Set[A]
  @`inline` final def - (elem: A): cimmutable.Set[A] = excl(elem)

  def incl(elem: A): cimmutable.Set[A]
  @`inline` def + (elem: A): cimmutable.Set[A] = incl(elem)
}

trait ImmutableMapProxy[A, B] { _: cimmutable.Map[A, B] =>
  def remove(key: A): cimmutable.Map[A, B]
  @`inline` final def - (key: A): cimmutable.Map[A, B] = remove(key)

  def updated[B1 >: B](key: A, value: B1): cimmutable.Map[A, B1]
  override def + [B1 >: B](kv: (A, B1)): cimmutable.Map[A, B1] = updated(kv._1, kv._2)
}

trait MapOps[A, B] {
  def filterInPlace(p: ((A, B)) => Boolean): this.type
}

trait SetOps[A] {
  def filterInPlace(p: A ⇒ Boolean): this.type
  def addOne(elem: A): this.type
}
