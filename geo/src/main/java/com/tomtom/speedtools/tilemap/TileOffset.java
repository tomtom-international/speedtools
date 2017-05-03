/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import com.google.common.base.Objects;
import com.tomtom.speedtools.json.Json;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Point within a map tile, given a map tile key.
 */
public final class TileOffset {
    @Nonnull
    private final TileKey key;     // Tile index.
    private final int offsetX;     // Pixel offset right from south-west corner.
    private final int offsetY;     // Pixel offset up from south-west corner.

    /**
     * Create a map tile key with a zoom level and offset within the tile.
     *
     * @param key     Map tile key (X/Y index and zoom level).
     * @param offsetX Offset X within the tile.
     * @param offsetY Offset Y within the tile.
     */
    TileOffset(
            @Nonnull final TileKey key,
            final int offsetX,
            final int offsetY) {
        super();
        assert key != null;
        assert offsetX >= 0;
        assert offsetY >= 0;

        this.key = key;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Nonnull
    public TileKey getKey() {
        return key;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    @Nonnull
    public TileOffset withKey(@Nonnull final TileKey key) {
        return new TileOffset(key, offsetX, offsetY);
    }

    @Nonnull
    public TileOffset withOffsetX(final int offsetX) {
        return new TileOffset(key, offsetX, offsetY);
    }

    @Nonnull
    public TileOffset withOffsetY(final int offsetY) {
        return new TileOffset(key, offsetX, offsetY);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof TileOffset;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof TileOffset)) {
            final TileOffset that = (TileOffset) obj;
            eq = that.canEqual(this);
            eq = eq && key.equals(that.key);
            eq = eq && (offsetX == that.offsetX);
            eq = eq && (offsetY == that.offsetY);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, offsetX, offsetY);
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}

