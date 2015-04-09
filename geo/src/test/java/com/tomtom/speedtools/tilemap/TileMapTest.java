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

package com.tomtom.speedtools.tilemap;

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.objects.Tuple;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class TileMapTest {
    private static final Logger LOG = LoggerFactory.getLogger(TileMapTest.class);

    @Test
    public void testMercator() {
        LOG.info("testMercator");
        Assert.assertTrue(doMercator(new GeoPoint(0.0, 0.0)));
        Assert.assertTrue(doMercator(new GeoPoint(-85.0, -180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(85.0, -180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(-85.0, 180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(85.0, 180.0)));
        Assert.assertTrue(doMercator(new GeoPoint(52.32461, 4.79905)));                     // Amsterdam.
        Assert.assertTrue(doMercator(new GeoPoint(-51.790780289309, -59.478705147248)));    // Falklands.
    }

    @Test
    public void testLatLonToViewportXY() {
        LOG.info("testLatLonToViewportXY");
        final int posX = 150;
        final int posY = 120;
        final int width = 600;
        final int height = 400;
        final int zoomLevel = 6;
        final GeoPoint mapCenter = MapConst.POS_AMSTERDAM;
        final GeoPoint point = TileMap.convertViewportXYToLatLon(posX, posY, width, height, zoomLevel, mapCenter);
        Assert.assertNotNull(point);

        final Tuple<Integer, Integer> xy =
                TileMap.convertLatLonToViewportXY(point, width, height, zoomLevel, mapCenter);
        Assert.assertNotNull(xy);
        Assert.assertEquals(posX, (int) xy.getValue1());
        Assert.assertEquals(posY, (int) xy.getValue2());
    }

    private static boolean doMercator(@Nonnull final GeoPoint ref) {
        assert ref != null;
        final double delta = 1.0E-7;
        final MercatorPoint mercs1 = MercatorPoint.latLonToMercs(ref);
        final GeoPoint calc = MercatorPoint.mercsToLatLon(mercs1.mercX, mercs1.mercY);
        return (Math.abs(ref.getLat() - calc.getLat()) < delta) &&
                (Math.abs(ref.getLon() - calc.getLon()) < delta);
    }
}
