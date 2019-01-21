/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeoRectangleTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoRectangleTest.class);

    private final GeoPoint x1 = new GeoPoint(0.0, 0.0);
    private final GeoPoint x2 = new GeoPoint(0.0, 0.0);
    private final GeoRectangle x = new GeoRectangle(x1, x2);

    private GeoPoint y1 = null;
    private GeoPoint y2 = null;
    private GeoRectangle y = null;

    @Before
    public void setUp() {
        y1 = new GeoPoint(-1.0, -2.0);
        y2 = new GeoPoint(3.0, 4.0);
        y = new GeoRectangle(y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GeoRectangle.class).
                withRedefinedSuperclass().
                verify();
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testAssertNullCoordinates() throws ValidationFailException {
        LOG.info("testAssertNullCoordinates");

        ConstructorChecker.validateConstructor(GeoRectangle.class, new int[]{}, new GeoPoint(0.0, 0.0));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(GeoRectangle.class, new int[]{}, y1, y2);
    }

    @Test
    public void testgetWorld() {
        LOG.info("testgetWorld");
        Assert.assertTrue(GeoRectangle.getWorld().contains(new GeoPoint(-90.0, -180.0)));
        Assert.assertTrue(GeoRectangle.getWorld().contains(new GeoPoint(-90.0, 180.0)));
        Assert.assertTrue(GeoRectangle.getWorld().contains(new GeoPoint(90.0, -180.0)));
        Assert.assertTrue(GeoRectangle.getWorld().contains(new GeoPoint(90.0, 180.0)));
        Assert.assertTrue(GeoRectangle.getWorld().contains(new GeoPoint(0.0, 0.0)));
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
    public void testBoundingBox() {
        LOG.info("testBoundingBox");
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(0.0, 0.0)), x.boundingBox());
        Assert.assertEquals(new GeoRectangle(new GeoPoint(-1.0, -2.0), new GeoPoint(3.0, 4.0)), y.boundingBox());
        Assert.assertEquals(x, x.boundingBox());
        Assert.assertEquals(y, y.boundingBox());
    }

    @Test
    public void testOverlaps() {
        LOG.info("testOverlaps");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle a2 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(2.0, 2.0));
        final GeoRectangle a3 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(0.5, 0.5));
        final GeoRectangle a4 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(1.5, 1.5));
        final GeoRectangle a5 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(2.5, 2.5));
        Assert.assertTrue(a1.overlaps(a1));
        Assert.assertTrue(a1.overlaps(a2));
        Assert.assertFalse(a1.overlaps(a3));
        Assert.assertTrue(a4.overlaps(a2));
        Assert.assertTrue(a5.overlaps(a2));

        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0));
        final GeoRectangle b2 = new GeoRectangle(new GeoPoint(0.0, 161.0), new GeoPoint(2.0, -160.0));
        final GeoRectangle b3 = new GeoRectangle(new GeoPoint(0.0, 161.0), new GeoPoint(2.0, 162.0));
        final GeoRectangle b4 = new GeoRectangle(new GeoPoint(0.0, 159.0), new GeoPoint(2.0, -159.0));
        final GeoRectangle b5 = new GeoRectangle(new GeoPoint(0.0, -159.0), new GeoPoint(2.0, 159.0));
        final GeoRectangle b6 = new GeoRectangle(new GeoPoint(0.0, -161.0), new GeoPoint(2.0, -159.0));
        Assert.assertTrue(b1.overlaps(b2));
        Assert.assertTrue(b1.overlaps(b3));
        Assert.assertTrue(b1.overlaps(b4));
        Assert.assertFalse(b1.overlaps(b5));
        Assert.assertTrue(b1.overlaps(b6));
        Assert.assertFalse(a5.overlaps(b1));
    }

    @Test
    public void testContains() {
        LOG.info("testContains");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle a2 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(2.0, 2.0));
        final GeoRectangle a3 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(0.5, 0.5));
        final GeoRectangle a4 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(1.5, 1.5));
        final GeoRectangle a5 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(2.5, 2.5));
        Assert.assertFalse(a1.contains(new GeoPoint(1.0, 0.0)));
        Assert.assertFalse(a1.contains(new GeoPoint(0.0, 1.0)));
        Assert.assertTrue(a1.contains(new GeoPoint(1.0, 1.0)));
        Assert.assertTrue(a1.contains(a1));
        Assert.assertTrue(a2.contains(a1));
        Assert.assertTrue(a2.contains(a3));
        Assert.assertFalse(a3.contains(a2));
        Assert.assertFalse(a3.contains(a4));
        Assert.assertFalse(a3.contains(a4));
        Assert.assertTrue(a5.contains(a4));

        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0));
        final GeoRectangle b2 = new GeoRectangle(new GeoPoint(0.0, 161.0), new GeoPoint(2.0, -160.0));
        final GeoRectangle b3 = new GeoRectangle(new GeoPoint(0.0, 161.0), new GeoPoint(2.0, 162.0));
        final GeoRectangle b4 = new GeoRectangle(new GeoPoint(0.0, 159.0), new GeoPoint(2.0, -159.0));
        final GeoRectangle b5 = new GeoRectangle(new GeoPoint(0.0, -159.0), new GeoPoint(2.0, 159.0));
        final GeoRectangle b6 = new GeoRectangle(new GeoPoint(0.0, -161.0), new GeoPoint(2.0, -159.0));
        Assert.assertTrue(b1.contains(b2));
        Assert.assertTrue(b1.contains(b3));
        Assert.assertFalse(b1.contains(b4));
        Assert.assertFalse(b1.contains(b5));
        Assert.assertFalse(b1.contains(b6));
        Assert.assertFalse(a5.contains(b1));
    }

    @Test
    public void testTranslate() {
        LOG.info("testTranslate");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(2.0, 2.0));
        final GeoVector p = new GeoVector(1.0, 1.0);
        final GeoRectangle a2 = a1.translate(p);
        final GeoPoint p1 = new GeoPoint(1.0, 1.0);
        final GeoPoint p2 = new GeoPoint(3.0, 3.0);
        Assert.assertEquals(p1, a2.getSouthWest());
        Assert.assertEquals(p2, a2.getNorthEast());

        final GeoVector q = new GeoVector(0.0, -181.0);
        final GeoRectangle a3 = a1.translate(q);
        final GeoPoint q1 = new GeoPoint(0.0, 179.0);
        final GeoPoint q2 = new GeoPoint(2.0, -179.0);
        Assert.assertEquals(q1, a3.getSouthWest());
        Assert.assertEquals(q2, a3.getNorthEast());

        final GeoVector r = new GeoVector(0.0, 181.0);
        final GeoRectangle a4 = a1.translate(r);
        final GeoPoint r1 = new GeoPoint(0.0, -179.0);
        final GeoPoint r2 = new GeoPoint(2.0, -177.0);
        Assert.assertEquals(r1, a4.getSouthWest());
        Assert.assertEquals(r2, a4.getNorthEast());
    }

    @Test
    public void testTranslateWithElevation() {
        LOG.info("testTranslateWithElevation");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(0.0, 0.0, 5.0), new GeoPoint(2.0, 2.0));
        final GeoVector p = new GeoVector(1.0, 1.0, 10.0);
        final GeoRectangle a2 = a1.translate(p);
        final GeoPoint p1 = new GeoPoint(1.0, 1.0, 15.0);
        final GeoPoint p2 = new GeoPoint(3.0, 3.0, 15.0);
        Assert.assertEquals(p1, a2.getSouthWest());
        Assert.assertEquals(p2, a2.getNorthEast());

        final GeoVector q = new GeoVector(1.0, 1.0, -10.0);
        final GeoRectangle a3 = a1.translate(q);
        final GeoPoint q1 = new GeoPoint(1.0, 1.0, -5.0);
        final GeoPoint q2 = new GeoPoint(3.0, 3.0, -5.0);
        Assert.assertEquals(q1, a3.getSouthWest());
        Assert.assertEquals(q2, a3.getNorthEast());
    }

    @Test
    public void testMoveTo() {
        LOG.info("testMoveTo");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(2.0, 2.0));
        final GeoPoint p = new GeoPoint(10.0, 10.0);
        final GeoRectangle a2 = a1.moveTo(p);
        final GeoPoint p1 = new GeoPoint(10.0, 10.0);
        final GeoPoint p2 = new GeoPoint(12.0, 12.0);
        Assert.assertEquals(p1, a2.getSouthWest());
        Assert.assertEquals(p2, a2.getNorthEast());

        // Wrap over lon = 180.
        final GeoPoint q = new GeoPoint(10.0, 179.0);
        final GeoRectangle a3 = a1.moveTo(q);
        final GeoPoint q1 = new GeoPoint(10.0, 179.0);
        final GeoPoint q2 = new GeoPoint(12.0, -179.0);
        Assert.assertEquals(q1, a3.getSouthWest());
        Assert.assertEquals(q2, a3.getNorthEast());

        final GeoPoint r = new GeoPoint(10.0, -179.0);
        final GeoRectangle a4 = a1.moveTo(r);
        final GeoPoint r1 = new GeoPoint(10.0, -179.0);
        final GeoPoint r2 = new GeoPoint(12.0, -177.0);
        Assert.assertEquals(r1, a4.getSouthWest());
        Assert.assertEquals(r2, a4.getNorthEast());
    }

    @Test
    public void testGrowRect() {
        LOG.info("testGrowRect");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle a2 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(2.0, 2.0));
        final GeoRectangle a3 = new GeoRectangle(new GeoPoint(1.0, 1.0), new GeoPoint(3.0, 3.0));
        final GeoRectangle a4 = new GeoRectangle(new GeoPoint(-1.0, -1.0), new GeoPoint(4.0, 4.0));
        final GeoRectangle a5 = new GeoRectangle(new GeoPoint(0.2, 0.3), new GeoPoint(0.4, 0.5));
        final GeoRectangle a6 = new GeoRectangle(new GeoPoint(-1.0, 0.0), new GeoPoint(0.3, 0.5));
        final GeoRectangle a7 = new GeoRectangle(new GeoPoint(0.5, 0.5), new GeoPoint(3.0, 4.0));

        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(2.0, 2.0));
        final GeoRectangle b2 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(3.0, 3.0));
        final GeoRectangle b3 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle b4 = new GeoRectangle(new GeoPoint(-1.0, 0.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle b5 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(3.0, 4.0));
        Assert.assertEquals(b1, a1.grow(a2));
        Assert.assertEquals(b2, a1.grow(a3));
        Assert.assertEquals(a4, a1.grow(a4));
        Assert.assertEquals(b3, a1.grow(a5));
        Assert.assertEquals(b4, a1.grow(a6));
        Assert.assertEquals(b5, a1.grow(a7));
    }

    @Test
    public void testGrowPoint() {
        LOG.info("testGrowPoint");
        final GeoRectangle a1 = new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(2.0, 160.0));
        final GeoRectangle a2 = new GeoRectangle(new GeoPoint(0.0, -160.0), new GeoPoint(2.0, -150.0));
        final GeoRectangle a3 = new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0));

        final GeoPoint p1 = new GeoPoint(1.0, 170.0);
        final GeoPoint p2 = new GeoPoint(1.0, -170.0);
        final GeoPoint p3 = new GeoPoint(1.0, 150.0);
        final GeoPoint p4 = new GeoPoint(1.0, -150.0);

        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(2.0, 170.0)), a1.grow(p1));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(2.0, -170.0)), a1.grow(p2));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 170.0), new GeoPoint(2.0, -150.0)), a2.grow(p1));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, -170.0), new GeoPoint(2.0, -150.0)), a2.grow(p2));

        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0)), a3.grow(p1));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0)), a3.grow(p2));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(2.0, -160.0)), a3.grow(p3));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -150.0)), a3.grow(p4));

        // Non-wrapped rectangle. Try all points in and outside.
        final GeoPoint p5 = new GeoPoint(1.0, 151.0);
        final GeoPoint p6 = new GeoPoint(-1.0, 151.0);
        final GeoPoint p7 = new GeoPoint(0.0, 149.0);
        final GeoPoint p8 = new GeoPoint(1.0, 161.0);
        final GeoPoint p9 = new GeoPoint(3.0, 159.0);
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(2.0, 160.0)), a1.grow(p5));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(-1.0, 150.0), new GeoPoint(2.0, 160.0)), a1.grow(p6));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 149.0), new GeoPoint(2.0, 160.0)), a1.grow(p7));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(2.0, 161.0)), a1.grow(p8));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 150.0), new GeoPoint(3.0, 160.0)), a1.grow(p9));

        // Wrapped rectangle. Try all points in and outside.
        final GeoPoint p10 = new GeoPoint(1.0, 161.0);
        final GeoPoint p11 = new GeoPoint(1.0, -161.0);
        final GeoPoint p12 = new GeoPoint(1.0, 159.0);
        final GeoPoint p13 = new GeoPoint(-1.0, 161.0);
        final GeoPoint p14 = new GeoPoint(1.0, -159.0);
        final GeoPoint p15 = new GeoPoint(3.0, -161.0);
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0)), a3.grow(p10));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -160.0)), a3.grow(p11));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 159.0), new GeoPoint(2.0, -160.0)), a3.grow(p12));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(-1.0, 160.0), new GeoPoint(2.0, -160.0)), a3.grow(p13));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(2.0, -159.0)), a3.grow(p14));
        Assert.assertEquals(new GeoRectangle(new GeoPoint(0.0, 160.0), new GeoPoint(3.0, -160.0)), a3.grow(p15));
    }

    @Test
    public void testIsWrappedOnLongSide() {
        LOG.info("testIsWrappedOnLongSide");
        final GeoPoint a = new GeoPoint(0.0, 0.0);
        final GeoPoint b = new GeoPoint(1.0, 1.0);
        final GeoRectangle shortRect = new GeoRectangle(a, b);
        final GeoRectangle longRect = new GeoRectangle(b, a);
        Assert.assertFalse(shortRect.isWrapped());
        Assert.assertTrue(longRect.isWrapped());
    }
}

