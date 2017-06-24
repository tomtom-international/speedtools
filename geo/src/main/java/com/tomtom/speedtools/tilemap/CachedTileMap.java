/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;

/**
 * This class represents a cacheable map. It uses Google Guava for its (basic) caching. Use this class to avoid a huge
 * load on the LBS system (and loads of network traffic) if you will be retrieving the same tiles over and over again.
 * The cache is stored in the heap, so you may wish to set the VM '-Xmx700m' parameter (700m is an example; 700 megs of
 * RAM).
 */
public final class CachedTileMap<T> extends TileMap<T> {

    // Default maximum number of cache elements.
    private static final int CACHE_MAX_ELEMENTS_DEFAULT = 2500;
    private static final int CACHE_BUFFER_COLUMNS = 2;
    private static final int CACHE_BUFFER_ROWS = 2;

    // Local tile cache.
    @Nonnull
    private final LoadingCache<TileKey, T> mapTiles;

    /**
     * Constructor.
     *
     * @param imgLoad        Function for loading image tiles, or default if -1.
     * @param maxCachedTiles Maximum number of tiles to be cached.
     */
    public CachedTileMap(
            final long maxCachedTiles,
            @Nonnull final CacheLoader<TileKey, T> imgLoad) {
        super(CACHE_BUFFER_COLUMNS, CACHE_BUFFER_ROWS);
        assert maxCachedTiles >= -1;
        assert imgLoad != null;
        mapTiles = CacheBuilder.newBuilder().
                recordStats().
                maximumSize((maxCachedTiles >= 0) ? maxCachedTiles : CACHE_MAX_ELEMENTS_DEFAULT).
                build(imgLoad);
    }

    /**
     * Get a map tile, given a map tile key.
     *
     * @param key Map tile key.
     * @return Map tile.
     */
    @Override
    @Nullable
    public T getTile(@Nonnull final TileKey key) {
        try {
            return mapTiles.get(key);
        } catch (final ExecutionException ignored) {
            return null;
        }
    }

    /**
     * Returns if the map supports caching.
     *
     * @return True if the map supports caching (does not say anything about the cache size).
     */
    @Override
    public boolean isCacheEnabled() {
        return true;
    }

    /**
     * Buffer a map tile, given a map tile key.
     *
     * @param key Map tile key.
     */
    @Override
    public void cacheTile(@Nonnull final TileKey key) {
        try {
            mapTiles.get(key);
        } catch (final ExecutionException ignored) {
            mapTiles.invalidate(key);
        }
    }

    // Invalidate tile in the the tile cache.
    public void invalidate(@Nonnull final TileKey key) {
        assert key != null;
        mapTiles.invalidate(key);
    }

    // Invalidate tiles in the the tile cache.
    public void invalidateAll(@Nonnull final Iterable<TileKey> keys) {
        assert keys != null;
        mapTiles.invalidateAll(keys);
    }

    // Invalidate tiles in the the tile cache.
    public void invalidateAll() {
        mapTiles.invalidateAll();
    }

    /**
     * Return cache statistics for map tiles.
     *
     * @return Cache statistics.
     */
    @Nonnull
    public CacheStats stats() {
        return mapTiles.stats();
    }
}
