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

package test.scala.jdk.javaapi;

import org.junit.Assert;
import org.junit.Test;
import scala.Array;
import scala.Tuple2;
import scala.collection.Iterable;
import scala.collection.Iterator;
import scala.collection.concurrent.TrieMap;
import scala.collection.mutable.*;
import scala.collection.mutable.Map;
import scala.collection.mutable.Set;
import scala.jdk.javaapi.CollectionConverters;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class CollectionConvertersTest {

    /**
     * The following conversions are supported via asScala and asJava:
     *
     * scala.collection.Iterable       <=> java.lang.Iterable
     * scala.collection.Iterator       <=> java.util.Iterator
     * scala.collection.mutable.Buffer <=> java.util.List
     * scala.collection.mutable.Set    <=> java.util.Set
     * scala.collection.mutable.Map    <=> java.util.Map
     * scala.collection.concurrent.Map <=> java.util.concurrent.ConcurrentMap
     */
    @Test
    public void shouldConvertAsScala() {
        // scala.collection.Iterable       <=> java.lang.Iterable
        java.lang.Iterable iterable = CollectionConverters.asJava(TestObjects.iterable());
        Assert.assertEquals("A", iterable.iterator().next());
        Iterable scalaIterable = CollectionConverters.asScala(iterable);
        Assert.assertEquals(TestObjects.iterable().head(), scalaIterable.head());

        // scala.collection.Iterator       <=> java.util.Iterator
        java.util.Iterator iterator = CollectionConverters.asJava(TestObjects.iterator());
        Assert.assertEquals("A", iterator.next());
        Iterator scalaIterator = CollectionConverters.asScala(iterator);
        Assert.assertTrue(scalaIterator.contains("B"));

        // scala.collection.mutable.Buffer <=> java.util.List
        List<String> list = CollectionConverters.asJava(TestObjects.buffer());
        Assert.assertEquals("A", list.get(0));
        Buffer<String> scalaBuffer = CollectionConverters.asScala(list);
        Assert.assertEquals("A", scalaBuffer.head());

        // scala.collection.mutable.Set    <=> java.util.Set
        java.util.Set<String> set = CollectionConverters.asJava(TestObjects.mutableSet());
        Assert.assertTrue(set.contains("A"));
        Set<String> scalaSet = CollectionConverters.asScala(set);
        Assert.assertTrue(scalaSet.contains("A"));

        // scala.collection.mutable.Map    <=> java.util.Map
        java.util.Map<String, String> map = CollectionConverters.asJava(TestObjects.mutableMap());
        Assert.assertEquals("B", map.get("A"));
        Map<String, String> scalaMap = CollectionConverters.asScala(map);
        Assert.assertEquals("B", scalaMap.get("A").get());

        // scala.collection.concurrent.Map <=> java.util.concurrent.ConcurrentMap
        ConcurrentMap<String, String> concurrentMap = CollectionConverters.asJava(TestObjects.concurrentMap());
        Assert.assertEquals("B", concurrentMap.get("A"));
        scala.collection.concurrent.Map<String, String> scalaConcurrentMap = CollectionConverters.asScala(concurrentMap);
        Assert.assertEquals("B", scalaConcurrentMap.get("A").get());
    }

    /**
     * The following conversions are supported via asScala and through specially-named methods to convert to Java collections, as shown:
     *
     * scala.collection.Iterable    <=> java.util.Collection   (via asJavaCollection)
     * scala.collection.Iterator    <=> java.util.Enumeration  (via asJavaEnumeration)
     * scala.collection.mutable.Map <=> java.util.Dictionary   (via asJavaDictionary)
     */
    public void convertAsCollection() {
        // scala.collection.Iterable    <=> java.util.Collection   (via asJavaCollection)
        Collection<String> collection = CollectionConverters.asJavaCollection(TestObjects.iterable());
        Assert.assertTrue(collection.contains("A"));
        Iterable<String> iterable = CollectionConverters.asScala(collection);
        Assert.assertEquals("A", iterable.head());

        // scala.collection.Iterator    <=> java.util.Enumeration  (via asJavaEnumeration)
        Enumeration<String> enumeration = CollectionConverters.asJavaEnumeration(TestObjects.iterator());
        Assert.assertEquals("A", enumeration.nextElement());
        Iterator<String> iterator = CollectionConverters.asScala(enumeration);
        Assert.assertEquals("A", iterator.next());

        // scala.collection.mutable.Map <=> java.util.Dictionary   (via asJavaDictionary)
        Dictionary<String, String> dictionary = CollectionConverters.asJavaDictionary(TestObjects.mutableMap());
        Assert.assertEquals("B", dictionary.get("A"));
        Map<String, String> map = CollectionConverters.asScala(dictionary);
        Assert.assertEquals("B", map.get("A").get());
    }

    /** In addition, the following one-way conversions are provided via asJava:
     *
     * scala.collection.Seq         => java.util.List
     * scala.collection.mutable.Seq => java.util.List
     * scala.collection.Set         => java.util.Set
     * scala.collection.Map         => java.util.Map
     */
    public void convertsAsJava() {
        // scala.collection.Seq         => java.util.List
        Assert.assertEquals("A", CollectionConverters.asJava(TestObjects.seq()).get(0));

        // scala.collection.mutable.Seq => java.util.List
        Assert.assertEquals("A", CollectionConverters.asJava(TestObjects.mutableSeq()).get(0));

        // scala.collection.Set         => java.util.Set
        Assert.assertTrue(CollectionConverters.asJava(TestObjects.set()).contains("A"));

        // scala.collection.Map         => java.util.Map
        Assert.assertEquals("B", CollectionConverters.asJava(TestObjects.map()).get("A"));
    }

    /**
     * The following one way conversion is provided via asScala:
     *
     * java.util.Properties => scala.collection.mutable.Map
     */
    public void convertsFromProperties() {
        Properties properties = new Properties();
        properties.put("key", "value");
        Map<String, String> stringStringMap = CollectionConverters.asScala(properties);
        Assert.assertEquals("value", stringStringMap.get("key").get());
    }
}
