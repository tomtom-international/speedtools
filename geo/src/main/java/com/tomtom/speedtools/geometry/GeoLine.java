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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Geometric line between two points, from west to east (or, for vertical vectors east=west). If west &gt; east, the
 * vector is wrapped along the long side of Earth.
 *
 * Constructor: {@link #GeoLine}
 */
@Immutable
public final class GeoLine extends GeoObject {

    @Nonnull
    private final GeoPoint southWest;
    @Nonnull
    private final GeoPoint northEast;

    /**
     * Create a line. A line is always defined from west to east. Note that if the west.longitude &gt; east.longitude
     * that the line is wrapped along the long side of the Earth.
     *
     * @param southWest Start point, west side.
     * @param northEast End point, east side, although east and west may be same longitude.
     */
    public GeoLine(
            @Nonnull final GeoPoint southWest,
            @Nonnull final GeoPoint northEast) {
        super();
        assert southWest != null;
        assert northEast != null;
        final GeoPoint lowerLeft = (southWest.getLat() <= northEast.getLat()) ?
                southWest : southWest.withLat(northEast.getLat());
        final GeoPoint upperRight = (southWest.getLat() <= northEast.getLat()) ?
                northEast : northEast.withLat(southWest.getLat());
        this.southWest = lowerLeft;
        this.northEast = upperRight;
        assert this.southWest.getLat() <= this.northEast.getLat() : "SW above NE: " + this;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private GeoLine() {
        super();
        southWest = null;
        northEast = null;
    }

    /**
     * Return origin == southWest.
     *
     * @return Origin.
     */
    @Nonnull
    @Override
    public GeoPoint getOrigin() {
        return southWest;
    }

    /**
     * Return center == Mid point of (northEast + southWest).
     *
     * @return Center.
     */
    @Nonnull
    @Override
    public GeoPoint getCenter() {
        final double lat = (southWest.getLat() + northEast.getLat()) / 2.0;
        final double lon = (((northEast.getLon() >= southWest.getLon()) ?
                (northEast.getLon()) : ((northEast.getLon() + 360.0))) +
                southWest.getLon()) / 2.0;
        return new GeoPoint(lat, lon);
    }

    /**
     * Get start point.
     *
     * @return Start point.
     */
    @Nonnull
    public GeoPoint getSouthWest() {
        return southWest;
    }

    /**
     * Get end point.
     *
     * @return End point.
     */
    @Nonnull
    public GeoPoint getNorthEast() {
        return northEast;
    }

    /**
     * Setter for {@link #getSouthWest()}.
     *
     * @param southWest Location.
     * @return New line.
     */
    @Nonnull
    public GeoLine withSouthWest(@Nonnull final GeoPoint southWest) {
        return new GeoLine(southWest, northEast);
    }

    /**
     * Setter for {@link #getNorthEast()}.
     *
     * @param northEast Location.
     * @return New line.
     */
    @Nonnull
    public GeoLine withNorthEast(@Nonnull final GeoPoint northEast) {
        return new GeoLine(southWest, northEast);
    }

    @Nonnull
    @Override
    public GeoLine translate(@Nonnull final GeoVector vector) {
        return new GeoLine(southWest.translate(vector), northEast.translate(vector));
    }

    @Nonnull
    @Override
    public GeoLine moveTo(@Nonnull final GeoPoint origin) {
        final GeoPoint p1 = origin;
        final GeoPoint p2 = origin.translate(new GeoVector(getNorthing(), getEasting()));
        return new GeoLine(p1, p2);
    }

    /**
     * Return degrees northing.
     *
     * @return Degrees northing, [0, 180].
     */
    public double getNorthing() {
        final double northing = northEast.getLat() - southWest.getLat();
        assert MathUtils.isBetween(northing, 0, 180) : "Northing not in [0, 180]: " + northing + ", " + this;
        return northing;
    }

    /**
     * Return degrees easting.
     *
     * @return Degrees easting, [0, 360).
     */
    public double getEasting() {
        final double easting;

        if (northEast.getLon() >= southWest.getLon()) {
            easting = northEast.getLon() - southWest.getLon();
        } else {
            easting = 360.0 + (northEast.getLon() - southWest.getLon());
        }
        assert (0.0 <= easting) && (easting < 360.0) : "Easting not in [0, 360): " + easting + ", " + this;
        return easting;
    }

    /**
     * Return length of line in meters (approximation).
     *
     * @return Length in meters.
     */
    public double getLengthMeters() {
        return Geo.distanceInMeters(southWest, northEast);
    }

    /**
     * Return whether the vector is wrapped around the long or short side of Earth. The 'normal case' is that vectors
     * and rectangles are wrapped along the short side indeed.
     *
     * @return True if wrapped around long side, false if wrapped along short side.
     */
    public boolean isWrappedOnLongSide() {
        return getEasting() >= 180.0;
    }

    /**
     * Return the shortest GeoLine for two points, which is never wrapped around the long side of the Earth.
     *
     * @param from From.
     * @param to   To.
     * @return Shortest connection.
     */
    @Nonnull
    public static GeoLine getShortestLine(
            @Nonnull final GeoPoint from,
            @Nonnull final GeoPoint to) {
        assert from != null;
        assert to != null;
        GeoLine shortest = new GeoLine(from, to);
        if (shortest.isWrappedOnLongSide()) {
            shortest = new GeoLine(to, from);
        }
        return shortest;
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof GeoLine;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoLine)) {
            final GeoLine that = (GeoLine) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && southWest.equals(that.southWest);
            eq = eq && northEast.equals(that.northEast);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(southWest, northEast);
    }
}
