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

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.tomtom.speedtools.services.lbs.Lbs;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.utils.MathUtils;

/**
 * Map tile keys for the TomTom LBS system.
 */
public final class TileKey {
    private final long tileX;           // LBS tile index X.
    private final long tileY;           // LBS tile index Y.
    private final int zoomLevel;       // LBS zoom-level.

    public TileKey(final long tileX, final long tileY, final int zoomLevel) {
        super();
        assert MathUtils.isBetween(zoomLevel, Lbs.MINIMUM_ZOOM, Lbs.MAXIMUM_ZOOM) : zoomLevel;
        assert (0 <= tileX) && (tileX < (1 << zoomLevel)) : tileX + ", " + zoomLevel;
        assert (0 <= tileY) && (tileY < (1 << zoomLevel)) : tileY + ", " + zoomLevel;
        this.tileX = tileX;
        this.tileY = tileY;
        this.zoomLevel = zoomLevel;
    }

    /**
     * Get tile X index.
     *
     * @return X index.
     */
    public long getTileX() {
        return tileX;
    }

    /**
     * Get tile Y index.
     *
     * @return Y index.
     */
    public long getTileY() {
        return tileY;
    }

    @Nonnull
    public TileKey withTileX(final long tileX) {
        return new TileKey(tileX, tileY, zoomLevel);
    }

    @Nonnull
    public TileKey withTileY(final long tileY) {
        return new TileKey(tileX, tileY, zoomLevel);
    }

    @Nonnull
    public TileKey withZoomLevel(final int zoomLevel) {
        return new TileKey(tileX, tileY, zoomLevel);
    }

    /**
     * Get zoomlevel.
     *
     * @return zoomlevel.
     */
    public int getZoomLevel() {
        return zoomLevel;
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof TileKey;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof TileKey)) {
            final TileKey that = (TileKey) obj;
            eq = that.canEqual(this);
            eq = eq && (tileX == that.tileX);
            eq = eq && (tileY == that.tileY);
            eq = eq && (zoomLevel == that.zoomLevel);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tileX, tileY, zoomLevel);
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}

