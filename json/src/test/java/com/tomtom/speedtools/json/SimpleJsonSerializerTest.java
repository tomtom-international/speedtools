/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.speedtools.json;

import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.utils.MathUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("ConstantConditions")
public class SimpleJsonSerializerTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleJsonSerializerTest.class);

    private final SimpleJsonSerializer serializer = SimpleJsonSerializer.getInstance();

    // ------------------------------------------------------------------------
    // Test simple types.
    // ------------------------------------------------------------------------

    @Test
    public void testChar() {
        LOG.info("testChar");

        char before = 'A';
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Character);
        char after = (Character) deserialized;
        Assert.assertEquals(before, after);

        for (int i = 0; i < 255; ++i) {
            //noinspection NumericCastThatLosesPrecision
            before = (char) i;
            serialized = serializer.serialize(before);
            deserialized = serializer.deserialize(serialized);
            Assert.assertTrue(deserialized instanceof Character);
            after = (Character) deserialized;
            Assert.assertEquals(before, after);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testString() {
        LOG.info("testString");

        // Null string.
        String before = null;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertNull(deserialized);

        // Empty string.
        before = "";
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof String);
        String after = (String) deserialized;
        Assert.assertEquals(before, after);

        // Non-empty string.
        before = "A";
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof String);
        after = (String) deserialized;
        Assert.assertEquals(before, after);

        // Non-empty string, with ctrl char.
        before = "\n";
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof String);
        after = (String) deserialized;
        Assert.assertEquals(before, after);

        before = "A\n";
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof String);
        after = (String) deserialized;
        Assert.assertEquals(before, after);

        before = "\nA";
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof String);
        after = (String) deserialized;
        Assert.assertEquals(before, after);

        before = "A" + ((char) 30) + 'B';
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof String);
        after = (String) deserialized;
        Assert.assertEquals(before, after);
    }

    @Test
    public void testBoolean() {
        LOG.info("testBoolean");

        boolean before = false;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Boolean);
        boolean after = (Boolean) deserialized;
        Assert.assertEquals(before, after);

        before = true;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Boolean);
        after = (Boolean) deserialized;
        Assert.assertEquals(before, after);
    }

    @Test
    public void testInteger() {
        LOG.info("testInteger");

        int before = Integer.MIN_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Integer);
        int after = (Integer) deserialized;
        Assert.assertEquals(before, after);

        before = Integer.MAX_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Integer);
        after = (Integer) deserialized;
        Assert.assertEquals(before, after);
    }

    @Test
    public void testLong() {
        LOG.info("testLong");

        long before = Long.MIN_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Long);
        long after = (Long) deserialized;
        Assert.assertEquals(before, after);

        before = Long.MAX_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Long);
        after = (Long) deserialized;
        Assert.assertEquals(before, after);
    }

    @Test
    public void testShort() {
        LOG.info("testShort");

        short before = Short.MIN_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Short);
        short after = (Short) deserialized;
        Assert.assertEquals(before, after);

        before = Short.MAX_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Short);
        after = (Short) deserialized;
        Assert.assertEquals(before, after);
    }

    @Test
    public void testByte() {
        LOG.info("testByte");

        byte before = Byte.MIN_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Byte);
        byte after = (Byte) deserialized;
        Assert.assertEquals(before, after);

        before = Byte.MAX_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Byte);
        after = (Byte) deserialized;
        Assert.assertEquals(before, after);
    }

    @Test
    public void testFloat() {
        LOG.info("testFloat");

        float before = Float.MIN_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Float);
        float after = (Float) deserialized;
        Assert.assertTrue(MathUtils.isAlmostEqual(before, after));

        before = Float.MAX_VALUE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Float);
        after = (Float) deserialized;
        Assert.assertTrue(MathUtils.isAlmostEqual(before, after));
    }

    @Test
    public void testDouble() {
        LOG.info("testDouble");

        double before = Double.MIN_VALUE;
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Double);
        double after = (Double) deserialized;
        Assert.assertTrue(MathUtils.isAlmostEqual(before, after));

        before = Double.MAX_VALUE;
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Double);
        after = (Double) deserialized;
        Assert.assertTrue(MathUtils.isAlmostEqual(before, after));
    }

    @Test
    public void testDateTime() {
        LOG.info("testDateTime");

        DateTime before = UTCTime.now();
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof DateTime);
        DateTime after = (DateTime) deserialized;
        Assert.assertEquals(before, after);

        before = UTCTime.now().withZone(DateTimeZone.forOffsetHours(2));
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof DateTime);
        after = (DateTime) deserialized;
        Assert.assertEquals(before.withZone(DateTimeZone.UTC), after);
    }

    @Test
    public void testCurrency() {
        LOG.info("testCurrency");

        Currency before = Currency.getInstance("USD");
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Currency);
        Currency after = (Currency) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));

        before = Currency.getInstance("EUR");
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Currency);
        after = (Currency) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));
    }

    @Test
    public void testUid() {
        LOG.info("testUid");

        Uid<?> before = Uid.fromString("1-2-3-4-5");
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Uid);
        Uid<?> after = (Uid) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));

        before = new Uid();
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Uid);
        after = (Uid) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));
    }

    @Test
    public void testEnum() {
        LOG.info("testEnum");

        Gender before = Gender.MALE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Gender);
        Gender after = (Gender) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));

        before = Gender.FEMALE;
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Gender);
        after = (Gender) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlainList() {
        LOG.info("testPlainList");

        // Create empty list.
        ArrayList<Integer> before = new ArrayList<>();
        Assert.assertEquals(0, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        List<Integer> after = (List<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(0, after.size());

        // List with 1 element.
        before = new ArrayList<>();
        before.add(1);
        Assert.assertEquals(1, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        after = (List<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(1, after.size());
        Assert.assertEquals(1, after.get(0).intValue());

        // List with 2 elements.
        before = new ArrayList<>();
        before.add(2);
        before.add(3);
        Assert.assertEquals(2, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        after = (List<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(2, after.size());
        Assert.assertEquals(2, after.get(0).intValue());
        Assert.assertEquals(3, after.get(1).intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlainSet() {
        LOG.info("testPlainSet");

        // Create empty set.
        LinkedHashSet<Integer> before = new LinkedHashSet<>();
        Assert.assertEquals(0, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Set);
        Set<Integer> after = (Set<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(0, after.size());

        // Set with 1 element.
        before = new LinkedHashSet<>();
        before.add(1);
        Assert.assertEquals(1, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Set);
        after = (Set<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(1, after.size());
        Assert.assertTrue(after.contains(1));

        // Set with 2 elements.
        before = new LinkedHashSet<>();
        before.add(2);
        before.add(3);
        Assert.assertEquals(2, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Set);
        after = (Set<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(2, after.size());
        Assert.assertTrue(after.contains(2));
        Assert.assertTrue(after.contains(3));
    }

    // ------------------------------------------------------------------------
    // Test immutables.
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Test
    public void testImmutablesList() {
        LOG.info("testImmutablesList");

        // Create empty list.
        Collection<Integer> before = Immutables.emptyList();
        Assert.assertEquals(0, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        List<Integer> after = (List<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(0, after.size());

        // Use Immutables.listOf().
        final List<Integer> mutable = new ArrayList<>();
        mutable.add(100);
        mutable.add(200);
        mutable.add(300);

        before = Immutables.listOf(mutable);
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        after = (List<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(3, after.size());
        Assert.assertEquals((Integer) 100, after.get(0));
        Assert.assertEquals((Integer) 200, after.get(1));
        Assert.assertEquals((Integer) 300, after.get(2));

        // Use Collections.unmodifiableList().
        before = Collections.unmodifiableList(mutable);
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        after = (List<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(3, after.size());
        Assert.assertEquals((Integer) 100, after.get(0));
        Assert.assertEquals((Integer) 200, after.get(1));
        Assert.assertEquals((Integer) 300, after.get(2));

        // TODO: Fix serializer.
        // Use Collections.unmodifiableCollection().

//        before = Collections.unmodifiableCollection(mutable);
//        serialized = serializer.serialize(before);
//        deserialized = serializer.deserialize(serialized);
//        Assert.assertTrue(deserialized instanceof List);
//        after = (List<Integer>) deserialized;
//        Assert.assertNotNull(after);
//        Assert.assertEquals(3, after.size());
//        Assert.assertEquals((Integer) 100, after.get(0));
//        Assert.assertEquals((Integer) 200, after.get(1));
//        Assert.assertEquals((Integer) 300, after.get(2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testImmutablesSet() {
        LOG.info("testImmutablesSet");

        // Create empty set.
        Collection<Integer> before = Immutables.emptySet();
        Assert.assertEquals(0, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Set);
        Set<Integer> after = (Set<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(0, after.size());

        // Use Immutables.listOf().
        final Set<Integer> mutable = new HashSet<>();
        mutable.add(100);
        mutable.add(200);
        mutable.add(300);

        before = Immutables.setOf(mutable);
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Set);
        after = (Set<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(3, after.size());
        Assert.assertTrue(after.contains(100));
        Assert.assertTrue(after.contains(200));
        Assert.assertTrue(after.contains(300));

        // Use Collections.unmodifiableSet().
        before = Collections.unmodifiableSet(mutable);
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Set);
        after = (Set<Integer>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(3, after.size());
        Assert.assertTrue(after.contains(100));
        Assert.assertTrue(after.contains(200));
        Assert.assertTrue(after.contains(300));

        // TODO: Fix serializer.

        // Use Collections.unmodifiableCollection().
//        before = Collections.unmodifiableCollection(mutable);
//        serialized = serializer.serialize(before);
//        deserialized = serializer.deserialize(serialized);
//        Assert.assertTrue(deserialized instanceof Set);
//        after = (Set<Integer>) deserialized;
//        Assert.assertNotNull(after);
//        Assert.assertEquals(3, after.size());
//        Assert.assertTrue(after.contains(100));
//        Assert.assertTrue(after.contains(200));
//        Assert.assertTrue(after.contains(300));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testImmutablesMap() {
        LOG.info("testImmutablesMap");

        // Use Immutables.listOf().
        final Map<String, Integer> mutable = new HashMap<>();
        mutable.put("x", 100);
        mutable.put("y", 200);
        mutable.put("z", 300);
        Assert.assertTrue(mutable.size() == 3);

        // TODO: Fix serializer.
//        Map<String, Integer> before = Immutables.emptyMap();
//        Assert.assertEquals(0, before.size());
//        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
//
//        Object serialized = serializer.serialize(before);
//        Object deserialized = serializer.deserialize(serialized);
//        Assert.assertTrue(deserialized instanceof Map);
//        Map<String, Integer> after = (Map<String, Integer>) deserialized;
//        Assert.assertNotNull(after);
//        Assert.assertEquals(0, after.size());
//
//        before = Immutables.mapOf(mutable);
//        serialized = serializer.serialize(before);
//        deserialized = serializer.deserialize(serialized);
//        Assert.assertTrue(deserialized instanceof Map);
//        after = (Map<String, Integer>) deserialized;
//        Assert.assertNotNull(after);
//        Assert.assertEquals(3, after.size());
//        Assert.assertEquals((Integer) 100, after.get("x"));
//        Assert.assertEquals((Integer) 200, after.get("y"));
//        Assert.assertEquals((Integer) 300, after.get("z"));
//
//        before = Collections.unmodifiableMap(mutable);
//        serialized = serializer.serialize(before);
//        deserialized = serializer.deserialize(serialized);
//        Assert.assertTrue(deserialized instanceof Map);
//        after = (Map<String, Integer>) deserialized;
//        Assert.assertNotNull(after);
//        Assert.assertEquals(3, after.size());
//        Assert.assertEquals((Integer) 100, after.get("x"));
//        Assert.assertEquals((Integer) 200, after.get("y"));
//        Assert.assertEquals((Integer) 300, after.get("z"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testImmutablesCombined() {
        LOG.info("testImmutablesCombined");

        // Create empty list.
        List<Set<Integer>> before = Immutables.emptyList();
        Assert.assertEquals(0, before.size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        List<Set<Integer>> after = (List<Set<Integer>>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(0, after.size());

        // Create empty set in list.
        before = new ArrayList<>();
        Set<Integer> set = Immutables.emptySet();
        before.add(set);
        Assert.assertEquals(1, before.size());
        Assert.assertEquals(0, before.get(0).size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        after = (List<Set<Integer>>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(1, after.size());
        Assert.assertEquals(0, after.get(0).size());

        // Create empty set in list.
        before = new ArrayList<>();
        set = new LinkedHashSet<>();
        set.add(10);
        set.add(20);
        set.add(30);
        before.add(Immutables.setOf(set));
        before = Immutables.listOf(before);
        Assert.assertEquals(1, before.size());
        Assert.assertEquals(3, before.get(0).size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof List);
        after = (List<Set<Integer>>) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(1, after.size());
        Assert.assertEquals(3, after.get(0).size());
    }

    // ------------------------------------------------------------------------
    // Test collections as property in a class.
    // ------------------------------------------------------------------------

    private static class MyCollections {
        private final List<Set<Integer>> list;

        public MyCollections(@Nonnull final List<Set<Integer>> list) {
            this.list = list;
        }

        @Nonnull
        public List<Set<Integer>> getList() {
            return list;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCollectionsInClass() {
        LOG.info("testCollectionsInClass");

        // Create empty list.
        List<Set<Integer>> list = new ArrayList<>();
        MyCollections before = new MyCollections(list);
        Assert.assertEquals(0, before.getList().size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof MyCollections);
        MyCollections after = (MyCollections) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(0, after.getList().size());

        // List with 1 element which is empty set.
        list = new ArrayList<>();
        final Set<Integer> set0 = Immutables.emptySet();
        list.add(set0);
        before = new MyCollections(list);
        Assert.assertEquals(1, before.getList().size());
        Assert.assertEquals(0, before.getList().get(0).size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof MyCollections);
        after = (MyCollections) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(1, after.getList().size());
        Assert.assertEquals(0, after.getList().get(0).size());

        // List with 1 element which is set with 1 element.
        list = new ArrayList<>();
        final LinkedHashSet<Integer> set1 = new LinkedHashSet<>();
        set1.add(1);
        list.add(set0);
        list.add(set1);
        before = new MyCollections(list);
        Assert.assertEquals(2, before.getList().size());
        Assert.assertEquals(0, before.getList().get(0).size());
        Assert.assertEquals(1, before.getList().get(1).size());
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof MyCollections);
        after = (MyCollections) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(2, after.getList().size());
        Assert.assertEquals(0, after.getList().get(0).size());
        Assert.assertEquals(1, after.getList().get(1).size());

        // List with 1 element which is set with 2 elements.
        list = new ArrayList<>();
        final LinkedHashSet<Integer> set2 = new LinkedHashSet<>();
        set2.add(2);
        set2.add(3);
        list.add(set0);
        list.add(set1);
        list.add(set2);
        before = new MyCollections(list);
        Assert.assertEquals(3, before.getList().size());
        Assert.assertEquals(0, before.getList().get(0).size());
        Assert.assertEquals(1, before.getList().get(1).size());
        Assert.assertEquals(2, before.getList().get(2).size());

        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof MyCollections);
        after = (MyCollections) deserialized;
        Assert.assertNotNull(after);
        Assert.assertEquals(3, after.getList().size());
        Assert.assertEquals(0, after.getList().get(0).size());
        Assert.assertEquals(1, after.getList().get(1).size());
        Assert.assertEquals(2, after.getList().get(2).size());
    }

    // ------------------------------------------------------------------------
    // Test types as property in a class.
    // ------------------------------------------------------------------------

    @SuppressWarnings("rawtypes")
    private static class MyProperties {
        private final char charValue;
        @Nonnull
        private final String stringValue;
        private final boolean booleanValue;
        private final int intValue;
        private final long longValue;
        private final short shortValue;
        private final byte byteValue;
        private final float floatValue;
        private final double doubleValue;
        @Nonnull
        private final DateTime dateTimeValue;
        @Nonnull
        private final Currency currencyValue;
        @Nonnull
        private final Uid uidValue;
        @Nonnull
        private final List<Integer> listValue;
        @Nonnull
        private final Set<Float> setValue;
        @Nonnull
        private final List<Set<String>> listSetValue;

        public MyProperties(
                final char charValue,
                @Nonnull final String stringValue,
                final boolean booleanValue,
                final int intValue,
                final long longValue,
                final short shortValue,
                final byte byteValue,
                final float floatValue,
                final double doubleValue,
                @Nonnull final DateTime dateTimeValue,
                @Nonnull final Currency currencyValue,
                @Nonnull final Uid<?> uidValue,
                @Nonnull final List<Integer> listValue,
                @Nonnull final Set<Float> setValue,
                @Nonnull final List<Set<String>> listSetValue) {
            this.charValue = charValue;
            this.stringValue = stringValue;
            this.booleanValue = booleanValue;
            this.intValue = intValue;
            this.longValue = longValue;
            this.shortValue = shortValue;
            this.byteValue = byteValue;
            this.floatValue = floatValue;
            this.doubleValue = doubleValue;
            this.dateTimeValue = dateTimeValue;
            this.currencyValue = currencyValue;
            this.uidValue = uidValue;
            this.listValue = listValue;
            this.setValue = setValue;
            this.listSetValue = listSetValue;
        }

        public char getCharValue() {
            return charValue;
        }

        @Nonnull
        public String getStringValue() {
            return stringValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public short getShortValue() {
            return shortValue;
        }

        public byte getByteValue() {
            return byteValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        @Nonnull
        public DateTime getDateTimeValue() {
            return dateTimeValue;
        }

        @Nonnull
        public Currency getCurrencyValue() {
            return currencyValue;
        }

        @Nonnull
        public Uid<?> getUidValue() {
            return uidValue;
        }

        @Nonnull
        public List<Integer> getListValue() {
            return listValue;
        }

        @Nonnull
        public Set<Float> getSetValue() {
            return setValue;
        }

        @Nonnull
        public List<Set<String>> getListSetValue() {
            return listSetValue;
        }
    }

    @Test
    public void testPropertiesInClass() {
        LOG.info("testPropertiesInClass");

        // Empty list in properties.
        final DateTime now = UTCTime.now();
        final Currency eur = Currency.getInstance("EUR");
        final Uid<?> uid = new Uid("1-2-3-4-5");

        List<Integer> listValue = new ArrayList<>();
        Set<Float> setValue = new LinkedHashSet<>();
        List<Set<String>> listSetValue = new ArrayList<>();
        MyProperties before = new MyProperties(
                'A', "", false, 1, 2L, (short) 3, (byte) 4, 5.6f, 7.8, now, eur,
                uid, listValue, setValue, listSetValue);
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        Object serialized = serializer.serialize(before);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof MyProperties);
        MyProperties after = (MyProperties) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));
        Assert.assertEquals('A', after.getCharValue());
        Assert.assertEquals("", after.getStringValue());
        Assert.assertEquals(1, after.getIntValue());
        Assert.assertEquals(2L, after.getLongValue());
        Assert.assertEquals((short) 3, after.getShortValue());
        Assert.assertEquals((byte) 4, after.getByteValue());
        Assert.assertTrue(MathUtils.isAlmostEqual(5.6f, after.getFloatValue()));
        Assert.assertTrue(MathUtils.isAlmostEqual(7.8, after.getDoubleValue()));
        Assert.assertEquals(now, after.getDateTimeValue());
        Assert.assertEquals(eur.getCurrencyCode(), after.getCurrencyValue().getCurrencyCode());
        Assert.assertEquals(uid, after.getUidValue());
        Assert.assertEquals(listValue, after.getListValue());
        Assert.assertEquals(setValue, after.getSetValue());
        Assert.assertEquals(listSetValue, after.getListSetValue());
        Assert.assertEquals(0, after.getListValue().size());
        Assert.assertEquals(0, after.getSetValue().size());
        Assert.assertEquals(0, after.getListSetValue().size());

        // Non-empty list in properties.
        listValue = new ArrayList<>();
        listValue.add(123);
        setValue = new LinkedHashSet<>();
        setValue.add(1.1f);
        setValue.add(2.2f);
        //noinspection MismatchedQueryAndUpdateOfCollection
        final Set<String> set1 = new LinkedHashSet<>();
        final Set<String> set2 = new LinkedHashSet<>();
        set2.add("S");
        set2.add("PQR");
        listSetValue = new ArrayList<>();
        listSetValue.add(set1);
        listSetValue.add(set2);
        before = new MyProperties(
                'A', "XYZ", false, 1, 2L, (short) 3, (byte) 4, 5.6f, 7.8, now, eur,
                uid, listValue, setValue, listSetValue);
        LOG.debug("JSON:\n{}", serializer.toStringPretty(before, true));
        serialized = serializer.serialize(before);
        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof MyProperties);
        after = (MyProperties) deserialized;
        Assert.assertEquals(Json.toStringJson(before), Json.toStringJson(after));
        Assert.assertEquals('A', after.getCharValue());
        Assert.assertEquals("XYZ", after.getStringValue());
        Assert.assertEquals(1, after.getIntValue());
        Assert.assertEquals(2L, after.getLongValue());
        Assert.assertEquals((short) 3, after.getShortValue());
        Assert.assertEquals((byte) 4, after.getByteValue());
        Assert.assertTrue(MathUtils.isAlmostEqual(5.6f, after.getFloatValue()));
        Assert.assertTrue(MathUtils.isAlmostEqual(7.8, after.getDoubleValue()));
        Assert.assertEquals(now, after.getDateTimeValue());
        Assert.assertEquals(eur.getCurrencyCode(), after.getCurrencyValue().getCurrencyCode());
        Assert.assertEquals(uid, after.getUidValue());
        Assert.assertEquals(listValue, after.getListValue());
        Assert.assertEquals(1, after.getListValue().size());
        Assert.assertEquals(123, (int) after.getListValue().get(0));
        Assert.assertEquals(setValue, after.getSetValue());
        Assert.assertEquals(2, after.getListSetValue().size());
        Assert.assertTrue(after.getSetValue().contains(1.1f));
        Assert.assertTrue(after.getSetValue().contains(2.2f));
        Assert.assertEquals(listSetValue, after.getListSetValue());
        Assert.assertEquals(2, after.getListSetValue().size());
        Assert.assertEquals(0, after.getListSetValue().get(0).size());
        Assert.assertEquals(2, after.getListSetValue().get(1).size());
        Assert.assertTrue(after.getListSetValue().get(1).contains("S"));
        Assert.assertTrue(after.getListSetValue().get(1).contains("PQR"));
    }

    // ------------------------------------------------------------------------
    // Test cyclic example with external class.
    // ------------------------------------------------------------------------

    @Test
    public void testCyclesExternalClass() {
        LOG.info("testCyclesExternalClass");

        // Not cyclic.
        Cycle before = new Cycle(100);
        Object serialized = serializer.serialize(before);
        LOG.info("c (pretty):\n{}", serializer.toStringPretty(before, true));
        LOG.info("c:\n{}", serialized);

        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Cycle);
        Cycle after = (Cycle) deserialized;
        Assert.assertEquals(100, after.i);
        Assert.assertNull(after.c);

        // Not cyclic, ref to same object type however.
        before = new Cycle(200);
        Cycle other = new Cycle(300);
        before.c = other;
        serialized = serializer.serialize(before);
        LOG.info("c (pretty):\n{}", serializer.toStringPretty(before, true));
        LOG.info("c:\n{}", serialized);

        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Cycle);
        after = (Cycle) deserialized;
        Assert.assertEquals(200, after.i);
        Cycle afterC = after.c;
        Assert.assertNotNull(afterC);
        Assert.assertEquals(300, afterC.i);
        Assert.assertNull(afterC.c);

        // Full cycle between c and d.
        before = new Cycle(2000);
        other = new Cycle(3000);
        before.c = other;
        other.c = before;
        serialized = serializer.serialize(before);
        LOG.info("c (pretty):\n{}", serializer.toStringPretty(before, true));
        LOG.info("c:\n{}", serialized);

        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Cycle);
        after = (Cycle) deserialized;
        Assert.assertEquals(2000, after.i);
        afterC = after.c;
        Assert.assertNotNull(afterC);
        Assert.assertEquals(3000, afterC.i);

        /**
         * This cycle cannot be resolved by the deserializer yet, so it will cut
         * the cycle off here.
         */
        Assert.assertNull(afterC.c);
    }

    // ------------------------------------------------------------------------
    // Test cyclic example with internal class.
    // ------------------------------------------------------------------------

    public static class MyClassX {
        public int a;
        @Nullable
        public MyClassY y;

        public MyClassX(final int a) {
            this.y = null;
            this.a = a;
        }

        // The not-null for y forces the serializer to use the other constructor if y == null!
        @SuppressWarnings("NullableProblems")
        public MyClassX(final int a, @Nonnull final MyClassY y) {
            assert y != null;
            this.y = y;
            this.a = a;
        }

        @Nullable
        public MyClassY getY() {
            return y;
        }

        public int getA() {
            return a;
        }
    }

    public static class MyClassY {
        public int b;
        @Nullable
        public MyClassX x;

        public MyClassY(final int b) {
            this.x = null;
            this.b = b;
        }

        // The not-null for x forces the serializer to use the other constructor if x == null!
        @SuppressWarnings("NullableProblems")
        public MyClassY(final int b, @Nonnull final MyClassX x) {
            assert x != null;
            this.x = x;
            this.b = b;
        }

        @Nullable
        public MyClassX getX() {
            return x;
        }

        public int getB() {
            return b;
        }
    }

    @Test
    public void testCyclesInnerClass() {
        LOG.info("testCyclesInnerClass");

        // Not cyclic, but class X contains a null-reference to Y, but the constructor does not allow that.
        MyClassX xBefore = new MyClassX(100);
        MyClassY yBefore = new MyClassY(200, xBefore);

        Object serializedX = serializer.serialize(xBefore);
        Object serializedY = serializer.serialize(yBefore);
        LOG.info("x (pretty):\n{}", serializer.toStringPretty(xBefore, true));
        LOG.info("y (pretty):\n{}", serializer.toStringPretty(yBefore, true));
        LOG.info("x:\n{}", serializedX);
        LOG.info("y:\n{}", serializedY);

        Object deserializedX = serializer.deserialize(serializedX);
        Assert.assertTrue(deserializedX instanceof MyClassX);
        MyClassX xAfter = (MyClassX) deserializedX;
        Assert.assertEquals(100, xAfter.a);
        Assert.assertNull(xAfter.y);

        Object deserializedY = serializer.deserialize(serializedY);
        Assert.assertTrue(deserializedY instanceof MyClassY);
        MyClassY yAfter = (MyClassY) deserializedY;
        Assert.assertEquals(200, yAfter.b);
        MyClassX yAfterX = yAfter.x;
        Assert.assertNotNull(yAfterX);
        Assert.assertEquals(100, yAfterX.a);
        Assert.assertNull(yAfterX.y);

        // Not cyclic, but class X references an instance of class Y, and Y references X.
        xBefore = new MyClassX(10, new MyClassY(11));
        yBefore = new MyClassY(20, xBefore);

        serializedX = serializer.serialize(xBefore);
        serializedY = serializer.serialize(yBefore);
        LOG.info("x (pretty):\n{}", serializer.toStringPretty(xBefore, true));
        LOG.info("y (pretty):\n{}", serializer.toStringPretty(yBefore, true));
        LOG.info("x:\n{}", serializedX);
        LOG.info("y:\n{}", serializedY);

        deserializedX = serializer.deserialize(serializedX);
        Assert.assertTrue(deserializedX instanceof MyClassX);
        xAfter = (MyClassX) deserializedX;
        Assert.assertEquals(10, xAfter.a);
        Assert.assertNotNull(xAfter.y);
        MyClassY xAfterY = xAfter.y;
        Assert.assertNotNull(xAfterY);
        Assert.assertEquals(11, xAfterY.b);

        deserializedY = serializer.deserialize(serializedY);
        Assert.assertTrue(deserializedY instanceof MyClassY);
        yAfter = (MyClassY) deserializedY;
        Assert.assertEquals(20, yAfter.b);
        yAfterX = yAfter.x;
        Assert.assertNotNull(yAfterX);
        Assert.assertEquals(10, yAfterX.a);
        final MyClassY yAfterXY = yAfterX.y;
        Assert.assertNotNull(yAfterXY);
        Assert.assertEquals(11, yAfterXY.b);

        // Cyclic, class X references Y and Y references X.
        xBefore = new MyClassX(1000);
        yBefore = new MyClassY(2000, xBefore);

        // Make it cyclic now, after constructing Y:
        xBefore.y = yBefore;

        serializedX = serializer.serialize(xBefore);
        serializedY = serializer.serialize(yBefore);
        LOG.info("x (pretty):\n{}", serializer.toStringPretty(xBefore, true));
        LOG.info("y (pretty):\n{}", serializer.toStringPretty(yBefore, true));
        LOG.info("x:\n{}", serializedX);
        LOG.info("y:\n{}", serializedY);

        deserializedX = serializer.deserialize(serializedX);
        Assert.assertTrue(deserializedX instanceof MyClassX);
        xAfter = (MyClassX) deserializedX;
        Assert.assertEquals(1000, xAfter.a);
        xAfterY = xAfter.y;
        Assert.assertNotNull(xAfterY);
        Assert.assertEquals(2000, xAfterY.b);

        deserializedY = serializer.deserialize(serializedY);
        Assert.assertTrue(deserializedY instanceof MyClassY);
        yAfter = (MyClassY) deserializedY;
        Assert.assertEquals(2000, yAfter.b);
        yAfterX = yAfter.x;
        Assert.assertNotNull(yAfterX);
        Assert.assertEquals(1000, yAfterX.a);
    }

    // ------------------------------------------------------------------------
    // Test complex example, including cyclic data structures.
    // ------------------------------------------------------------------------

    public static class Person {
        @Nonnull
        protected final String name;
        @Nonnull
        protected final DateTime birthDate;
        @Nullable
        protected final Woman mother;
        @Nonnull
        protected final Gender gender;
        @Nonnull
        protected final List<Person> friends;
        @Nonnull
        protected final Object[] things;

        public Person(
                @Nonnull final String name,
                @Nonnull final DateTime birthDate,
                @Nullable final Woman mother,
                @Nonnull final Gender gender,
                @Nonnull final List<Person> friends,
                @Nonnull final Object[] things) {
            this.name = name;
            this.birthDate = birthDate;
            this.mother = mother;
            this.gender = gender;
            this.friends = friends;
            this.things = things;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public DateTime getBirthDate() {
            return birthDate;
        }

        @Nullable
        public Woman getMother() {
            return mother;
        }

        @Nonnull
        public Gender getGender() {
            return gender;
        }

        @Nonnull
        public List<Person> getFriends() {
            return friends;
        }

        @Nonnull
        public Object[] getThings() {
            return things;
        }
    }

    public static class Woman extends Person {
        protected final boolean prefersDresses;
        protected final List<Set<Person>> favorites;

        public Woman(
                @Nonnull final String name,
                @Nonnull final DateTime birthDate,
                @Nullable final Woman mother,
                @Nonnull final Gender gender,
                @Nonnull final List<Person> friends,
                @Nonnull final Object[] things,
                final boolean prefersDresses,
                @Nonnull final List<Set<Person>> favorites) {
            super(name, birthDate, mother, gender, friends, things);
            this.prefersDresses = prefersDresses;
            this.favorites = favorites;
        }

        @Nonnull
        public Woman withAdditionalFavorites(@Nonnull final Set<Person> persons) {
            final List<Set<Person>> newFavorites = new ArrayList<>();
            newFavorites.addAll(favorites);
            newFavorites.add(persons);
            return new Woman(name, birthDate, mother, gender, friends, things,
                    prefersDresses, newFavorites);
        }

        public boolean getPrefersDresses() {
            return prefersDresses;
        }

        @Nonnull
        public List<Set<Person>> getFavorites() {
            return favorites;
        }
    }

    public static enum Gender {
        MALE,
        FEMALE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @Test
    public void testComplex() {
        LOG.info("testComplex");

        final DateTime now = UTCTime.now();
        final Woman jane = new Woman("jane", now, null, Gender.FEMALE, Collections.<Person>emptyList(), new Object[0],
                true, Collections.<Set<Person>>emptyList());
        Object serialized = serializer.serialize(jane);
        Object deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Woman);
        Assert.assertEquals(Json.toStringJson(jane), Json.toStringJson(deserialized));

        final Person joe = new Person("joe", now, jane, Gender.MALE, Collections.<Person>emptyList(), new Object[0]);
        final Person jim = new Person("jim", now, jane, Gender.MALE, Collections.<Person>emptyList(), new Object[0]);

        final String someString = "A String With a Backspace \b, a newline \n and ASCII(30) \036";
        final List<Set<Person>> favorites = Collections.singletonList(Immutables.setOf(jane, joe));
        final List<Person> friends = Immutables.listOf(joe, jim);
        final Object[] things = {someString, joe};
        Woman maryBefore = new Woman("mary", now, jane, Gender.FEMALE, friends, things, false, favorites);
        final HashSet<Person> persons = new HashSet<>();
        persons.add(maryBefore);
        maryBefore = maryBefore.withAdditionalFavorites(persons);
        LOG.debug("JSON:\n{}", serializer.toStringPretty(maryBefore, true));

        serialized = serializer.serialize(maryBefore);
        Assert.assertNotNull(serialized);
        LOG.debug("Serialized (Jackson)              : " + Json.toStringJson(maryBefore));
        LOG.debug("Serialized (SimpleJsonSerializer) : " + serializer.toString(maryBefore));

        deserialized = serializer.deserialize(serialized);
        Assert.assertTrue(deserialized instanceof Woman);
        LOG.debug("Deserialized (Jackson)              : " + Json.toStringJson(deserialized));
        LOG.debug("Deserialized (SimpleJsonSerializer) : " + serializer.toString(deserialized));

        final Woman maryAfter = (Woman) deserialized;
        Assert.assertEquals(now, maryAfter.getBirthDate());
        Assert.assertEquals(Json.toStringJson(jane), Json.toStringJson(maryAfter.getMother()));
        Assert.assertEquals(Gender.FEMALE, maryAfter.getGender());
        Assert.assertEquals(Json.toStringJson(friends), Json.toStringJson(maryAfter.getFriends()));
        Assert.assertEquals(Json.toStringJson(things), Json.toStringJson(maryAfter.getThings()));

        Assert.assertNotNull(maryAfter.getFavorites());
        Assert.assertEquals(2, maryAfter.getFavorites().size());
        Assert.assertEquals(2, maryAfter.getFavorites().get(0).size());
        final Woman mother = maryAfter.getMother();
        Assert.assertNotNull(mother);
        Assert.assertEquals(0, mother.getFavorites().size());
        Assert.assertSame(Woman.class, maryAfter.getFavorites().get(0).iterator().next().getClass());
        Assert.assertNotNull(maryAfter.getThings());
        Assert.assertEquals(2, maryAfter.getThings().length);
        Assert.assertEquals(someString, maryAfter.getThings()[0]);
        Assert.assertSame(Person.class, maryAfter.getThings()[1].getClass());
    }
}
