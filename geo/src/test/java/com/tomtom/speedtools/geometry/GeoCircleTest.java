/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

@SuppressWarnings("FieldMayBeStatic")
public class GeoCircleTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoCircleTest.class);

    private final GeoPoint x1 = new GeoPoint(1.0, 2.0);
    private final double x2 = 0.0;
    private final GeoCircle x = new GeoCircle(x1, x2);

    private GeoPoint y1 = null;
    private double y2 = 0.0;
    private GeoCircle y = null;

    @Before
    public void setUp() {
        y1 = new GeoPoint(3.0, 4.0);
        y2 = 5.0;
        y = new GeoCircle(y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GeoCircle.class).
                withRedefinedSuperclass().
                verify();
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(GeoCircle.class, new int[]{}, y1, y2);
    }

    @Test
    public void testWithCenter() {
        LOG.info("testWithCenter");
        Assert.assertTrue(y1.equals(x.withCenter(y1).getCenter()));
        Assert.assertTrue(x1.equals(y.withCenter(x1).getCenter()));
    }

    @Test
    public void testWithRadius() {
        LOG.info("testWithRadius");
        Assert.assertTrue(Double.compare(y2, x.withRadiusMeters(y2).getRadiusMeters()) == 0);
        Assert.assertTrue(Double.compare(x2, y.withRadiusMeters(x2).getRadiusMeters()) == 0);
    }

    @Test
    public void testOverlaps() {
        LOG.info("testOverlaps");
        final GeoCircle a1 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(0.5, 0.0));
        final GeoCircle a2 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(1.0, 0.0));
        final GeoCircle a3 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(1.5, 0.0));
        final GeoCircle a4 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(2.0, 0.0));
        Assert.assertTrue(a1.overlaps(a1));
        Assert.assertTrue(a1.overlaps(a2));
        Assert.assertTrue(a1.overlaps(a3));
        Assert.assertTrue(a1.overlaps(a4));

        final GeoCircle b1 = new GeoCircle(new GeoPoint(2.0, 0.0), Geo.degreesLonToMetersAtLat(0.5, 0.0));
        final GeoCircle b2 = new GeoCircle(new GeoPoint(2.0, 0.0), Geo.degreesLonToMetersAtLat(1.0, 0.0));
        final GeoCircle b3 = new GeoCircle(new GeoPoint(2.0, 0.0), Geo.degreesLonToMetersAtLat(1.5, 0.0));
        Assert.assertTrue(b1.overlaps(b1));
        Assert.assertTrue(b1.overlaps(b2));
        Assert.assertTrue(b1.overlaps(b3));

        Assert.assertFalse(a1.overlaps(b1));
        Assert.assertFalse(a1.overlaps(b2));
        // This test may fail or succeed, as "overlaps" is an approximation: Assert.assertFalse(a1.overlaps(b3));

        Assert.assertFalse(a2.overlaps(b1));
        Assert.assertTrue(a2.overlaps(b2));
        Assert.assertTrue(a2.overlaps(b3));

        Assert.assertTrue(a3.overlaps(b1));
        Assert.assertTrue(a3.overlaps(b2));
        Assert.assertTrue(a3.overlaps(b3));

        Assert.assertTrue(a4.overlaps(b1));
        Assert.assertTrue(a4.overlaps(b2));
        Assert.assertTrue(a4.overlaps(b3));

        // This test may fail or succeed, as "overlaps" is an approximation:
        //   final GeoCircle c1 = new GeoCircle(new GeoPoint(1, 1), 0.5);
        //   Assert.assertFalse(a1.overlaps(c1));
        final GeoCircle c2 = new GeoCircle(new GeoPoint(1.0, 1.0), Geo.degreesLonToMetersAtLat(1, 0));

        Assert.assertTrue(a1.overlaps(c2));
        Assert.assertTrue(a2.overlaps(c2));
        Assert.assertTrue(a3.overlaps(c2));
    }

    @Test
    public void testContains() {
        LOG.info("testContains");
        final GeoCircle a1 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(0.5, 0.0));
        final GeoCircle a2 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(1.0, 0.0));
        Assert.assertFalse(a1.contains(new GeoPoint(1.0, 0.0)));
        Assert.assertFalse(a1.contains(new GeoPoint(0.0, 1.0)));
        Assert.assertTrue(a1.contains(new GeoPoint(0.25, 0.25)));
        Assert.assertTrue(a1.contains(a1));
        Assert.assertTrue(a2.contains(a1));
        Assert.assertFalse(a1.contains(a2));
    }

    @Test
    public void testBoundingBox() {
        LOG.info("testBoundingBox");
        final GeoCircle a1 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.degreesLonToMetersAtLat(2, 0));
        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(-2.0, -2.0), new GeoPoint(2.0, 2.0));
        LOG.info("{} == {}", a1.boundingBox().toString(), b1.boundingBox().toString());
        Assert.assertTrue((b1.getNorthing() - a1.boundingBox().getNorthing()) < 0.001);
        Assert.assertTrue((b1.getEasting() - a1.boundingBox().getEasting()) < 0.001);
    }

    @Test
    public void testConstructor() {
        LOG.info("testConstructor");
        final GeoCircle c1 = new GeoCircle(new GeoPoint(0.0, 0.0), Geo.METERS_PER_DEGREE_LON_EQUATOR);
        final GeoCircle c2 = new GeoCircle(new GeoPoint(0.0, 0.0), new GeoPoint(0.0, 1.0));
        Assert.assertEquals(c1, c2);
    }
}
