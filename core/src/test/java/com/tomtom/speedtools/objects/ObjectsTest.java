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

package com.tomtom.speedtools.objects;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ObjectsTest {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectsTest.class);

    @Test
    public void testEqual() {
        LOG.info("testEqual");
        final String s0 = "";
        final String s1 = "abc";
        final String s2 = "abc";
        final String s3 = "Abc";
        Assert.assertTrue(Objects.equal(null, null));
        Assert.assertFalse(Objects.equal(s0, null));
        Assert.assertFalse(Objects.equal(null, s0));
        Assert.assertFalse(Objects.equal(s1, null));
        Assert.assertFalse(Objects.equal(null, s1));
        Assert.assertTrue(Objects.equal(s1, s1));
        Assert.assertTrue(Objects.equal(s1, s2));
        Assert.assertFalse(Objects.equal(s1, s3));
    }

    @SuppressWarnings({"RedundantArrayCreation", "PrimitiveArrayArgumentToVariableArgMethod"})
    @Test
    public void testHashCode() {
        LOG.info("testHashCode");
        final String s0 = "";
        final String s1 = "abc";
        final String s2 = "abc";
        final String s3 = "Abc";
        Assert.assertTrue(Objects.hashCode((String) null) == Objects.hashCode((String) null));
        Assert.assertTrue(Objects.hashCode(s0) == Objects.hashCode(""));
        Assert.assertTrue(Objects.hashCode(s1) == Objects.hashCode(s2));
        Assert.assertFalse(Objects.hashCode(s1) == Objects.hashCode(s3));

        final double d0 = 0.0;
        final double d1 = 1.0;
        Assert.assertTrue(Objects.hashCode((double[]) null) == Objects.hashCode((double[]) null));
        Assert.assertTrue(Objects.hashCode(new double[]{d0}) == Objects.hashCode(new double[]{0.0}));
        Assert.assertTrue(Objects.hashCode(new double[]{d0}) == Objects.hashCode(new double[]{d0}));
        Assert.assertFalse(Objects.hashCode(new double[]{d0}) == Objects.hashCode(new double[]{d1}));
        Assert.assertTrue(Objects.hashCode(d0) == Objects.hashCode(0.0));

        final ArrayList<String> a1 = new ArrayList<>();
        a1.add("1");
        a1.add("2");
        final ArrayList<String> a2 = new ArrayList<>();
        a2.add("1");
        a2.add("2");
        final ArrayList<String> a3 = new ArrayList<>();
        a3.add("a");
        a3.add("b");
        Assert.assertTrue(Objects.hashCode((Object) null) == Objects.hashCode((Object) null));
        Assert.assertTrue(Objects.hashCode(a1) == Objects.hashCode(a1));
        Assert.assertTrue(Objects.hashCode(a1) == Objects.hashCode(a2));
        Assert.assertFalse(Objects.hashCode(a1) == Objects.hashCode(a3));
    }
}
