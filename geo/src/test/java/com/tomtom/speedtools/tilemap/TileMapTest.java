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

package com.tomtom.speedtools.tilemap;

import com.tomtom.speedtools.geometry.Geo;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.objects.Tuple;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileMapTest {
    private static final Logger LOG = LoggerFactory.getLogger(TileMapTest.class);

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

    @Test
    public void testConvertLatLonToTileOffset() {
        LOG.info("testConvertLatLonToTileOffset");
        Assert.assertEquals(new TileOffset(new TileKey(2, 2, 2), 0, 0),
                TileMap.convertLatLonToTileOffset(new GeoPoint(0.0, 0.0), 2));
        Assert.assertEquals(new TileOffset(new TileKey(2, 0, 2), 0, 2),
                TileMap.convertLatLonToTileOffset(new GeoPoint(90.0, 0.0), 2));
        Assert.assertEquals(new TileOffset(new TileKey(3, 2, 2), MapConst.PIXELS_PER_TILE, 0),
                TileMap.convertLatLonToTileOffset(new GeoPoint(0.0, Geo.LON180), 2));
        Assert.assertEquals(new TileOffset(new TileKey(0, 2, 2), 0, 0),
                TileMap.convertLatLonToTileOffset(new GeoPoint(0.0, 180.0), 2));
        Assert.assertEquals(new TileOffset(new TileKey(0, 2, 2), 0, 0),
                TileMap.convertLatLonToTileOffset(new GeoPoint(0.0, -180.0), 2));
        Assert.assertEquals(new TileOffset(new TileKey(0, 2, 2), 0, 0),
                TileMap.convertLatLonToTileOffset(new GeoPoint(0.0, -Geo.LON180), 2));
    }
}
