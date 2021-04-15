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

import com.google.common.base.Objects;
import com.tomtom.speedtools.json.Json;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class used to specify a plot position and crop area for images.
 */
public final class ViewportTile<T> {
    public final int seqX;        // Sequence number X for image (0, 1, 2, ...), 0 = left.
    public final int seqY;        // Sequence number Y for image (0, 1, 2, ...), 0 = top.
    @Nonnull
    public final TileKey tileKey;
    @Nullable
    public final T img;
    public final int viewportX;   // Top-left position of image in viewport.
    public final int viewportY;
    public final int tileOffsetX; // Crop to be taken from image.
    public final int tileOffsetY;
    public final int width;       // Width/height of area to be plotted/cropped.
    public final int height;

    ViewportTile(
            final int seqX, final int seqY,
            @Nonnull final TileKey tileKey,
            @Nullable final T img,
            final int viewportX, final int viewportY,
            final int tileOffsetX, final int tileOffsetY, final int width, final int height) {
        assert seqX >= 0;
        assert seqY >= 0;
        assert viewportX >= 0;
        assert viewportY >= 0;
        assert ((0 <= tileOffsetX) && (tileOffsetX < MapConst.PIXELS_PER_TILE)) : tileOffsetX;
        assert ((0 <= tileOffsetY) && (tileOffsetY < MapConst.PIXELS_PER_TILE)) : tileOffsetY;
        assert ((0 <= width) && ((tileOffsetX + width) <= MapConst.PIXELS_PER_TILE)) : tileOffsetX + ", " + width;
        assert ((0 <= height) && ((tileOffsetY + height) <= MapConst.PIXELS_PER_TILE)) : tileOffsetY + ", " + height;
        this.seqX = seqX;
        this.seqY = seqY;
        this.tileKey = tileKey;
        this.img = img;
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.tileOffsetX = tileOffsetX;
        this.tileOffsetY = tileOffsetY;
        this.width = width;
        this.height = height;
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof ViewportTile;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof ViewportTile)) {
            final ViewportTile<?> that = (ViewportTile) obj;
            eq = that.canEqual(this);
            eq = eq && (seqX == that.seqX);
            eq = eq && (seqY == that.seqY);
            eq = eq && tileKey.equals(that.tileKey);
            eq = eq && Objects.equal(img, that.img);
            eq = eq && (viewportX == that.viewportX);
            eq = eq && (viewportY == that.viewportY);
            eq = eq && (tileOffsetX == that.tileOffsetX);
            eq = eq && (tileOffsetY == that.tileOffsetY);
            eq = eq && (width == that.width);
            eq = eq && (height == that.height);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seqX, seqY, tileKey, img,
                viewportX, viewportY, tileOffsetX, tileOffsetY, width, height);
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
