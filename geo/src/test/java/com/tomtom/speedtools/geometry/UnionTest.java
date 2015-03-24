/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnionTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoRectangleTest.class);

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(Union.class).
                withRedefinedSuperclass().
                allFieldsShouldBeUsed().
                verify();
    }

    @Test
    public void testOverlaps() {
        LOG.info("testOverlaps");
        final GeoArea a = new GeoRectangle(new GeoPoint(1.0, 2.0), new GeoPoint(3.0, 4.0));
        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(2.5, 3.5), new GeoPoint(3.5, 4.5));
        final GeoRectangle b2 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(0.5, 0.5));
        final GeoRectangle b3 = new GeoRectangle(new GeoPoint(3.5, 4.5), new GeoPoint(4.0, 5.0));
        Assert.assertTrue(a.overlaps(b1));
        Assert.assertFalse(a.overlaps(b2));
        Assert.assertFalse(a.overlaps(b3));
    }

    @Test
    public void testContains() {
        LOG.info("testContains");
        final GeoArea a1 = new GeoCircle(new GeoPoint(1.0, 1.0), 2.0);
        final GeoRectangle a2 = new GeoRectangle(new GeoPoint(1.0, -1.0), new GeoPoint(4.0, 3.0));
        final Union u = new Union(a1, a2);
        Assert.assertTrue(u.contains(a1));
        Assert.assertTrue(u.contains(a2));
    }

    @Test
    public void testBoundingBox() {
        LOG.info("testBoundingBox");
        final GeoCircle a1 = new GeoCircle(new GeoPoint(1.0, 1.0), Geo.metersToDegreesLonAtLat(2, 1));
        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(-1.0, -1.0), new GeoPoint(3.0, 3.0));
        final Union u = new Union(a1, b1);
        Assert.assertTrue(b1.equals(u.boundingBox()));
    }

    @Test
    public void testOrigin() {
        LOG.info("testOrigin");
        final GeoCircle a1 = new GeoCircle(new GeoPoint(1.0, 1.0), Geo.metersToDegreesLonAtLat(2, 1));
        final GeoRectangle b1 = new GeoRectangle(new GeoPoint(-2.0, -2.0), new GeoPoint(3.0, 3.0));
        final Union u = new Union(a1, b1);
        Assert.assertEquals(new GeoPoint(-2.0, -2.0), u.getOrigin());
    }
}
