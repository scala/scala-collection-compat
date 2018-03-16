package scala
package collection
package immutable

import scala.reflect.ClassTag
import scala.runtime.BoxedUnit
import scala.collection.generic._
import scala.collection.mutable.{Builder, ArrayBuilder, ArrayBuffer, WrappedArrayBuilder}
import scala.util.hashing.MurmurHash3
import scala.annotation.unchecked.uncheckedVariance

import java.util.Arrays

/**
  * An immutable array.
  *
  * Supports efficient indexed access and has a small memory footprint.
  *
  *  @define Coll `ImmutableArray`
  *  @define coll wrapped array
  *  @define orderDependent
  *  @define orderDependentFold
  *  @define mayNotTerminateInf
  *  @define willNotTerminateInf
  */
abstract class ImmutableArray[+T]
  extends AbstractSeq[T]
    with IndexedSeq[T]
{

  override protected[this] def thisCollection: ImmutableArray[T] = this

  /** The tag of the element type */
  protected[this] def elemTag: ClassTag[T]

  /** The length of the array */
  def length: Int

  /** The element at given index */
  def apply(index: Int): T

  /** The underlying array */
  def unsafeArray: Array[T @uncheckedVariance]

  private def elementClass: Class[_] =
    unsafeArray.getClass.getComponentType

  override def stringPrefix = "ImmutableArray"

  /** Clones this object, including the underlying Array. */
  override def clone(): ImmutableArray[T] = ImmutableArray unsafeWrapArray unsafeArray.clone()

  /** Creates new builder for this collection ==> move to subclasses
    */
  override protected[this] def newBuilder: Builder[T, ImmutableArray[T]] =
    new WrappedArrayBuilder[T](elemTag).mapResult(w => ImmutableArray.unsafeWrapArray(w.array))

}

/** A companion object used to create instances of `ImmutableArray`.
  */
object ImmutableArray {
  // This is reused for all calls to empty.
  private val EmptyImmutableArray  = new ofRef[AnyRef](new Array[AnyRef](0))
  def empty[T <: AnyRef]: ImmutableArray[T] = EmptyImmutableArray.asInstanceOf[ImmutableArray[T]]

  // If make is called explicitly we use whatever we're given, even if it's
  // empty.  This may be unnecessary (if ImmutableArray is to honor the collections
  // contract all empty ones must be equal, so discriminating based on the reference
  // equality of an empty array should not come up) but we may as well be
  // conservative since wrapRefArray contributes most of the unnecessary allocations.
  def unsafeWrapArray[T](x: AnyRef): ImmutableArray[T] = (x match {
    case null              => null
    case x: Array[AnyRef]  => new ofRef[AnyRef](x)
    case x: Array[Int]     => new ofInt(x)
    case x: Array[Double]  => new ofDouble(x)
    case x: Array[Long]    => new ofLong(x)
    case x: Array[Float]   => new ofFloat(x)
    case x: Array[Char]    => new ofChar(x)
    case x: Array[Byte]    => new ofByte(x)
    case x: Array[Short]   => new ofShort(x)
    case x: Array[Boolean] => new ofBoolean(x)
    case x: Array[Unit]    => new ofUnit(x)
  }).asInstanceOf[ImmutableArray[T]]

  implicit def canBuildFrom[T](implicit m: ClassTag[T]): CanBuildFrom[ImmutableArray[_], T, ImmutableArray[T]] =
    new CanBuildFrom[ImmutableArray[_], T, ImmutableArray[T]] {
      def apply(from: ImmutableArray[_]): Builder[T, ImmutableArray[T]] =
        ArrayBuilder.make[T]()(m) mapResult ImmutableArray.unsafeWrapArray[T]
      def apply: Builder[T, ImmutableArray[T]] =
        ArrayBuilder.make[T]()(m) mapResult ImmutableArray.unsafeWrapArray[T]
    }

  final class ofRef[T <: AnyRef](val unsafeArray: Array[T]) extends ImmutableArray[T] with Serializable {
    lazy val elemTag = ClassTag[T](unsafeArray.getClass.getComponentType)
    def length: Int = unsafeArray.length
    def apply(index: Int): T = unsafeArray(index).asInstanceOf[T]
    def update(index: Int, elem: T) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofRef[_] => Arrays.equals(unsafeArray.asInstanceOf[Array[AnyRef]], that.unsafeArray.asInstanceOf[Array[AnyRef]])
      case _ => super.equals(that)
    }
  }

  final class ofByte(val unsafeArray: Array[Byte]) extends ImmutableArray[Byte] with Serializable {
    def elemTag = ClassTag.Byte
    def length: Int = unsafeArray.length
    def apply(index: Int): Byte = unsafeArray(index)
    def update(index: Int, elem: Byte) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedBytesHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofByte => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofShort(val unsafeArray: Array[Short]) extends ImmutableArray[Short] with Serializable {
    def elemTag = ClassTag.Short
    def length: Int = unsafeArray.length
    def apply(index: Int): Short = unsafeArray(index)
    def update(index: Int, elem: Short) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofShort => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofChar(val unsafeArray: Array[Char]) extends ImmutableArray[Char] with Serializable {
    def elemTag = ClassTag.Char
    def length: Int = unsafeArray.length
    def apply(index: Int): Char = unsafeArray(index)
    def update(index: Int, elem: Char) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofChar => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofInt(val unsafeArray: Array[Int]) extends ImmutableArray[Int] with Serializable {
    def elemTag = ClassTag.Int
    def length: Int = unsafeArray.length
    def apply(index: Int): Int = unsafeArray(index)
    def update(index: Int, elem: Int) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofInt => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofLong(val unsafeArray: Array[Long]) extends ImmutableArray[Long] with Serializable {
    def elemTag = ClassTag.Long
    def length: Int = unsafeArray.length
    def apply(index: Int): Long = unsafeArray(index)
    def update(index: Int, elem: Long) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofLong => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofFloat(val unsafeArray: Array[Float]) extends ImmutableArray[Float] with Serializable {
    def elemTag = ClassTag.Float
    def length: Int = unsafeArray.length
    def apply(index: Int): Float = unsafeArray(index)
    def update(index: Int, elem: Float) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofFloat => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofDouble(val unsafeArray: Array[Double]) extends ImmutableArray[Double] with Serializable {
    def elemTag = ClassTag.Double
    def length: Int = unsafeArray.length
    def apply(index: Int): Double = unsafeArray(index)
    def update(index: Int, elem: Double) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofDouble => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofBoolean(val unsafeArray: Array[Boolean]) extends ImmutableArray[Boolean] with Serializable {
    def elemTag = ClassTag.Boolean
    def length: Int = unsafeArray.length
    def apply(index: Int): Boolean = unsafeArray(index)
    def update(index: Int, elem: Boolean) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofBoolean => Arrays.equals(unsafeArray, that.unsafeArray)
      case _ => super.equals(that)
    }
  }

  final class ofUnit(val unsafeArray: Array[Unit]) extends ImmutableArray[Unit] with Serializable {
    def elemTag = ClassTag.Unit
    def length: Int = unsafeArray.length
    def apply(index: Int): Unit = unsafeArray(index)
    def update(index: Int, elem: Unit) { unsafeArray(index) = elem }
    override def hashCode = MurmurHash3.wrappedArrayHash(unsafeArray)
    override def equals(that: Any) = that match {
      case that: ofUnit => unsafeArray.length == that.unsafeArray.length
      case _ => super.equals(that)
    }
  }
}
