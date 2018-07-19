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

package com.tomtom.speedtools.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MathUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(MathUtilsTest.class);

    @Test
    public void testFindMin() {
        LOG.info("testFindMin");
        final double[] a1 = {1, 2, 3};
        final double[] a2 = {3, 1, 2};
        final double[] a3 = {2, 3, 1};
        Assert.assertTrue("Min should be 1", Double.compare(MathUtils.findMin(a1), 1.0) == 0);
        Assert.assertTrue("Min should be 1", Double.compare(MathUtils.findMin(a2), 1.0) == 0);
        Assert.assertTrue("Min should be 1", Double.compare(MathUtils.findMin(a3), 1.0) == 0);

        final double[] b1 = {-1, 2, 3};
        final double[] b2 = {3, -1, 2};
        final double[] b3 = {2, 3, -1};
        Assert.assertTrue("Min should be -1", Double.compare(MathUtils.findMin(b1), -1.0) == 0);
        Assert.assertTrue("Min should be -1", Double.compare(MathUtils.findMin(b2), -1.0) == 0);
        Assert.assertTrue("Min should be -1", Double.compare(MathUtils.findMin(b3), -1.0) == 0);
    }

    @Test
    public void testFindMax() {
        LOG.info("testFindMax");
        final double[] a1 = {1, 2, 3};
        final double[] a2 = {3, 1, 2};
        final double[] a3 = {2, 3, 1};
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(a1), 3.0) == 0);
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(a2), 3.0) == 0);
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(a3), 3.0) == 0);

        final double[] b1 = {-1, 2, 3};
        final double[] b2 = {3, -1, 2};
        final double[] b3 = {2, 3, -1};
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(b1), 3.0) == 0);
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(b2), 3.0) == 0);
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(b3), 3.0) == 0);
    }

    @Test
    public void testRandom() {
        LOG.info("testRandom");
        MathUtils.setRandomSeed(12121212);
        final int min = 10;
        final int max = 20;
        Assert.assertEquals(11, MathUtils.random(min, max));
        Assert.assertEquals(16, MathUtils.random(min, max));
        for (int i = 0; i < 1000; ++i) {
            final int r = MathUtils.random(min, max);
            Assert.assertTrue((min <= r) && (r <= max));
        }

        final double[] b1 = {-1, 2, 3};
        final double[] b2 = {3, -1, 2};
        final double[] b3 = {2, 3, -1};
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(b1), 3.0) == 0);
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(b2), 3.0) == 0);
        Assert.assertTrue("Max should be 3", Double.compare(MathUtils.findMax(b3), 3.0) == 0);
    }

    @Test
    public void testIsAlmostZero() {
        LOG.info("testIsAlmostZero");
        Assert.assertTrue(MathUtils.isAlmostZero(0));
        Assert.assertFalse(MathUtils.isAlmostZero(0.01));
        Assert.assertFalse(MathUtils.isAlmostZero(-0.01));
        Assert.assertTrue(MathUtils.isAlmostZero(MathUtils.EPSILON / 2));
        Assert.assertTrue(MathUtils.isAlmostZero(-MathUtils.EPSILON / 2));
        Assert.assertFalse(MathUtils.isAlmostZero(MathUtils.EPSILON * 2));
        Assert.assertFalse(MathUtils.isAlmostZero(-MathUtils.EPSILON * 2));
    }

    @Test
    public void testWrapValueToWithinLimits() {
        LOG.info("testWrapValueToWithinLimits");
        Assert.assertEquals(-1, MathUtils.wrapValueToWithinLimits(1, 1), 1.0e-8);
        Assert.assertEquals(0, MathUtils.wrapValueToWithinLimits(0, 180), 1.0e-8);
        Assert.assertEquals(0, MathUtils.wrapValueToWithinLimits(360.0, 180), 1.0e-8);
        Assert.assertEquals(1, MathUtils.wrapValueToWithinLimits(361.0, 180), 1.0e-8);
        Assert.assertEquals(-1, MathUtils.wrapValueToWithinLimits(359.0, 180), 1.0e-8);
        Assert.assertEquals(179, MathUtils.wrapValueToWithinLimits(179.0, 180), 1.0e-8);
        Assert.assertEquals(-179, MathUtils.wrapValueToWithinLimits(181.0, 180), 1.0e-8);
    }
}
