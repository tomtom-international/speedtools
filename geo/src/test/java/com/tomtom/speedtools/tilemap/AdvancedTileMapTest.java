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

import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import com.google.common.cache.CacheLoader;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.concurrent.Future;
import scala.util.Try;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("NumericCastThatLosesPrecision")
public class AdvancedTileMapTest {
    private static final Logger LOG = LoggerFactory.getLogger(AdvancedTileMapTest.class);

    /**
     * Insert your own developer key here. All tests using this key are set to Ignored by default.
     */
    private static final String DEFAULT_DEVELOPER_API_KEY = "0-0-0-0-0";

    private static final String DEFAULT_TILEMAP_URL = "http://lbs.tomtom.com/lbs/map/1/basic";

    private static final Duration TIMEOUT_RETRY_NETWORK_AFTER = Duration.millis(5000);
    private static DateTime networkFailureTime = UTCTime.MIN_TIMESTAMP_DATE;

    // Akka actor system for futures.
    private static final ActorSystem AKKA_SYSTEM = ActorSystem.create("Map");

    @Test
    public void testAdvancedSmoothAsyncMapTiles() {
        LOG.info("testAdvancedSmoothAsyncMapTiles");

        /**
         * Create a tile map cache to hold the images for the map.
         * The cache actually holds futures to images to make it possible to fetch
         * all images asynchronously and in parallel, rather than serialized. This is not
         * mandatory; it's a nice use of futures and a low-cost optimization.
         */
        final CachedTileMap<Future<Bitmap>> map = new CachedTileMap<>(
                -1,                                                 // -1 means: use internal default limit.
                new CacheLoader<TileKey, Future<Bitmap>>() {        // Define loading method for tiles.

                    @Override
                    public Future<Bitmap> load(@Nonnull final TileKey key) {
                        return getMapTileFromLbs(key);
                    }
                });

        /**
         * We can enable or disable (default) pre-caching of additional tiles to allow for better
         * zoom and pan performance.
         */
        map.setPreCachingHint(false);

        /**
         * Assume a viewport width/height,map center and zoom-level.
         *
         * Note that the zoom-level is a double, not an int, because it allows you to switch between
         * zoom-levels seemlessly. For example, if you are showing zoom-level 5 and wish to zoom in
         * to zoom-level 6, you can gradually change the value of zoomLevel from 5, 5.1, 5.2, ... 6
         * and redraw the screen every time to have a smooth transition rather than an immediate one.
         *
         * A really nice transition can be obtained by "easing out" the zoom level which is increasing
         * or decreasing the value at a rate of n% until a limit has been reached. (A nice one is
         * add/subtract 3% at 25 fps until you are within 0.02 of your goal level, then snap to the
         * goal level).
         */
        final int mapWidth = 256;
        final int mapHeight = 256;
        final GeoPoint mapCenter = MapConst.POS_AMSTERDAM;
        final double zoomLevel = 6.0;   // Use fractional zoom-levels for smooth transitions.

        /**
         * The purpose of this calculation is to allow "intermediate" zoom levels between integers
         * which show scaled images between to discrete zoom levels. This allows seemless zooming
         * from 1 level to another (very smooth). It also require a plot function that is able to
         * scale images (see below).
         */
        final int discreteZoomLevel = (int) Math.round(zoomLevel);
        final double zoomFactor = 1.0 - ((discreteZoomLevel - zoomLevel) / 2.0);
        assert Math.abs(zoomFactor - 1) <= 0.25 : zoomFactor;
        final double zoomFactorRecip = 1.0 / zoomFactor;

        /**
         * Scaling in this context always means scaling down. So we may need more scaled images
         * than original images to fill our viewport. This is why we need to potentially request
         * more images than just "viewport_size / tile_size".
         */
        final int requestWidth = (int) Math.round(zoomFactorRecip * mapWidth);
        final int requestHeight = (int) Math.round(zoomFactorRecip * mapHeight);

        /**
         * Now, collect the viewport tiles for a specific viewport. This needs to specify
         * the viewport and the discrete zoom-level. The call uses caching if available
         * (and pre-caching).
         */
        final Collection<ViewportTile<Future<Bitmap>>> tiles = map.collectViewportTiles(
                requestWidth,       // Width of viewport.
                requestHeight,      // Height of viewport.
                mapCenter,          // Center of viewport.
                discreteZoomLevel); // Zoom-level to fetch.
        assertEquals(4, tiles.size());

        /**
         * Draw the tiles 1 by 1. This requires a bit of knowledge about futures, as we
         * store futures to images in the cache, rather than images. What we do here is, is
         * plot the image if it's there and show an "empty" tile if it's not.
         *
         * The screen is supposed to be refreshed periodically, rather than only on scroll
         * or zoom actions, so missing tiles will show up automatically once they're there.
         */
        for (final ViewportTile<Future<Bitmap>> tile : tiles) {
            final Bitmap actualImg;

            /**
             * Determine if the image already has been loaded or not. If not, use a temporary
             * image until it has.
             */
            assert tile.img != null;
            if (!tile.img.isCompleted()) {

                // The tile wasn't loaded yet. Use an empty bitmap.
                actualImg = new Bitmap();
            } else {

                // The tile was loaded.
                final Option<Try<Bitmap>> imgValue = tile.img.value();
                assert imgValue.isDefined();
                final Try<Bitmap> imgGet = imgValue.get();
                if (imgGet != null) {

                    // Get the bitmap from the future.
                    actualImg = imgGet.get();
                    assert actualImg != null;
                } else {

                    // The tile wasn't loaded properly. Invalidate it, so it will be retried.
                    map.invalidate(tile.tileKey);
                    actualImg = new Bitmap();
                }
            }

            /**
             * Calculate the scaled tile position and sizes. Add just under 0.5 pixels to the requested
             * width, so the tiles may slightly overlap during a transition, but not 'underlap' (causes
             * ugly gaps). Stay under 0.5 to avoid actual overlap when the zoom transition stops.
             */
            final double x = zoomFactor * tile.viewportX;   // Calculate X and Y.
            final double y = zoomFactor * tile.viewportY;
            final double dX = x - Math.floor(x);          // Calculate pixel fractions.
            final double dY = y - Math.floor(y);
            final long newX = (long) Math.floor(zoomFactor * tile.viewportX);
            final long newY = (long) Math.floor(zoomFactor * tile.viewportY);
            final long newWidth = (long) Math.ceil(zoomFactor * (dX + (double) tile.width));
            final long newHeight = (long) Math.ceil(zoomFactor * (dY + (double) tile.height));

            /**
             * Call the actual plot method, which is able to plot a rectangular portion of a tile
             * only and to scale it down.
             */
            plotResizedPartialTile(
                    actualImg,
                    tile.tileOffsetX, tile.tileOffsetY, tile.width, tile.height,
                    (int) newX, (int) newY, (int) newWidth, (int) newHeight);
        }
    }

    // Dummy implementation of plot method. Does nothing here, expect log its parameters.
    private static void plotResizedPartialTile(
            final Bitmap bitmap,
            final int tileOffsetX, final int tileOffsetY,
            final int tileWidth, final int tileHeight,
            final int newX, final int newY,
            final int newWidth, final int newHeight) {
        LOG.debug("plotResizedTile: tile ({}, {}, {}, {}) -> ({}, {}, {}, {}) - {}",
                tileOffsetX, tileOffsetY, tileWidth, tileHeight,
                newX, newY, newWidth, newHeight,
                bitmap.isDefined() ? "loaded" : "not loaded yet");
    }

    /**
     * Dummy implementation of a bitmap buffer. Does nothing. The 'defined' value is just to show
     * in the test output that tiles may or may not be loaded yet asynchronously.
     */
    private static class Bitmap {
        private final boolean isDefined;

        public Bitmap() {
            isDefined = false;
        }

        public Bitmap(final boolean isDefined) {
            this.isDefined = isDefined;
        }

        public boolean isDefined() {
            return isDefined;
        }
    }

    /**
     * Get a map tile from LBS and return a {@link Future} to its tile image.
     *
     * @param key Tile key.
     * @return {@link Future} to tile image.
     */
    @Nonnull
    private static Future<Bitmap> getMapTileFromLbs(@Nonnull final TileKey key) {

        // Create a URL to fetch a bitmap from the TomTom LBS system. This is an actual working URL!
        final String url = DEFAULT_TILEMAP_URL + '/' +
                key.getZoomLevel() + '/' +
                key.getTileX() + '/' +
                key.getTileY() + '/' +
                DEFAULT_DEVELOPER_API_KEY;

        final Future<Bitmap> promise = Futures.future(new Callable<Bitmap>() {

            @Nonnull
            @Override
            public Bitmap call() throws IOException {

                /**
                 * Try to load an image if the network is not down, or retry after some time.
                 *
                 * Note: app.loadImage seems to have an occasional problem with thread-safety,
                 * but synchronizing on app makes it way too slow.
                 *
                 * Note also that accessing networkFailureTime is extremely fast, but not
                 * thread-safe this way, so it might be set to null any time. Hence, do not
                 * rely on its non-nullity in the if-expression (DateTime.isAfter works fine
                 * with null).
                 */
                if ((networkFailureTime == null) ||
                        (UTCTime.now().minus(TIMEOUT_RETRY_NETWORK_AFTER).isAfter(networkFailureTime))) {
                    Bitmap img = null;
                    try {

                        /**
                         * Fake implementation of calling the URL and storing the HTTP response in
                         * the bitmap buffer. The code assumes that if "img == null", the call to
                         * get the image failed. In this example, the call never fails though.
                         */

                        img = new Bitmap(true);
                        LOG.info("getMapTileFromLbs: HTTP GET {}...", url);
                    } catch (Exception ignored) {
                        // Continue.
                    }

                    // Remember failure time.
                    if (img == null) {
                        networkFailureTime = UTCTime.now();
                        throw new IOException("Image could not be loaded, url=" + url);
                    }

                    // Network seems to be OK again.
                    networkFailureTime = null;
                    return img;
                } else {
                    throw new IOException("Image could not be loaded (not even tried), url=" + url);
                }
            }
        }, AKKA_SYSTEM.dispatcher());
        return promise;
    }
}
