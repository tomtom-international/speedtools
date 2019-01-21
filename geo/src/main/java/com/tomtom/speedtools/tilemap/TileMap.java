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

package com.tomtom.speedtools.tilemap;

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.geometry.GeoRectangle;
import com.tomtom.speedtools.objects.Tuple;
import com.tomtom.speedtools.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents an (abstract) cacheable map. The type parameter T is the image tile type. It is parametrized to
 * allow retrieving tiles in different internal formats, such as AWT Image objects, or Processing PImage objects.
 */
public abstract class TileMap<T> {
    private final int bufferColumns;    // Additional tile columns to left and right of viewport, for buffering.
    private final int bufferRows;       // Additional tile rows on top and bottom of viewport, for buffering.

    /**
     * Pre-caching means fetching tiles that might be useful later. This may speed up or slow down the application,
     * depending on network performance.
     */
    private boolean preCaching;

    protected TileMap() {
        this(0, 0);
    }

    protected TileMap(final int bufferColumns, final int bufferRows) {
        assert bufferColumns >= 0;
        assert bufferRows >= 0;
        this.bufferColumns = bufferColumns;
        this.bufferRows = bufferRows;
        this.preCaching = false;
    }

    /**
     * Get a map tile, given a map tile key. This function is implemented by the user of the tile.
     *
     * @param key Tile key.
     * @return Tile, or null if missing.
     */
    @Nullable
    public abstract T getTile(@Nonnull final TileKey key);

    /**
     * Returns if the map supports caching.
     *
     * @return True if the map supports caching (does not say anything about the cache size).
     */
    public boolean isCacheEnabled() {
        return false;
    }

    /**
     * Set pre-caching on or off, if possible. Pre-caching means fetching tiles that might be useful later. This may
     * speed up or slow down the application, depending on network performance.
     *
     * Pre-caching can only be set if caching is enabled. Setting the value to true does not always mean pre-caching
     * will be enabled. The cache itself must support it.
     *
     * @param preCaching True of pre-caching is enabled.
     */
    public void setPreCachingHint(final boolean preCaching) {
        this.preCaching = isCacheEnabled() && preCaching;
    }

    /**
     * Returns if the map supports pre-caching of additional non-visible tiles.
     *
     * @return True if the map supports pre-caching (does not say anything about the cache size).
     */
    public boolean isPreCacheEnabled() {
        return preCaching;
    }

    /**
     * Buffer a map tile which is not shown in the viewport, given a map tile key. This function can be used to preload
     * image tiles for caching maps. Note that this function ALWAYS gets called, so only buffer the image if you do not
     * already have it.
     *
     * @param key Tile key.
     */
    public void cacheTile(@Nonnull final TileKey key) {
        assert key != null;
        // Empty.
    }

    /**
     * Callback function for viewport tile processing.
     *
     * The template parameter T is the image class, e.g. Image or PImage.
     */
    public interface ViewportTileProcessor<T> {

        /**
         * Process one viewport tile.
         *
         * @param seqX        Sequence number in X direction (0, 1, 2, ...), 0 = left.
         * @param seqY        Sequence number in Y direction (0, 1, 2, ...), 0 = top.
         * @param tileKey     Tile key.
         * @param viewportX   Position X from top-left of viewport.
         * @param viewportY   Position Y from top-left of viewport.
         * @param img         Tile image.
         * @param tileOffsetX Crop area, position X from top-left of image.
         * @param tileOffsetY Crop area, position Y from top-left of image.
         * @param width       Width of crop area to plot.
         * @param height      Height of crop area to plot.
         */
        void process(
                final int seqX, final int seqY,
                @Nonnull final TileKey tileKey,
                @Nullable final T img,
                final int viewportX, final int viewportY,
                final int tileOffsetX, final int tileOffsetY, final int width, final int height);
    }

    /**
     * Process all tiles for a specific viewport. For every tile a callback function 'process()' is called. This
     * function can either immediately draw the image, or for example store it in a collection.
     *
     * @param widthPixels  Width of viewport in pixels.
     * @param heightPixels Height of viewport in pixels.
     * @param mapCenter    Center of map, as lat/lon.
     * @param zoomLevel    Zoomlevel (from 0..LbsConst.MapConst.MAXIMUM_ZOOM).
     * @param processor    Viewport processor.
     */
    @SuppressWarnings("ConstantConditions")
    public void processViewportTiles(
            final int widthPixels, final int heightPixels,
            @Nonnull final GeoPoint mapCenter, final int zoomLevel,
            @Nonnull final ViewportTileProcessor<T> processor) {
        assert widthPixels >= 0;
        assert heightPixels >= 0;
        assert mapCenter != null;
        assert processor != null;
        assert MathUtils.isBetween(zoomLevel, 0, MapConst.MAXIMUM_ZOOM) : zoomLevel;
        assert MapConst.PIXELS_PER_TILE > 0;
        assert processor != null;

        // Calculate total number of tiles on this zoomlevel.
        final long nrTiles = (1L << zoomLevel);

        // Determine how many tiles top-left tile should shift to center the map.
        final int shiftTileIndexX = widthPixels / MapConst.PIXELS_PER_TILE / 2;
        final int shiftTileIndexY = heightPixels / MapConst.PIXELS_PER_TILE / 2;

        // Determine offset within tile when centering tiles.
        final int centerPixelX = widthPixels / 2;
        final int centerPixelY = heightPixels / 2;
        final int offsetCenterPixelX = centerPixelX - (shiftTileIndexX * MapConst.PIXELS_PER_TILE);
        final int offsetCenterPixelY = centerPixelY - (shiftTileIndexY * MapConst.PIXELS_PER_TILE);
        assert MathUtils.isBetween(offsetCenterPixelX, 0, MapConst.PIXELS_PER_TILE) : offsetCenterPixelX;
        assert MathUtils.isBetween(offsetCenterPixelY, 0, MapConst.PIXELS_PER_TILE) : offsetCenterPixelY;

        // Determine top-left tile.
        final TileOffset centerTile = convertLatLonToTileOffset(mapCenter, zoomLevel);
        long tileIndexX = (((centerTile.getKey().getTileX() - shiftTileIndexX) + nrTiles) % nrTiles);
        long tileIndexY = (((centerTile.getKey().getTileY() - shiftTileIndexY) + nrTiles) % nrTiles);
        assert (0 <= tileIndexX) && (tileIndexX < nrTiles) : tileIndexX;
        assert (0 <= tileIndexY) && (tileIndexY < nrTiles) : tileIndexY;

        // Offset within tile may require an additional tile shift.
        final int offsetTilePixelX;
        final int offsetTilePixelY;
        if (centerTile.getOffsetX() <= offsetCenterPixelX) {
            offsetTilePixelX = (MapConst.PIXELS_PER_TILE - 1) - (offsetCenterPixelX - centerTile.getOffsetX());
            tileIndexX = ((tileIndexX + nrTiles) - 1) % nrTiles;
        } else {
            offsetTilePixelX = centerTile.getOffsetX() - offsetCenterPixelX;
        }
        assert MathUtils.isBetween(offsetTilePixelX, 0, MapConst.PIXELS_PER_TILE) : offsetTilePixelX;

        if (centerTile.getOffsetY() <= offsetCenterPixelY) {
            offsetTilePixelY = (MapConst.PIXELS_PER_TILE - 1) - (offsetCenterPixelY - centerTile.getOffsetY());
            tileIndexY = ((tileIndexY + nrTiles) - 1) % nrTiles;
        } else {
            offsetTilePixelY = centerTile.getOffsetY() - offsetCenterPixelY;
        }
        assert MathUtils.isBetween(offsetTilePixelY, 0, MapConst.PIXELS_PER_TILE) : offsetTilePixelY;

        // Constrain tile numbers for extreme coordinates.
        final long startTileIndexX = tileIndexX;
        final long startTileIndexY = tileIndexY;

        // Create top-left tile object.
        final TileKey topLeftKey = new TileKey(tileIndexX, tileIndexY, zoomLevel);
        final TileOffset topLeft = new TileOffset(topLeftKey, offsetTilePixelX, offsetTilePixelY);

        // Set colors for grid and draw map.
        tileIndexY = startTileIndexY % nrTiles;
        int seqIndexY = 0;
        int viewportPixelX = 0;
        int viewportPixelY = 0;
        int tileOffsetPixelY = topLeft.getOffsetY();
        while (viewportPixelY < heightPixels) {
            tileIndexX = startTileIndexX % nrTiles;
            int seqIndexX = 0;
            int tileOffsetPixelX = topLeft.getOffsetX();
            final int tilePixelHeight = Math.min(MapConst.PIXELS_PER_TILE, heightPixels - viewportPixelY) - tileOffsetPixelY;

            while (viewportPixelX < widthPixels) {
                final int tilePixelWidth = Math.min(MapConst.PIXELS_PER_TILE, widthPixels - viewportPixelX) - tileOffsetPixelX;

                // Create tile key.
                final TileKey tileKey = new TileKey(tileIndexX, tileIndexY, zoomLevel);

                // Get tile from cache (or load it now).
                final T img = getTile(tileKey);

                // Call "process()" method for tile.
                processor.process(seqIndexX, seqIndexY, tileKey, img, viewportPixelX, viewportPixelY,
                        tileOffsetPixelX, tileOffsetPixelY, tilePixelWidth, tilePixelHeight);

                tileOffsetPixelX = 0;
                viewportPixelX = viewportPixelX + tilePixelWidth;
                ++seqIndexX;
                tileIndexX = (tileIndexX + 1) % nrTiles;
            }
            tileOffsetPixelY = 0;
            viewportPixelX = 0;
            viewportPixelY = viewportPixelY + tilePixelHeight;
            ++seqIndexY;
            tileIndexY = (tileIndexY + 1) % nrTiles;
        }

        if (preCaching) {
            final long bufferFromIndexX = Math.max(0, startTileIndexX - bufferColumns);
            final long bufferToIndexX = Math.min(nrTiles, (tileIndexX + bufferColumns) - 1);
            final long bufferFromIndexY = Math.max(0, startTileIndexY - bufferRows);
            final long bufferToIndexY = Math.min(nrTiles, (tileIndexY + bufferRows) - 1);

            // Top.
            for (long y = bufferFromIndexY; y < startTileIndexY; ++y) {
                for (long x = bufferFromIndexX; x <= bufferToIndexX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
            }

            // Left/right.
            for (long y = startTileIndexY; y < tileIndexY; ++y) {
                for (long x = bufferFromIndexX; x < startTileIndexX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
                for (long x = tileIndexX; x <= bufferToIndexX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
            }

            // Bottom.
            for (long y = tileIndexY; y <= bufferToIndexY; ++y) {
                for (long x = bufferFromIndexX; x <= bufferToIndexX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
            }
        }
    }

    /**
     * Get all tiles for a specific viewport.
     *
     * @param widthPixels  Width of viewport in pixels.
     * @param heightPixels Height of viewport in pixels.
     * @param mapCenter    Center of map, as lat/lon.
     * @param zoomLevel    Zoomlevel (from 0..LbsConst.MapConst.MAXIMUM_ZOOM).
     * @return The returned value is a collection of viewport images. Every viewport image element is contains an image
     * to be plotted, the position from the top-left of the viewport and the crop area to plot from the image. This
     * makes it easy to crop the images at the edges correctly.
     */
    @Nonnull
    public Collection<ViewportTile<T>> collectViewportTiles(
            final int widthPixels, final int heightPixels,
            @Nonnull final GeoPoint mapCenter, final int zoomLevel) {
        assert widthPixels >= 0;
        assert heightPixels >= 0;
        assert mapCenter != null;
        assert MathUtils.isBetween(zoomLevel, 0, MapConst.MAXIMUM_ZOOM) : zoomLevel;
        assert MapConst.PIXELS_PER_TILE > 0;

        final Collection<ViewportTile<T>> tiles = new ArrayList<>();
        processViewportTiles(widthPixels, heightPixels, mapCenter, zoomLevel, new ViewportTileProcessor<T>() {

            @Override
            public void process(
                    final int seqX, final int seqY, @Nonnull final TileKey tileKey, @Nullable final T img,
                    final int viewportX, final int viewportY,
                    final int tileOffsetX, final int tileOffsetY, final int width, final int height) {
                final ViewportTile<T> viewportTile =
                        new ViewportTile<>(
                                seqX, seqY, tileKey, img,
                                viewportX, viewportY, tileOffsetX, tileOffsetY, width, height);
                tiles.add(viewportTile);
            }
        });
        final int sizeMin = (widthPixels / MapConst.PIXELS_PER_TILE) * ((heightPixels / MapConst.PIXELS_PER_TILE));
        final int sizeMax = ((widthPixels / MapConst.PIXELS_PER_TILE) + 2) * ((heightPixels / MapConst.PIXELS_PER_TILE) + 2);
        assert MathUtils.isBetween(tiles.size(), sizeMin, sizeMax) :
                tiles.size() + " not in [" + sizeMin + ", " + sizeMax + ']';
        return tiles;
    }

    /**
     * Convert a lat/lon coordinate to a map tile with an offset within the tile.
     *
     * @param point     Coordinate.
     * @param zoomLevel Zoom level.
     * @return Map tile with zoomlevel and offset.
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Nonnull
    public static TileOffset convertLatLonToTileOffset(@Nonnull final GeoPoint point, final int zoomLevel) {

        // Normalize lat/lon to 0..1.
        final MercatorPoint mercs = MercatorPoint.latLonToMercs(point);
        assert mercs != null;
        assert MathUtils.isBetween(mercs.getMercX(), 0.0, 1.0) : mercs.getMercX();
        assert MathUtils.isBetween(mercs.getMercY(), 0.0, 1.0) : mercs.getMercY();

        // Maximum number of tiles on this zoom level (same for X and Y).
        final double nrTiles = (1L << zoomLevel);

        // Determine tile X and Y.
        final long tileX = Math.min((long) (nrTiles - 1), (long) Math.floor(mercs.getMercX() * nrTiles));
        final long tileY = Math.min((long) (nrTiles - 1), (long) Math.floor(mercs.getMercY() * nrTiles));
        final TileKey key = new TileKey(tileX, tileY, zoomLevel);

        final double deltaMercX = mercs.getMercX() - ((double) tileX / nrTiles);
        final double deltaMercY = mercs.getMercY() - ((double) tileY / nrTiles);
        final long nrPixels = Math.round(nrTiles * MapConst.PIXELS_PER_TILE);
        final int offsetX = (int) Math.min(Math.round(deltaMercX * nrPixels), MapConst.PIXELS_PER_TILE);
        final int offsetY = (int) Math.min(Math.round(deltaMercY * nrPixels), MapConst.PIXELS_PER_TILE);
        assert MathUtils.isBetween(offsetX, 0, MapConst.PIXELS_PER_TILE) : offsetX;
        assert MathUtils.isBetween(offsetY, 0, MapConst.PIXELS_PER_TILE) : offsetY;

        return new TileOffset(key, offsetX, offsetY);
    }

    /**
     * Convert a map tile with zoomlevel and offset to a lat/lon coordinate.
     *
     * @param tileOffset Map tile with zoomlevel and offset within tile.
     * @return Coordinate.
     */
    @Nonnull
    public static GeoPoint convertTileOffsetToLatLon(@Nonnull final TileOffset tileOffset) {
        assert tileOffset != null;
        final int offsetX = tileOffset.getOffsetX();
        final int offsetY = tileOffset.getOffsetY();
        final int zoomLevel = tileOffset.getKey().getZoomLevel();
        final double nrTiles = (1L << zoomLevel) * MapConst.PIXELS_PER_TILE;
        final long tileX = tileOffset.getKey().getTileX() * MapConst.PIXELS_PER_TILE;
        final long tileY = tileOffset.getKey().getTileY() * MapConst.PIXELS_PER_TILE;
        final double mercX = (tileX + offsetX) / nrTiles;
        final double mercY = (tileY + offsetY) / nrTiles;
        final GeoPoint point = MercatorPoint.mercsToLatLon(mercX, mercY);
        return point;
    }

    /**
     * Convert a (X, Y) position on a screen to a latitude and longitude, given the center of the map and the
     * zoomLevel.
     *
     * @param posX      Offset in pixels from left.
     * @param posY      Offset in pixels from top.
     * @param width     Width in pixels.
     * @param height    Height in pixels.
     * @param zoomLevel Zoom level.
     * @param mapCenter Latitude and longitude of center of map.
     * @return Latitude and longitude of XY position.
     */
    @Nonnull
    public static GeoPoint convertViewportXYToLatLon(final int posX, final int posY,
                                                     final int width, final int height, final int zoomLevel,
                                                     @Nonnull final GeoPoint mapCenter) {
        assert (0 <= posX) && (posX < width);
        assert (0 <= posY) && (posY < height);
        assert width > 0;
        assert height > 0;
        assert MathUtils.isBetween(zoomLevel, MapConst.MINIMUM_ZOOM, MapConst.MAXIMUM_ZOOM) : zoomLevel;
        assert mapCenter != null;
        final double deltaX = posX - ((double) width / 2.0);
        final double deltaY = posY - ((double) height / 2.0);
        final double totalSize = (1L << zoomLevel) * MapConst.PIXELS_PER_TILE;

        final MercatorPoint mercs = MercatorPoint.latLonToMercs(mapCenter);
        final double mercX = MathUtils.limitTo(mercs.getMercX() + (deltaX / totalSize), 0.0, 1.0);
        final double mercY = MathUtils.limitTo(mercs.getMercY() + (deltaY / totalSize), 0.0, 1.0);
        final GeoPoint point = MercatorPoint.mercsToLatLon(mercX, mercY);
        return point;
    }

    /**
     * Given a viewport calculate the (x, y) position of a lat/lon. If the lat or the lon is not displayed in the
     * viewport, null is returned for that value.
     *
     * @param point     Lat/lon.
     * @param width     Width of viewport.
     * @param height    Height of viewport.
     * @param zoomLevel Zoomlevel of viewport.
     * @param mapCenter Latitude and longitude of center of map.
     * @return Returns (x, y) position in viewport. 0 &lt;= x &lt; width and 0 &lt;= y &lt; height, or null if either of the values
     * is out of range, if the lat or lon is not positioned within the viewport.
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Nullable
    public static Tuple<Integer, Integer> convertLatLonToViewportXY(
            @Nonnull final GeoPoint point,
            final int width, final int height, final int zoomLevel,
            @Nonnull final GeoPoint mapCenter) {
        assert point != null;
        assert mapCenter != null;
        assert width > 0;
        assert height > 0;
        assert MathUtils.isBetween(zoomLevel, MapConst.MINIMUM_ZOOM, MapConst.MAXIMUM_ZOOM) : zoomLevel;
        final double totalSize = (1L << zoomLevel) * MapConst.PIXELS_PER_TILE;

        final MercatorPoint mercsCenter = MercatorPoint.latLonToMercs(mapCenter);
        final MercatorPoint mercsPoint = MercatorPoint.latLonToMercs(point);

        final double deltaX = mercsCenter.getMercX() - mercsPoint.getMercX();
        final double deltaY = mercsCenter.getMercY() - mercsPoint.getMercY();

        final double centerX = width / 2.0;
        final double centerY = height / 2.0;
        final double newX = centerX - (deltaX * totalSize);
        final double newY = centerY - (deltaY * totalSize);

        // Calculate position within viewport.
        final Integer x;
        if (MathUtils.isBetween(newX, 0.0, width)) {
            x = (int) Math.floor(newX);
        } else {
            x = null;
        }

        final Integer y;
        if (MathUtils.isBetween(newY, 0.0, height)) {
            y = (int) Math.floor(newY);
        } else {
            y = null;
        }
        if ((x != null) && (y != null)) {
            return new Tuple<>(x, y);
        } else {
            return null;
        }
    }

    /**
     * Given a set of points, a viewport width/height and the border width, return a zoomlevel and map center that will
     * contain the specific viewport.
     *
     * @param width       Width of viewport.
     * @param height      Height of viewport.
     * @param borderWidth Border width (in pixels) inside viewport to keep points clear from.
     * @param points      Points to fit in viewport (but not in border).
     * @return Zoom level and map center of map to display.
     */
    @Nonnull
    public static Tuple<Integer, GeoPoint> findZoomLevelAndMapCenter(
            final int width, final int height, final int borderWidth,
            @Nonnull final Collection<GeoPoint> points) {
        assert (width - (2 * borderWidth)) > 0;
        assert (height - (2 * borderWidth)) > 0;
        assert points != null;
        assert !points.isEmpty();

        final int actualWidth = width - (2 * borderWidth);
        final int actualHeight = height - (2 * borderWidth);

        // Determine full rectangle of all points.
        final GeoPoint first = points.iterator().next();
        assert first != null;
        GeoRectangle rect = new GeoRectangle(first, first);
        for (final GeoPoint point : points) {
            assert point != null;
            rect = rect.grow(point);
        }

        final GeoPoint northEast = rect.getNorthEast();
        final GeoPoint southWest = rect.getSouthWest();

        // Search for a zoom-level until both northEast and southWest fit.
        boolean found = false;
        final GeoPoint mapCenter = rect.getCenter();
        int zoomLevel = MapConst.MAXIMUM_ZOOM + 1;
        while ((zoomLevel > MapConst.MINIMUM_ZOOM) && !found) {
            --zoomLevel;
            final Tuple<Integer, Integer> topLeft =
                    convertLatLonToViewportXY(northEast, actualWidth, actualHeight, zoomLevel, mapCenter);
            final Tuple<Integer, Integer> bottomRight =
                    convertLatLonToViewportXY(southWest, actualWidth, actualHeight, zoomLevel, mapCenter);
            found = (topLeft != null) && (bottomRight != null);
        }
        return new Tuple<>(zoomLevel, mapCenter);
    }

    /**
     * Calculate a new map center, given a delta in pixels.
     *
     * @param deltaX    Delta in pixels to left.
     * @param deltaY    Delta in pixels to bottom.
     * @param zoomLevel Zoom level.
     * @param mapCenter Latitude and longitude of center of map.
     * @return Latitude and longitude of XY position.
     */
    @Nonnull
    public static GeoPoint moveLatLonByViewportXY(
            final int deltaX, final int deltaY, final int zoomLevel,
            @Nonnull final GeoPoint mapCenter) {
        assert mapCenter != null;
        assert MathUtils.isBetween(zoomLevel, MapConst.MINIMUM_ZOOM, MapConst.MAXIMUM_ZOOM) : zoomLevel;
        final double totalSize = (1L << zoomLevel) * MapConst.PIXELS_PER_TILE;

        final MercatorPoint mercs = MercatorPoint.latLonToMercs(mapCenter);
        final double mercX = MathUtils.limitTo(mercs.getMercX() + (deltaX / totalSize), 0.0, 1.0);
        final double mercY = MathUtils.limitTo(mercs.getMercY() + (deltaY / totalSize), 0.0, 1.0);
        final GeoPoint point = MercatorPoint.mercsToLatLon(mercX, mercY);
        return point;
    }
}
