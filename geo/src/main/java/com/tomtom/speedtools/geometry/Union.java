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

/**
 * Binary expression: Union.
 *
 * Note: This class is package private.
 *
 * Constructor: {@link #Union}
 */
@Immutable
final class Union extends BinaryExpr {

    /**
     * Create the union geo area between the left and right operand. This is sum of the left geo area and the right geo
     * area.
     *
     * For example (in numbers):
     *
     * Union({1, 2, 3}, {3, 4, 5}) == {1, 2, 3, 4, 5}
     *
     * If left == Nil, the result is always right. If right == Nil, the result is always left.
     *
     * @param opLeft  Left operand.
     * @param opRight Right operand.
     */
    Union(
            @Nonnull final GeoArea opLeft,
            @Nonnull final GeoArea opRight) {
        super(opLeft, opRight);
        assert opLeft != null;
        assert opRight != null;
    }

    @Override
    public boolean overlaps(@Nonnull final GeoArea area) {
        return opLeft.overlaps(area) || opRight.overlaps(area);
    }

    @Override
    public boolean contains(@Nonnull final GeoArea area) {
        /**
         * Note that the formula is not just:
         *   contains := opLeft.contains(area) || opRight.contains(area)
         * Because 'area' may partially be contained in left and right!
         *
         * It is also not:
         *   contains := boundingBox.contains(area)
         * Because the left and right areas may be disjunct.
         *
         * This is why we combine the two approaches.
         */
        if (opLeft.overlaps(opRight)) {

            // If the left and right areas touch/overlap, then the bounding box should contain area.
            return boundingBox().contains(area);
        } else {

            // If they do not overlap, either one must fully contain area.
            return opLeft.contains(area) || opRight.contains(area);
        }
    }

    @Override
    public boolean contains(@Nonnull final GeoPoint point) {
        return opLeft.contains(point) || opRight.contains(point);
    }

    @Nonnull
    @Override
    public Union translate(@Nonnull final GeoVector vector) {
        return new Union(opLeft.translate(vector), opRight.translate(vector));
    }

    @Nonnull
    @Override
    public Union moveTo(@Nonnull final GeoPoint origin) {
        final double northing = origin.getLat() - boundingBox().getSouthWest().getLat();
        final double easting = origin.getLon() - boundingBox().getSouthWest().getLon();
        final GeoVector vector = new GeoVector(northing, easting);
        return new Union(opLeft.translate(vector), opRight.translate(vector));
    }

    @Override
    @Nonnull
    public GeoRectangle boundingBox() {
        final GeoRectangle a = opLeft.boundingBox();
        final GeoRectangle b = opRight.boundingBox();

        // Return a bounding box that contains a and b.
        return a.grow(b);
    }

    @Override
    @Nonnull
    protected GeoArea optimize() {
        if (opLeft.contains(opRight)) {

            // Right is entirely contained within left, so return left.
            return opLeft.optimize();
        } else if (opRight.contains(opLeft)) {

            // Left is entirely contained within right, so return right.
            return opRight.optimize();
        } else {

            // Just return this.
            return this;
        }
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof Union;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Union)) {
            final Union that = (Union) obj;
            eq = that.canEqual(this);
            eq = eq && super.equals(that);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
