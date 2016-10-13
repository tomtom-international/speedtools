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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import com.tomtom.speedtools.utils.MathUtils;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoLineTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoLineTest.class);

    private final GeoPoint x1 = new GeoPoint(1.0, 2.0);
    private final GeoPoint x2 = new GeoPoint(3.0, 4.0);
    private final GeoLine x = new GeoLine(x1, x2);

    private GeoPoint y1 = null;
    private GeoPoint y2 = null;
    private GeoLine y = null;

    @Before
    public void setUp() {
        y1 = new GeoPoint(-3.0, -4.0);
        y2 = new GeoPoint(5.0, 6.0);
        y = new GeoLine(y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GeoLine.class).
                withRedefinedSuperclass().
                verify();
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(GeoLine.class, new int[]{}, y1, y2);
    }

    @Test
    public void testWithSouthWest() {
        LOG.info("testWithSouthWest");
        Assert.assertTrue(y1.equals(x.withSouthWest(y1).getSouthWest()));
        Assert.assertTrue(x1.equals(y.withSouthWest(x1).getSouthWest()));
    }

    @Test
    public void testWithNorthEast() {
        LOG.info("testWithNorthEast");
        Assert.assertTrue(y2.equals(x.withNorthEast(y2).getNorthEast()));
        Assert.assertTrue(x2.equals(y.withNorthEast(x2).getNorthEast()));
    }

    @Test
    public void testGetLengthMeters() {
        LOG.info("testGetLengthMeters");
        final GeoPoint o = new GeoPoint(-0.5, 10.0); // Note that the values are chosen symmetrically around lat=0!
        final GeoPoint x = new GeoPoint(0.5, 10.0);
        final GeoPoint y = new GeoPoint(0.5, 11.0);
        final GeoLine ver = new GeoLine(o, x);
        final GeoLine dia = new GeoLine(o, y);

        final double len1 = Geo.METERS_PER_DEGREE_LAT;
        final double len2 = ver.getLengthMeters();
        Assert.assertEquals(0, Double.compare(len1, len2));

        final double len = 157000;
        Assert.assertTrue(Math.abs(len - dia.getLengthMeters()) < 200.0);
    }

    @Test
    public void testGetCenter() {
        LOG.info("testGetCenter");
        final GeoPoint x1 = new GeoPoint(0.0, 2.0);
        final GeoPoint y1 = new GeoPoint(10.0, 4.0);
        final GeoPoint m1 = new GeoPoint(5.0, 3.0);
        final GeoLine line1 = new GeoLine(x1, y1);
        Assert.assertEquals(m1, line1.getCenter());

        final GeoPoint x2 = new GeoPoint(-10.0, 170.0);
        final GeoPoint y2 = new GeoPoint(10.0, -175.0);

        // First check a line that is over the long side of the Earth.
        final GeoPoint shortMid = new GeoPoint(0.0, -2.5);
        final GeoLine shortSide = new GeoLine(y2, x2);
        Assert.assertEquals(shortMid, shortSide.getCenter());

        // Then over the short side.
        final GeoPoint longMid = new GeoPoint(0.0, 177.5);
        final GeoLine longSide = new GeoLine(x2, y2);
        Assert.assertEquals(longMid, longSide.getCenter());

        {
            final GeoPoint p1 = new GeoPoint(0.0, 160.0);
            final GeoPoint p2 = new GeoPoint(0.0, 170.0);
            final GeoLine p1short = new GeoLine(p1, p2);
            final GeoLine p1long = new GeoLine(p2, p1);
            final double eastingShort = p1short.getEasting();
            final double eastingLong = p1long.getEasting();
            Assert.assertTrue(eastingShort >= 0.0);
            Assert.assertTrue(eastingLong >= 0.0);
            final GeoPoint midpShort = new GeoPoint(0.0, 165.0);
            final GeoPoint midpLong = new GeoPoint(0.0, -15.0);
            Assert.assertEquals(midpShort, p1short.getCenter());
            Assert.assertEquals(midpLong, p1long.getCenter());
        }
        {
            final GeoPoint p1 = new GeoPoint(0.0, -170.0);
            final GeoPoint p2 = new GeoPoint(0.0, -160.0);
            final GeoLine p1short = new GeoLine(p1, p2);
            final GeoLine p1long = new GeoLine(p2, p1);
            final double eastingShort = p1short.getEasting();
            final double eastingLong = p1long.getEasting();
            Assert.assertTrue(eastingShort >= 0.0);
            Assert.assertTrue(eastingLong >= 0.0);
            final GeoPoint midpShort = new GeoPoint(0.0, -165.0);
            final GeoPoint midpLong = new GeoPoint(0.0, 15.0);
            Assert.assertEquals(midpShort, p1short.getCenter());
            Assert.assertEquals(midpLong, p1long.getCenter());
        }
    }

    @Test
    public void testGetNorthing() {
        LOG.info("testGetNorthing");
        final GeoPoint s2 = new GeoPoint(-80.0, 0.0);
        final GeoPoint s1 = new GeoPoint(-20.0, 1.0);
        final GeoPoint o = new GeoPoint(0.0, 2.0);
        final GeoPoint n1 = new GeoPoint(20.0, 3.0);
        final GeoPoint n2 = new GeoPoint(80.0, 4.0);

        Assert.assertTrue(MathUtils.isAlmostEqual(60.0, new GeoLine(s2, s1).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(60.0, new GeoLine(s1, s2).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(20.0, new GeoLine(s1, o).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(20.0, new GeoLine(o, s1).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(40.0, new GeoLine(s1, n1).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(40.0, new GeoLine(n1, s1).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(20.0, new GeoLine(o, n1).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(20.0, new GeoLine(n1, o).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(60.0, new GeoLine(n1, n2).getNorthing()));
        Assert.assertTrue(MathUtils.isAlmostEqual(60.0, new GeoLine(n2, n1).getNorthing()));
    }

    @Test
    public void testGetEasting() {
        LOG.info("testGetEasting");
        final GeoPoint x1 = new GeoPoint(0.0, -170.0);
        final GeoPoint x2 = new GeoPoint(1.0, -20.0);
        final GeoPoint o = new GeoPoint(2.0, 0.0);
        final GeoPoint x3 = new GeoPoint(3.0, 20.0);
        final GeoPoint x4 = new GeoPoint(4.0, 170.0);

        Assert.assertTrue(MathUtils.isAlmostEqual(150.0, new GeoLine(x1, x2).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(360.0 - 150.0, new GeoLine(x2, x1).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(170.0, new GeoLine(x1, o).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(360.0 - 170.0, new GeoLine(o, x1).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(190.0, new GeoLine(x1, x3).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(360.0 - 190.0, new GeoLine(x3, x1).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(340.0, new GeoLine(x1, x4).getEasting()));
        Assert.assertTrue(MathUtils.isAlmostEqual(360.0 - 340.0, new GeoLine(x4, x1).getEasting()));
    }

    @Test
    public void testIsWrappedOnLongSide1() {
        LOG.info("testIsWrappedOnLongSide1");

        final GeoPoint a = new GeoPoint(0.0, 0.0);
        final GeoPoint b = new GeoPoint(1.0, 1.0);
        final GeoLine shortLine = new GeoLine(a, b);
        final GeoLine longLine = new GeoLine(b, a);
        Assert.assertFalse(shortLine.isWrappedOnLongSide());
        Assert.assertTrue(longLine.isWrappedOnLongSide());
    }

    @Test
    public void testIsWrappedOnLongSide2() {
        LOG.info("testIsWrappedOnLongSide2");

        final GeoPoint a = new GeoPoint(0.0, 179.0);
        final GeoPoint b = new GeoPoint(0.0, -179.0);
        final GeoLine shortLine = new GeoLine(a, b);
        final GeoLine longLine = new GeoLine(b, a);
        Assert.assertFalse(shortLine.isWrappedOnLongSide());
        Assert.assertTrue(longLine.isWrappedOnLongSide());
    }
}
