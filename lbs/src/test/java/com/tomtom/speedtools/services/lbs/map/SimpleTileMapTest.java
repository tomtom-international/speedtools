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

package com.tomtom.speedtools.services.lbs.map;

import com.google.common.cache.CacheLoader;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.objects.Tuple;
import com.tomtom.speedtools.services.lbs.Lbs;
import com.tomtom.speedtools.services.lbs.map.TileMap.ViewportTileProcessor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class SimpleTileMapTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleTileMapTest.class);

    /**
     * Insert your own developer key here. All tests using this key are set to Ignored by default.
     */
    private static final String DEFAULT_DEVELOPER_API_KEY = "0-0-0-0-0";

    private static final String DEFAULT_TILEMAP_URL = "http://lbs.tomtom.com/lbs/map/1/basic";

    @Test
    public void testSimpleMapTiles() {
        LOG.info("testSimpleMapTiles");

        /**
         * Create a tile map cache to hold the images for the map.
         */
        final CachedTileMap<Bitmap> map = new CachedTileMap<>(
                -1,                                         // -1 means: use internal default limit.
                new CacheLoader<TileKey, Bitmap>() {        // Define loading method for tiles.

                    @Override
                    public Bitmap load(@Nonnull final TileKey key) {

                        // Construct a URL, or a disk path to fetch images from.
                        final String url = DEFAULT_TILEMAP_URL + '/' +
                                key.getZoomLevel() + '/' +
                                key.getTileX() + '/' +
                                key.getTileY() + '/' +
                                DEFAULT_DEVELOPER_API_KEY;

                        /**
                         * Fake implementation of fetching the image and storing the result in the
                         * bitmap. Note that this implementation serializes the loading of map tiles.
                         * Look at the "advanced" test to overcome this.
                         */
                        LOG.info("getMapTileFromLbs: HTTP GET {}", url);
                        return new Bitmap();
                    }
                });


        /**
         * Assume a viewport width/height,map center and zoom-level.
         */
        final int mapWidth = 256;
        final int mapHeight = 256;
        final GeoPoint mapCenter = Lbs.POS_AMSTERDAM;
        final int discreteZoomLevel = 6;

        // Required for unit test checking (see below).
        final List<Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>>> check = new ArrayList<>();

        /**
         * Now, collect the viewport tiles for a specific viewport. This needs to specify
         * the viewport and zoomlevel. The call uses caching if available (and pre-caching).
         */
        map.processViewportTiles(
                mapWidth,           // Width of viewport.
                mapHeight,          // Height of viewport.
                mapCenter,          // Center of viewport.
                discreteZoomLevel,  // Zoom-level to fetch.
                new ViewportTileProcessor<Bitmap>() {

                    @Override
                    public void process(
                            final int seqX, final int seqY, @Nonnull final TileKey tileKey, @Nullable final Bitmap img,
                            final int viewportX, final int viewportY,
                            final int tileOffsetX, final int tileOffsetY, final int width, final int height) {

                        /**
                         * Dummy implementation of tile plotting. The actual plot method should cut out the rectangle
                         * fo the tile described by tileOffsetXY and width/height.
                         */
                        LOG.info("process: plot tile ({}, {}) at ({}, {})", seqX, seqY, viewportX, viewportY);

                        // This code is just to make this an actual unit test, rather than just sample code.
                        check.add(new Tuple<>(new Tuple<>(seqX, seqY), new Tuple<>(viewportX, viewportY)));
                    }
                });

        /**
         * This code is added to perform an actual unit test check. It checks if the process() method
         * was called for the tiles we expected it to.
         */
        assertTrue(check.contains(new Tuple<>(new Tuple<>(0, 0), new Tuple<>(0, 0))));
        assertTrue(check.contains(new Tuple<>(new Tuple<>(1, 0), new Tuple<>(161, 0))));
        assertTrue(check.contains(new Tuple<>(new Tuple<>(0, 1), new Tuple<>(0, 121))));
        assertTrue(check.contains(new Tuple<>(new Tuple<>(1, 1), new Tuple<>(161, 121))));
    }

    // Dummy implementation of a bitmap buffer. Does nothing.
    private static class Bitmap {
        // Empty.
    }

}
