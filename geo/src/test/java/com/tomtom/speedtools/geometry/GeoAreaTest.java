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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class GeoAreaTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoAreaTest.class);

    @Test
    public void testFlatten() {
        LOG.info("testFlatten");
        final GeoRectangle r1 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle r2 = new GeoRectangle(new GeoPoint(1.0, 0.0), new GeoPoint(2.0, 1.0));
        final GeoArea area = r1.add(r2);
        final Collection<GeoRectangle> flat = area.pixelate();
        Assert.assertFalse(flat.isEmpty());
        boolean b1 = false;
        boolean b2 = false;
        for (final GeoRectangle rect : flat) {
            b1 = b1 || rect.contains(r1);
            b2 = b2 || rect.contains(r2);
        }
        Assert.assertTrue(b1);
        Assert.assertTrue(b2);
    }

    @Test
    public void testFromAreas() {
        LOG.info("testFromAreas");
        final GeoRectangle r1 = new GeoRectangle(new GeoPoint(0.0, 0.0), new GeoPoint(1.0, 1.0));
        final GeoRectangle r2 = new GeoRectangle(new GeoPoint(1.0, 0.0), new GeoPoint(2.0, 1.0));
        final Collection<GeoArea> list = new ArrayList<>();
        list.add(r1);
        list.add(r2);
        final GeoArea area = GeoArea.fromAreas(list);
        Assert.assertTrue(area.contains(r1));
        Assert.assertTrue(area.contains(r2));
    }

    @Test
    public void testTranslateInMeters() {
        LOG.info("testTranslateInMeters");
        final double meters = 20.0;
        final double lat = 10.0;
        final GeoRectangle r1 = new GeoRectangle(new GeoPoint(lat, 10.0), new GeoPoint(12.0, 112.0));
        final double northing = Geo.metersToDegreesLat(meters);
        final double easting = Geo.metersToDegreesLonAtLat(meters, lat);
        Assert.assertEquals(r1.translate(new GeoVector(northing, easting)), r1.translate(20, 20));
    }
}

