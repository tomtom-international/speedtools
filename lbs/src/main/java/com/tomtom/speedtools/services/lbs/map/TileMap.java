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

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.geometry.GeoRectangle;
import com.tomtom.speedtools.objects.Tuple;
import com.tomtom.speedtools.services.lbs.Lbs;
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
     * @param zoomLevel    Zoomlevel (from 0..LbsConst.MAXIMUM_ZOOM).
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
        assert MathUtils.isBetween(zoomLevel, 0, Lbs.MAXIMUM_ZOOM) : zoomLevel;
        assert Lbs.PIXELS_PER_TILE > 0;
        assert processor != null;

        // Calculate total number of tiles on this zoomlevel.
        final long nrTiles = (1L << zoomLevel);

        // Determine how many tiles top-left tile should shift to center the map.
        final int shiftTileX = (widthPixels / Lbs.PIXELS_PER_TILE) / 2;
        final int shiftTileY = (heightPixels / Lbs.PIXELS_PER_TILE) / 2;

        // Determine offset within tile when centering tiles.
        final int centerX = widthPixels / 2;
        final int centerY = heightPixels / 2;
        final int offsetCenterX = centerX - (shiftTileX * Lbs.PIXELS_PER_TILE);
        final int offsetCenterY = centerY - (shiftTileY * Lbs.PIXELS_PER_TILE);
        assert MathUtils.isBetween(offsetCenterX, 0, Lbs.PIXELS_PER_TILE) : offsetCenterX;
        assert MathUtils.isBetween(offsetCenterY, 0, Lbs.PIXELS_PER_TILE) : offsetCenterY;

        // Determine top-left tile.
        final TileOffset centerTile =
                convertLatLonToTileOffset(mapCenter, zoomLevel);
        long tileX = Math.max(0, centerTile.getKey().getTileX() - shiftTileX);
        long tileY = Math.max(0, centerTile.getKey().getTileY() - shiftTileY);
        assert tileX >= 0 : tileX;
        assert tileY >= 0 : tileY;

        // Offset within tile may require an additional tile shift.
        final int offsetTileX;
        final int offsetTileY;
        if (centerTile.getOffsetX() <= offsetCenterX) {
            if (tileX > 0) {
                offsetTileX = (Lbs.PIXELS_PER_TILE - 1) - (offsetCenterX - centerTile.getOffsetX());
                --tileX;
            } else {
                offsetTileX = 0;
            }
        } else {
            offsetTileX = centerTile.getOffsetX() - offsetCenterX;
        }
        assert MathUtils.isBetween(offsetTileX, 0, Lbs.PIXELS_PER_TILE) : offsetTileX;

        if (centerTile.getOffsetY() <= offsetCenterY) {
            if (tileY > 0) {
                offsetTileY = (Lbs.PIXELS_PER_TILE - 1) - (offsetCenterY - centerTile.getOffsetY());
                --tileY;
            } else {
                offsetTileY = 0;
            }
        } else {
            offsetTileY = centerTile.getOffsetY() - offsetCenterY;
        }
        assert MathUtils.isBetween(offsetTileY, 0, Lbs.PIXELS_PER_TILE) : offsetTileY;

        // Constrain tile numbers for extreme coordinates.
        final long startTileIndexX = Math.min(Math.max(tileX, 0), nrTiles);
        final long startTileIndexY = Math.min(Math.max(tileY, 0), nrTiles);

        // Create top-left tile object.
        final TileKey topLeftKey = new TileKey(tileX, tileY, zoomLevel);
        final TileOffset topLeft =
                new TileOffset(topLeftKey, offsetTileX, offsetTileY);

        // Set colors for grid and draw map.
        tileY = startTileIndexY;
        int seqY = 0;
        int viewportX = 0;
        int viewportY = 0;
        int tileOffsetY = topLeft.getOffsetY();
        while (viewportY < heightPixels) {
            tileX = startTileIndexX;
            int seqX = 0;
            int tileOffsetX = topLeft.getOffsetX();
            final int tileHeight = Math.min(Lbs.PIXELS_PER_TILE, heightPixels - viewportY) - tileOffsetY;

            while (viewportX < widthPixels) {
                final int tileWidth = Math.min(Lbs.PIXELS_PER_TILE, widthPixels - viewportX) - tileOffsetX;

                // Create tile key.
                tileX = tileX % nrTiles;
                tileY = tileY % nrTiles;
                final TileKey tileKey = new TileKey(tileX, tileY, zoomLevel);

                // Get tile from cache (or load it now).
                final T img = getTile(tileKey);

                // Call "process()" method for tile.
                processor.process(seqX, seqY, tileKey, img, viewportX, viewportY,
                        tileOffsetX, tileOffsetY, tileWidth, tileHeight);

                tileOffsetX = 0;
                viewportX = viewportX + tileWidth;
                ++seqX;
                ++tileX;
            }
            tileOffsetY = 0;
            viewportX = 0;
            viewportY = viewportY + tileHeight;
            ++seqY;
            ++tileY;
        }

        if (preCaching) {
            final long bufferFromX = Math.max(0, startTileIndexX - bufferColumns);
            final long bufferToX = Math.min(nrTiles, (tileX + bufferColumns) - 1);
            final long bufferFromY = Math.max(0, startTileIndexY - bufferRows);
            final long bufferToY = Math.min(nrTiles, (tileY + bufferRows) - 1);

            // Top.
            for (long y = bufferFromY; y < startTileIndexY; ++y) {
                for (long x = bufferFromX; x <= bufferToX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
            }

            // Left/right.
            for (long y = startTileIndexY; y < tileY; ++y) {
                for (long x = bufferFromX; x < startTileIndexX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
                for (long x = tileX; x <= bufferToX; ++x) {
                    final TileKey key = new TileKey(x % nrTiles, y % nrTiles, zoomLevel);
                    cacheTile(key);
                }
            }

            // Bottom.
            for (long y = tileY; y <= bufferToY; ++y) {
                for (long x = bufferFromX; x <= bufferToX; ++x) {
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
     * @param zoomLevel    Zoomlevel (from 0..LbsConst.MAXIMUM_ZOOM).
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
        assert MathUtils.isBetween(zoomLevel, 0, Lbs.MAXIMUM_ZOOM) : zoomLevel;
        assert Lbs.PIXELS_PER_TILE > 0;

        final Collection<ViewportTile<T>> tiles =
                new ArrayList<>();
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
        final int sizeMin = (widthPixels / Lbs.PIXELS_PER_TILE) * ((heightPixels / Lbs.PIXELS_PER_TILE));
        final int sizeMax = ((widthPixels / Lbs.PIXELS_PER_TILE) + 2) * ((heightPixels / Lbs.PIXELS_PER_TILE) + 2);
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
        assert MathUtils.isBetween(mercs.mercX, 0.0, 1.0) : mercs.mercX;
        assert MathUtils.isBetween(mercs.mercY, 0.0, 1.0) : mercs.mercY;

        // Maximum number of tiles on this zoom level (same for X and Y).
        final double nrTiles = (1L << zoomLevel);

        // Determine tile X and Y.
        final long tileX = Math.min((long) (nrTiles - 1), (long) Math.floor(mercs.mercX * nrTiles));
        final long tileY = Math.min((long) (nrTiles - 1), (long) Math.floor(mercs.mercY * nrTiles));
        final TileKey key = new TileKey(tileX, tileY, zoomLevel);

        final double deltaMercX = mercs.mercX - ((double) tileX / nrTiles);
        final double deltaMercY = mercs.mercY - ((double) tileY / nrTiles);
        final long nrPixels = Math.round(nrTiles * Lbs.PIXELS_PER_TILE);
        final int offsetX = (int) Math.min(Math.round(deltaMercX * nrPixels), Lbs.PIXELS_PER_TILE);
        final int offsetY = (int) Math.min(Math.round(deltaMercY * nrPixels), Lbs.PIXELS_PER_TILE);
        assert MathUtils.isBetween(offsetX, 0, Lbs.PIXELS_PER_TILE) : offsetX;
        assert MathUtils.isBetween(offsetY, 0, Lbs.PIXELS_PER_TILE) : offsetY;

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
        final double nrTiles = (1L << zoomLevel) * Lbs.PIXELS_PER_TILE;
        final long tileX = tileOffset.getKey().getTileX() * Lbs.PIXELS_PER_TILE;
        final long tileY = tileOffset.getKey().getTileY() * Lbs.PIXELS_PER_TILE;
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
        assert MathUtils.isBetween(zoomLevel, Lbs.MINIMUM_ZOOM, Lbs.MAXIMUM_ZOOM) : zoomLevel;
        assert mapCenter != null;
        final double deltaX = posX - ((double) width / 2.0);
        final double deltaY = posY - ((double) height / 2.0);
        final double totalSize = (1L << zoomLevel) * Lbs.PIXELS_PER_TILE;

        final MercatorPoint mercs = MercatorPoint.latLonToMercs(mapCenter);
        final double mercX = MathUtils.limitTo(mercs.mercX + (deltaX / totalSize), 0.0, 1.0);
        final double mercY = MathUtils.limitTo(mercs.mercY + (deltaY / totalSize), 0.0, 1.0);
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
     * is out ofrange, if the lat or lon is not positioned within the viewport.
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
        assert MathUtils.isBetween(zoomLevel, Lbs.MINIMUM_ZOOM, Lbs.MAXIMUM_ZOOM) : zoomLevel;
        final double totalSize = (1L << zoomLevel) * Lbs.PIXELS_PER_TILE;

        final MercatorPoint mercsCenter = MercatorPoint.latLonToMercs(mapCenter);
        final MercatorPoint mercsPoint = MercatorPoint.latLonToMercs(point);

        final double deltaX = mercsCenter.mercX - mercsPoint.mercX;
        final double deltaY = mercsCenter.mercY - mercsPoint.mercY;

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
        int zoomLevel = Lbs.MAXIMUM_ZOOM + 1;
        while ((zoomLevel > Lbs.MINIMUM_ZOOM) && !found) {
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
        assert MathUtils.isBetween(zoomLevel, Lbs.MINIMUM_ZOOM, Lbs.MAXIMUM_ZOOM) : zoomLevel;
        final double totalSize = (1L << zoomLevel) * Lbs.PIXELS_PER_TILE;

        final MercatorPoint mercs = MercatorPoint.latLonToMercs(mapCenter);
        final double mercX = MathUtils.limitTo(mercs.mercX + (deltaX / totalSize), 0.0, 1.0);
        final double mercY = MathUtils.limitTo(mercs.mercY + (deltaY / totalSize), 0.0, 1.0);
        final GeoPoint point = MercatorPoint.mercsToLatLon(mercX, mercY);
        return point;
    }
}
