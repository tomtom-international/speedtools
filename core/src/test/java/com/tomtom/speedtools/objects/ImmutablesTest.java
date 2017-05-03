/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.speedtools.objects;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ImmutablesTest {
    private static final Logger LOG = LoggerFactory.getLogger(ImmutablesTest.class);

    @Test
    public void testEmptyList() {
        LOG.info("testEmptyList");
        final Collection<Integer> a = Immutables.emptyList();
        Assert.assertTrue("Array " + a + " should be empty", a.isEmpty());
    }

    @Test
    public void testListOfSingle() {
        LOG.info("testListOfSingle");

        final List<Set<Integer>> x = Immutables.listOfSingle(Immutables.setOf(1));

        Assert.assertTrue("Collection should contain 1 set with 1 in it", x.get(0).iterator().next().equals(1));
    }

    @Test
    public void testListOf() {
        LOG.info("testListOf");
        final Collection<Integer> x = Immutables.listOf(1);
        Assert.assertTrue("Array " + x + " should contain 1", x.contains(1));

        final Collection<Integer> a = Immutables.listOf(1, 2);
        Assert.assertTrue("Array " + a + " should contain 1", a.contains(1));
        Assert.assertTrue("Array " + a + " should contain 2", a.contains(2));

        @SuppressWarnings("unchecked")
        final Collection<Integer> b = Immutables.listOf();
        Assert.assertTrue("Array " + b + " should be empty", b.isEmpty());

        final Collection<Integer> c = Immutables.listOf(a, 3, 4);
        final Collection<Integer> d = Immutables.listOf(1, 2, 3, 4);
        final Collection<Integer> e = Immutables.listOf(2, 1, 3, 4);
        Assert.assertTrue("Array " + c + " should equal " + d, d.equals(c));
        Assert.assertFalse("Array " + c + " should not equal " + e, e.equals(c));

        final Collection<Integer> u0 = Immutables.listOf(Immutables.<Integer>emptyList());
        @SuppressWarnings("unchecked")
        final Collection<Integer> v0 = Immutables.listOf();
        Assert.assertTrue("Array " + u0 + " should equal " + v0, u0.equals(v0));

        final ArrayList<Integer> z1 = new ArrayList<>();
        final ArrayList<Integer> z2 = new ArrayList<>();
        z2.add(1);

        final LinkedHashSet<Integer> zz1 = new LinkedHashSet<>();
        final LinkedHashSet<Integer> zz2 = new LinkedHashSet<>();
        zz2.add(1);

        final Collection<Integer> u1 = Immutables.listOf(Immutables.<Integer>emptyList(), 1);
        final Collection<Integer> v1 = Immutables.listOf(Immutables.listOf(1));
        final Collection<Integer> uu1 = Immutables.listOf(z1);
        final Collection<Integer> uuu1 = Immutables.listOf(z2);
        final Collection<Integer> vv1 = Immutables.listOf(zz1);
        final Collection<Integer> vvv1 = Immutables.listOf(zz2);
        final Collection<Integer> w1 = Immutables.listOf(1);
        Assert.assertTrue("Array " + u1 + " should equal " + w1, u1.equals(w1));
        Assert.assertTrue("Array " + v1 + " should equal " + w1, v1.equals(w1));
        Assert.assertTrue("Array " + uu1 + " should be empty", uu1.isEmpty());
        Assert.assertTrue("Array " + uuu1 + " should equal w1" + w1, uuu1.equals(w1));
        Assert.assertTrue("Array " + vv1 + " should be empty", vv1.isEmpty());
        Assert.assertTrue("Array " + vvv1 + " should equal w1" + w1, vvv1.equals(w1));

        final Collection<Integer> u2 = Immutables.listOf(Immutables.<Integer>emptyList(), 1, 2);
        final Collection<Integer> v2 = Immutables.listOf(1, 2);
        Assert.assertTrue("Array " + u2 + " should equal " + v2, u2.equals(v2));
    }

    @Test
    public void testCopyOf() {
        LOG.info("testCopyOf");
        final Collection<Integer> x = Immutables.listOf(1);
        Assert.assertTrue("Array " + x + " should contain 1", x.contains(1));

        final Collection<Integer> x2 = Immutables.copyOf(x);
        Assert.assertTrue("Array " + x2 + " should equal ", x2.equals(x));

        final Collection<Integer> a = Immutables.copyOf(x, 2);
        Assert.assertTrue("Array " + a + " should contain 1", a.contains(1));
        Assert.assertTrue("Array " + a + " should contain 2", a.contains(2));

        @SuppressWarnings("unchecked")
        final Set<Integer> b = new LinkedHashSet<>();
        final Collection<Integer> b1 = Immutables.copyOf(b);
        Assert.assertTrue("Array " + b1 + " should be empty", b1.isEmpty());

        final Collection<Integer> c = Immutables.copyOf(a, 3, 4);
        final Collection<Integer> d = Immutables.copyOf(b, 1, 2, 3, 4);
        final Collection<Integer> e = Immutables.copyOf(b, 2, 1, 3, 4);
        Assert.assertTrue("Array " + c + " should equal " + d, d.equals(c));
        Assert.assertFalse("Array " + c + " should not equal " + e, e.equals(c));

        final Collection<Integer> u0 = Immutables.copyOf(Immutables.<Integer>emptyList());
        @SuppressWarnings("unchecked")
        final Collection<Integer> v0 = Immutables.copyOf(new ArrayList<>());
        Assert.assertTrue("Array " + u0 + " should equal " + v0, u0.equals(v0));
    }

    @Test
    public void testEmptySet() {
        LOG.info("testEmptySet");
        final Collection<Integer> a = Immutables.emptySet();
        Assert.assertTrue("Set " + a + " should be empty", a.isEmpty());
    }

    @Test
    public void testSetOf() {
        LOG.info("testSetOf");
        final Collection<Integer> x = Immutables.setOf(1);
        Assert.assertTrue("Set " + x + " should contain 1", x.contains(1));

        final Collection<Integer> a = Immutables.setOf(1, 2);
        Assert.assertTrue("Set " + a + " should contain 1", a.contains(1));
        Assert.assertTrue("Set " + a + " should contain 2", a.contains(2));

        @SuppressWarnings("unchecked")
        final Collection<Integer> b = Immutables.setOf();
        Assert.assertTrue("Set " + b + " should be empty", b.isEmpty());

        final Collection<Integer> c = Immutables.setOf(a, 3, 4);
        final Collection<Integer> d = Immutables.setOf(4, 2, 1, 3);
        Assert.assertTrue("Set " + c + " should equal " + d, d.equals(c));

        final Collection<Integer> u0 = Immutables.setOf(Immutables.<Integer>emptyList());
        @SuppressWarnings("unchecked")
        final Collection<Integer> v0 = Immutables.setOf();
        Assert.assertTrue("Set " + u0 + " should equal " + v0, u0.equals(v0));

        final ArrayList<Integer> z1 = new ArrayList<>();
        final ArrayList<Integer> z2 = new ArrayList<>();
        z2.add(1);

        final LinkedHashSet<Integer> zz1 = new LinkedHashSet<>();
        final LinkedHashSet<Integer> zz2 = new LinkedHashSet<>();
        zz2.add(1);

        final Collection<Integer> u1 = Immutables.setOf(Immutables.<Integer>emptyList(), 1);
        final Collection<Integer> v1 = Immutables.setOf(Immutables.setOf(1));
        final Collection<Integer> uu1 = Immutables.setOf(z1);
        final Collection<Integer> uuu1 = Immutables.setOf(z2);
        final Collection<Integer> vv1 = Immutables.setOf(zz1);
        final Collection<Integer> vvv1 = Immutables.setOf(zz2);
        final Collection<Integer> w1 = Immutables.setOf(1);
        Assert.assertTrue("Set " + u1 + " should equal " + w1, u1.equals(w1));
        Assert.assertTrue("Set " + v1 + " should equal " + w1, v1.equals(w1));
        Assert.assertTrue("Array " + uu1 + " should be empty", uu1.isEmpty());
        Assert.assertTrue("Array " + uuu1 + " should equal w1" + w1, uuu1.equals(w1));
        Assert.assertTrue("Array " + vv1 + " should be empty", vv1.isEmpty());
        Assert.assertTrue("Array " + vvv1 + " should equal w1" + w1, vvv1.equals(w1));

        final Collection<Integer> u2 = Immutables.setOf(Immutables.<Integer>emptyList(), 1, 2);
        final Collection<Integer> v2 = Immutables.setOf(1, 2);
        Assert.assertTrue("Set " + u2 + " should equal " + v2, u2.equals(v2));
    }
}
