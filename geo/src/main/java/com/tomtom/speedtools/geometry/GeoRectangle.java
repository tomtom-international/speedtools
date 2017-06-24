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
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a rectangle.
 *
 * Constructor: {@link #GeoRectangle}
 */
@Immutable
public final class GeoRectangle extends Primitive {

    @Nonnull
    private final GeoPoint southWest;
    @Nonnull
    private final GeoPoint northEast;

    /**
     * Create a rectangular geo area. A rectangle is always defined by two points, east and west. Note that if
     * west.longitude &gt; east.longitude that the rectangle is wrapped along the long side of the Earth.
     *
     * @param southWest West point.
     * @param northEast East point.
     */
    public GeoRectangle(
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
    private GeoRectangle() {
        super();
        southWest = null;
        northEast = null;
    }

    /**
     * Return a rectangle, the size of the world (in Mercator). Meaning: lat=-90..90, and lon=-180..180.
     *
     * @return World-size rectangle.
     */
    @Nonnull
    public static GeoRectangle getWorld() {
        return new GeoRectangle(new GeoPoint(-90.0, -180.0), new GeoPoint(90.0, Geo.LON180));
    }

    @Nonnull
    @Override
    public GeoRectangle boundingBox() {
        return this;
    }

    @Nonnull
    public GeoPoint getSouthWest() {
        return southWest;
    }

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
    public GeoRectangle withSouthWest(@Nonnull final GeoPoint southWest) {
        return new GeoRectangle(southWest, northEast);
    }

    /**
     * Setter for {@link #getNorthEast()}.
     *
     * @param northEast Location.
     * @return New line.
     */
    @Nonnull
    public GeoRectangle withNorthEast(@Nonnull final GeoPoint northEast) {
        return new GeoRectangle(southWest, northEast);
    }

    /**
     * Get Northing degrees.
     *
     * @return Northing, [0, 180].
     */
    public double getNorthing() {
        final double northing = Math.abs(northEast.getLat() - southWest.getLat());
        assert MathUtils.isBetween(northing, 0, 180) : "Northing not in [0, 180]: " + northing + ", " + this;
        return northing;
    }

    /**
     * Get Easting degrees.
     *
     * @return Easting, [0, 360).
     */
    public double getEasting() {
        final double easting;
        if (southWest.getLon() <= northEast.getLon()) {
            easting = northEast.getLon() - southWest.getLon();
        } else {
            easting = 360.0 - (southWest.getLon() - northEast.getLon());
        }
        assert ((0 <= easting) && (easting < 360)) : "Easting not in [0, 360): " + easting + ", " + this;
        return easting;
    }

    @Override
    public boolean overlaps(@Nonnull final GeoArea area) {
        final GeoRectangle other = area.boundingBox();

        // Compare bounding boxes.
        final boolean overlap;
        if (isWrapped()) {
            final GeoRectangle t1 = new GeoRectangle(
                    new GeoPoint(southWest.getLat(), -180.0),
                    new GeoPoint(northEast.getLat(), northEast.getLon())
            );
            final GeoRectangle t2 = new GeoRectangle(
                    new GeoPoint(southWest.getLat(), southWest.getLon()),
                    new GeoPoint(northEast.getLat(), Geo.LON180)
            );

            if (other.isWrapped()) {
                final GeoRectangle o1 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), -180.0),
                        new GeoPoint(other.northEast.getLat(), other.northEast.getLon())
                );
                final GeoRectangle o2 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), other.southWest.getLon()),
                        new GeoPoint(other.northEast.getLat(), Geo.LON180)
                );
                overlap = t1.overlaps(o1) || t2.overlaps(o2);
            } else {
                overlap = t1.overlaps(other) || t2.overlaps(other);
            }
        } else {
            if (other.isWrapped()) {
                final GeoRectangle o1 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), -180.0),
                        new GeoPoint(other.northEast.getLat(), other.northEast.getLon())
                );
                final GeoRectangle o2 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), other.southWest.getLon()),
                        new GeoPoint(other.northEast.getLat(), Geo.LON180)
                );
                overlap = overlaps(o1) || overlaps(o2);
            } else {
                overlap = !((southWest.getLat() > other.northEast.getLat()) ||
                        (northEast.getLat() < other.southWest.getLat()) ||
                        (southWest.getLon() > other.northEast.getLon()) ||
                        (northEast.getLon() < other.southWest.getLon()));
            }
        }
        return overlap;
    }

    /**
     * Expands the rectangle by given amount of meters on each side.
     *
     * @param meters Meters to expand the rectangle with.
     * @return Expanded rectangle.
     */
    @Nonnull
    public GeoRectangle expand(final int meters) {
        final double latDelta = Geo.metersToDegreesLat(meters);
        final double southLonDelta = Geo.metersToDegreesLonAtLat(meters, southWest.getLat());
        final double northLonDelta = Geo.metersToDegreesLonAtLat(meters, northEast.getLat());
        return withSouthWest(new GeoPoint(southWest.getLat() - latDelta, southWest.getLon() - southLonDelta)).
                withNorthEast(new GeoPoint(northEast.getLat() + latDelta, northEast.getLon() + northLonDelta));
    }

    @Override
    public boolean contains(@Nonnull final GeoArea area) {
        final GeoRectangle other = area.boundingBox();

        // Compare bounding boxes.
        final boolean contain;
        if (isWrapped()) {
            final GeoRectangle t1 = new GeoRectangle(
                    new GeoPoint(southWest.getLat(), -180.0),
                    new GeoPoint(northEast.getLat(), northEast.getLon())
            );
            final GeoRectangle t2 = new GeoRectangle(
                    new GeoPoint(southWest.getLat(), southWest.getLon()),
                    new GeoPoint(northEast.getLat(), Geo.LON180)
            );

            if (other.isWrapped()) {
                final GeoRectangle o1 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), -180.0),
                        new GeoPoint(other.northEast.getLat(), other.northEast.getLon())
                );
                final GeoRectangle o2 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), other.southWest.getLon()),
                        new GeoPoint(other.northEast.getLat(), Geo.LON180)
                );
                contain = t1.contains(o1) && t2.contains(o2);
            } else {
                contain = t1.contains(other) || t2.contains(other);
            }
        } else {
            if (other.isWrapped()) {
                final GeoRectangle o1 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), -180.0),
                        new GeoPoint(other.northEast.getLat(), northEast.getLon())
                );
                final GeoRectangle o2 = new GeoRectangle(
                        new GeoPoint(other.southWest.getLat(), other.southWest.getLon()),
                        new GeoPoint(other.northEast.getLat(), Geo.LON180)
                );
                contain = contains(o1) && contains(o2);
            } else {
                contain = (southWest.getLat() <= other.southWest.getLat()) &&
                        (northEast.getLat() >= other.northEast.getLat()) &&
                        (southWest.getLon() <= other.southWest.getLon()) &&
                        (northEast.getLon() >= other.northEast.getLon());
            }
        }
        return contain;
    }

    @Override
    public boolean contains(@Nonnull final GeoPoint point) {
        return contains(new GeoRectangle(point, point));
    }

    @Nonnull
    @Override
    public GeoRectangle translate(@Nonnull final GeoVector vector) {
        return new GeoRectangle(southWest.translate(vector), northEast.translate(vector));
    }

    @Nonnull
    @Override
    public GeoRectangle moveTo(@Nonnull final GeoPoint origin) {
        return new GeoRectangle(origin, origin.translate(new GeoVector(getNorthing(), getEasting())));
    }

    @Override
    @Nonnull
    public Collection<GeoRectangle> pixelate() {
        final Collection<GeoRectangle> rects = new ArrayList<>();
        if (isWrapped()) {
            final GeoRectangle west = new GeoRectangle(
                    new GeoPoint(southWest.getLat(), -180.0), northEast);
            final GeoRectangle east = new GeoRectangle(
                    southWest, new GeoPoint(northEast.getLat(), Geo.LON180));
            rects.add(west);
            rects.add(east);
        } else {
            rects.add(this);
        }
        assert rects.size() >= 1;
        return rects;
    }

    /**
     * Create a rectangle that contains another point. The rectangle is always grown with the smallest rectangle
     * possible.
     *
     * @param point Point to contain.
     * @return New rectangle, containing this and point.
     */
    @Nonnull
    public GeoRectangle grow(@Nonnull final GeoPoint point) {
        final double newSouthWestLat = Math.min(southWest.getLat(), point.getLat());
        final double newNorthEastLat = Math.max(northEast.getLat(), point.getLat());

        final double newSouthWestLon1;
        final double newNorthEastLon1;

        final double newSouthWestLon2;
        final double newNorthEastLon2;

        // There are two way to grow the rectangle.
        if (isWrapped()) {
            newSouthWestLon1 = Math.min(southWest.getLon(), point.getLon());
            newNorthEastLon1 = Math.min(northEast.getLon(), point.getLon());

            newSouthWestLon2 = Math.max(southWest.getLon(), point.getLon());
            newNorthEastLon2 = Math.max(northEast.getLon(), point.getLon());
        } else {
            newSouthWestLon1 = Math.min(southWest.getLon(), point.getLon());
            newNorthEastLon1 = Math.max(northEast.getLon(), point.getLon());

            newSouthWestLon2 = Math.max(southWest.getLon(), point.getLon());
            newNorthEastLon2 = Math.min(northEast.getLon(), point.getLon());
        }

        // Simply try both rectangles.
        final GeoRectangle rect1 = new GeoRectangle(
                new GeoPoint(newSouthWestLat, newSouthWestLon1),
                new GeoPoint(newNorthEastLat, newNorthEastLon1));
        final GeoRectangle rect2 = new GeoRectangle(
                new GeoPoint(newSouthWestLat, newSouthWestLon2),
                new GeoPoint(newNorthEastLat, newNorthEastLon2));
        final GeoRectangle rect3 = new GeoRectangle(
                southWest.withLat(newSouthWestLat),
                northEast.withLat(newNorthEastLat));

        // Determine which one produced the smallest result, larger than 'this'.
        final GeoRectangle smallest;
        if (rect1.getEasting() <= rect2.getEasting()) {
            if (rect1.getEasting() > getEasting()) {
                smallest = rect1;
            } else {
                smallest = rect3;
            }

        } else {
            if (rect2.getEasting() > getEasting()) {
                smallest = rect2;
            } else {
                smallest = rect3;
            }
        }
        return smallest;
    }

    /**
     * Create a rectangle that contains both this and another rectangle.
     *
     * @param other Other rectangle.
     * @return New rectangle, containing this and other rectangle.
     */
    @Nonnull
    public GeoRectangle grow(@Nonnull final GeoRectangle other) {
        final GeoRectangle rect1 = grow(other.getSouthWest());
        final GeoRectangle rect2 = rect1.grow(other.getNorthEast());
        return rect2;
    }

    /**
     * Return whether the rectangle is wrapped around the long or short side of Earth. The 'normal case' is that vectors
     * and rectangles are wrapped along the short side indeed.
     *
     * @return True if wrapped around long side, false if wrapped along short side.
     */
    public boolean isWrapped() {
        return southWest.getLon() > northEast.getLon();
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof GeoRectangle;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoRectangle)) {
            final GeoRectangle that = (GeoRectangle) obj;
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
