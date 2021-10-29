package test.scala.collection

import org.junit.Assert.{assertSame, assertNotSame}
import org.junit.Test

import scala.collection.compat._
import scala.collection.compat.immutable.LazyList
import scala.{collection => c}
import scala.collection.{immutable => i, mutable => m}

class ToConserveTest {
  @Test def toConserveList: Unit = {
    val l: c.Iterable[Int] = (1 to 3).toList

    assertSame(l, l.toList)
    assertSame(l, l.toSeq)
    assertSame(l, l.toIterable)
    assertSame(l, l.toTraversable)

    assertSame(l, l.to(c.Traversable))
    assertSame(l, l.to(i.Traversable))

    assertSame(l, l.to(c.Iterable))
    assertSame(l, l.to(i.Iterable))

    assertSame(l, l.to(c.Seq))
    assertSame(l, l.to(i.Seq))

    assertSame(l, l.to(c.LinearSeq))
    assertSame(l, l.to(i.LinearSeq))

    assertSame(l, l.to(List))
  }

  @Test def toConserveImmutableHashSet: Unit = {
    val s: c.Iterable[Int] = (1 to 10).to(i.HashSet)
    assertSame(s, s.toSet)
    assertSame(s, s.toIterable)

    assertSame(s, s.to(c.Iterable))
    assertSame(s, s.to(i.Iterable))

    assertSame(s, s.to(c.Set))
    assertSame(s, s.to(i.Set))

    assertSame(s, s.to(i.HashSet))

    val ts: c.Iterable[Int] = s.to(i.TreeSet)
    assertSame(ts, ts.to(c.SortedSet))
    assertSame(ts, ts.to(i.SortedSet))
    assertSame(ts, ts.to(i.TreeSet))

    val bs: c.Iterable[Int] = s.to(i.BitSet)
    assertSame(bs, bs.to(c.SortedSet))
    assertSame(bs, bs.to(i.SortedSet))
    assertSame(bs, bs.to(c.BitSet))
    assertSame(bs, bs.to(i.BitSet))
  }

  @Test def toConserveImmutableHashMap: Unit = {
    val m: c.Iterable[(Int, Int)] = i.HashMap((1 to 10).map(x => (x, x)): _*)

    assertSame(m, m.toMap)
    assertSame(m, m.toIterable)

    assertSame(m, m.to(c.Iterable))
    assertSame(m, m.to(i.Iterable))

    // doesn't compile. found: Map.type, required: CanBuildFrom
    // that's because mapFactoryToCBF's signature should accept c.Map, not i.Map, but bin compat
    // assertSame(m, m.to(c.Map))
    assertSame(m, m.to(i.Map))

    assertSame(m, m.to(i.HashMap))

    val lm = m.to(i.ListMap)
    assertSame(lm, lm.to(i.ListMap))

    val tm = m.to(i.TreeMap)
    assertSame(tm, tm.to(c.SortedMap))
    assertSame(tm, tm.to(i.SortedMap))
    assertSame(tm, tm.to(i.TreeMap))
  }

  @Test def toConserveLazyList: Unit = {
    val l: c.Iterable[Int] = LazyList.from(1 to 10)

    assertSame(l, l.toSeq)
    assertSame(l, l.toIterable)

    assertSame(l, l.to(c.Iterable))
    assertSame(l, l.to(i.Iterable))

    assertSame(l, l.to(c.Seq))
    assertSame(l, l.to(i.Seq))

    assertSame(l, l.to(c.LinearSeq))
    assertSame(l, l.to(i.LinearSeq))

    assertSame(l, l.to(LazyList))
  }

  @Test def toConserveStream: Unit = {
    val l: c.Iterable[Int] = Stream.from(1 to 10)

    assertSame(l, l.toStream)
    assertSame(l, l.toSeq)
    assertSame(l, l.toIterable)

    assertSame(l, l.to(c.Iterable))
    assertSame(l, l.to(i.Iterable))

    assertSame(l, l.to(c.Seq))
    assertSame(l, l.to(i.Seq))

    assertSame(l, l.to(c.LinearSeq))
    assertSame(l, l.to(i.LinearSeq))

    assertSame(l, l.to(Stream))
  }

  @Test def toRebuildMutable: Unit = {
    val s: c.Iterable[Int] = (1 to 3).to(m.HashSet)
    assertSame(s, s.toIterable) // slightly inconsistent...
    assertNotSame(s, s.to(c.Iterable))
    assertNotSame(s, s.to(m.Iterable))
    assertNotSame(s, s.to(c.Set))
    assertNotSame(s, s.to(m.Set))
    assertNotSame(s, s.to(m.HashSet))

    val b: c.Iterable[Int] = (1 to 6).to(m.ArrayBuffer)
    assertSame(b, b.toIterable) // slightly inconsistent...
    assertNotSame(b, b.toBuffer)
    assertNotSame(b, b.to(c.Iterable))
    assertNotSame(b, b.to(m.Iterable))
    assertNotSame(b, b.to(c.Seq))
    assertNotSame(b, b.to(m.Seq))
    assertNotSame(b, b.to(m.Buffer))
    assertNotSame(b, b.to(m.ArrayBuffer))
  }
}
