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

package com.tomtom.speedtools.geometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;

/**
 * Unary expression.
 *
 * Note: This class is package private.
 *
 * Constructor: {@link #UnaryExpr}
 */
@Immutable
abstract class UnaryExpr extends GeoArea {
    @Nonnull
    protected final GeoArea op;

    /**
     * Create a unary geo expression.
     *
     * @param op Operand.
     */
    UnaryExpr(@Nonnull final GeoArea op) {
        super();
        assert op != null;
        this.op = op;
    }

    @Override
    @Nonnull
    public Collection<GeoRectangle> pixelate() {
        return op.pixelate();
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof UnaryExpr;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof UnaryExpr)) {
            final UnaryExpr that = (UnaryExpr) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && op.equals(that.op);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(op);
    }
}
