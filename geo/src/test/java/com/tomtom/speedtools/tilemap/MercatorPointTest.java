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

package com.tomtom.speedtools.tilemap;

import com.tomtom.speedtools.geometry.Geo;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.utils.MathUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class MercatorPointTest {
    private static final Logger LOG = LoggerFactory.getLogger(MercatorPointTest.class);

    @Test
    public void testMercsToLatLon() {
        LOG.info("testMercsToLatLon");
        Assert.assertEquals(MercatorPoint.mercsToLatLon(new MercatorPoint(0.5, 0.5)), new GeoPoint(0.0, 0.0));
        GeoPoint expected = MercatorPoint.mercsToLatLon(new MercatorPoint(0.0, 0.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getLat(), 85.0, 86.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getLon(), -180.0, -179.9));
        expected = MercatorPoint.mercsToLatLon(new MercatorPoint(1.0, 1.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getLat(), -86.0, -85.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getLon(), 179.9, 180.0));
    }

    @Test
    public void testLatLonToMercs() {
        LOG.info("testLatLonToMercs");
        Assert.assertEquals(new MercatorPoint(0.5, 0.5), MercatorPoint.latLonToMercs(new GeoPoint(0.0, 0.0)));
        MercatorPoint expected = MercatorPoint.latLonToMercs(new GeoPoint(90.0, -180.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getMercX(), 0.0, 0.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getMercY(), 0.0, 0.01));
        expected = MercatorPoint.latLonToMercs(new GeoPoint(-90.0, Geo.LON180));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getMercX(), 1.0, 1.0));
        Assert.assertTrue(expected.toString(), MathUtils.isBetween(expected.getMercY(), 0.99, 1.0));
    }

    @Test
    public void testMercatorBackAndForth() {
        LOG.info("testMercatorBackAndForth");
        Assert.assertTrue(doMercator(new GeoPoint(0.0, 0.0)));
        Assert.assertTrue(doMercator(new GeoPoint(-85.0, -180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(85.0, -180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(-85.0, 180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(85.0, 180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(52.32461, 4.79905)));                     // Amsterdam.
        Assert.assertTrue(doMercator(new GeoPoint(-51.790780289309, -59.478705147248)));    // Falklands.
    }

    private static boolean doMercator(@Nonnull final GeoPoint ref) {
        assert ref != null;
        final double delta = 1.0E-7;
        final MercatorPoint mercs1 = MercatorPoint.latLonToMercs(ref);
        final GeoPoint calc = MercatorPoint.mercsToLatLon(mercs1.getMercX(), mercs1.getMercY());
        return (Math.abs(ref.getLat() - calc.getLat()) < delta) &&
                (Math.abs(ref.getLon() - calc.getLon()) < delta);
    }
}
