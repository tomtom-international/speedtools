/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Important:</strong> See {@link com.tomtom.speedtools.testutils.TestUtils} for an explanation of the unit test code
 * structure.
 */
@SuppressWarnings("FieldMayBeStatic")
public class ColorRGB8Test {
    private static final Logger LOG = LoggerFactory.getLogger(ColorRGB8Test.class);

    private final int x1 = 0;
    private final int x2 = 0;
    private final int x3 = 0;
    private final ColorRGB8 x = new ColorRGB8(x1, x2, x3);

    private int y1;
    private int y2;
    private int y3;
    private ColorRGB8 y = null;

    @Before
    public void setUp() {
        y1 = 1;
        y2 = 2;
        y3 = 3;
        y = new ColorRGB8(y1, y2, y3);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(ColorRGB8.class).
                verify();
    }

    @Test
    public void testEquals() {
        LOG.info("testEquals");
        final ColorRGB8 s = y.withR(x1).withG(x2).withB(x3);
        final ColorRGB8 t = x.withR(y1).withG(y2).withB(y3);
        Assert.assertTrue(x.equals(s));
        Assert.assertTrue(y.equals(t));
        Assert.assertFalse(x.equals(t));
        Assert.assertFalse(y.equals(s));
    }

    @Test
    public void testWithR() {
        LOG.info("testWithR");
        Assert.assertTrue(Double.compare(y1, x.withR(y1).getR()) == 0);
        Assert.assertTrue(Double.compare(x1, y.withR(x1).getR()) == 0);
    }

    @Test
    public void testWithG() {
        LOG.info("testWithG");
        Assert.assertTrue(Double.compare(y2, x.withG(y2).getG()) == 0);
        Assert.assertTrue(Double.compare(x2, y.withG(x2).getG()) == 0);
    }

    @Test
    public void testWithB() {
        LOG.info("testWithB");
        Assert.assertTrue(Double.compare(y3, x.withB(y3).getB()) == 0);
        Assert.assertTrue(Double.compare(x3, y.withB(x3).getB()) == 0);
    }

    @Test
    public void testInteger() {
        LOG.info("testInteger");
        final int i1 = 0x00010203;
        final ColorRGB8 c1 = new ColorRGB8(i1);
        final ColorRGB8 c2 = new ColorRGB8(1, 2, 3);
        Assert.assertEquals(c1, c2);
        Assert.assertEquals((Integer) i1, c2.toIntegerRGB());
    }
}
