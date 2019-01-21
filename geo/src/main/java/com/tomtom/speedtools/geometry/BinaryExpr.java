/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.geometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;

/**
 * Binary expression for geometry operations.
 *
 * Note: This class is package private.
 *
 * Constructor: {@link #BinaryExpr}
 */
@Immutable
abstract class BinaryExpr extends GeoArea {
    @Nonnull
    protected final GeoArea opLeft;
    @Nonnull
    protected final GeoArea opRight;

    /**
     * Create a binary geo area expression.
     *
     * @param opLeft  Left operand.
     * @param opRight Right operand.
     */
    BinaryExpr(
            @Nonnull final GeoArea opLeft,
            @Nonnull final GeoArea opRight) {
        super();
        assert opLeft != null;
        assert opRight != null;
        this.opLeft = opLeft;
        this.opRight = opRight;
    }

    @Override
    public boolean isCompound() {
        return true;
    }

    @Override
    @Nonnull
    public Collection<GeoRectangle> pixelate() {
        final Collection<GeoRectangle> rects = opLeft.pixelate();
        rects.addAll(opRight.pixelate());
        assert rects.size() >= 1;
        return rects;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof BinaryExpr)) {
            final BinaryExpr that = (BinaryExpr) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && opLeft.equals(that.opLeft);
            eq = eq && opRight.equals(that.opRight);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(opLeft, opRight);
    }
}
