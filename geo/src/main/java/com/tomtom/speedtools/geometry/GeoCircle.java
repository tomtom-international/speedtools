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

package com.tomtom.speedtools.geometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents circle primitive as a GeoPoint and a radius. The radius is specified in degrees and is only valid for
 * smaller areas.
 *
 * Constructor: {@link #GeoCircle}
 */
@Immutable
public final class GeoCircle extends Primitive {

    @Nonnull
    private final GeoPoint center;
    @Nonnull
    private final Double radiusMeters;

    /**
     * Create a circle geo area.
     *
     * @param center       Center.
     * @param radiusMeters Radius, in meters. Must be &gt;= 0.
     */
    public GeoCircle(
            @Nonnull final GeoPoint center,
            @Nonnull final Double radiusMeters) {
        super();
        assert center != null;
        assert radiusMeters != null;
        assert radiusMeters >= 0.0 : "Radius must be >= 0: " + radiusMeters;
        this.center = center;
        this.radiusMeters = radiusMeters;
    }

    /**
     * Create a circle geo area with 2 points: one is the center, the other a point on the circle.
     *
     * @param center Center.
     * @param point  Point in circle.
     */
    public GeoCircle(
            @Nonnull final GeoPoint center,
            @Nonnull final GeoPoint point) {
        super();
        assert center != null;
        assert point != null;
        final double lat = Math.abs(center.getLat() - point.getLat());
        final double lon = Math.abs(center.getLon() - point.getLon());
        final double w = Geo.degreesLatToMeters(lat);
        final double h = Geo.degreesLonToMetersAtLat(lon, (center.getLat() + point.getLat()) / 2.0);
        final double radius = Math.sqrt((w * w) + (h * h));
        this.center = center;
        this.radiusMeters = radius;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private GeoCircle() {
        super();
        center = null;
        radiusMeters = null;
    }

    /**
     * Get center.
     *
     * @return Center.
     */
    @Override
    @Nonnull
    public GeoPoint getCenter() {
        return center;
    }

    /**
     * Get radius (in meters).
     *
     * @return Radius in meters.
     */
    @Nonnull
    public Double getRadiusMeters() {
        return radiusMeters;
    }

    /**
     * Setter for {@link #getCenter()}.
     *
     * @param center Center.
     * @return New circle.
     */
    @Nonnull
    public GeoCircle withCenter(@Nonnull final GeoPoint center) {
        return new GeoCircle(center, radiusMeters);
    }

    /**
     * Setter for {@link #getRadiusMeters()}.
     *
     * @param radiusMeters Radius.
     * @return New circle.
     */
    @Nonnull
    public GeoCircle withRadiusMeters(@Nonnull final Double radiusMeters) {
        return new GeoCircle(center, radiusMeters);
    }

    @Override
    public boolean overlaps(@Nonnull final GeoArea area) {
        final boolean overlap = area.boundingBox().overlaps(boundingBox());
        return overlap;
    }

    @Override
    public boolean contains(@Nonnull final GeoArea area) {
        return boundingBox().contains(area);
    }

    @Override
    public boolean contains(@Nonnull final GeoPoint point) {
        return contains(new GeoRectangle(point, point));
    }

    /**
     * The bounding box (or 'outer bounding box') of a circle is the rectangle that lies encloses the circle and touches
     * it at 4 points.
     *
     * @return Outer bounding box.
     */
    @Override
    @Nonnull
    public GeoRectangle boundingBox() {
        return calcBoundingBox(1.0);
    }

    @Nonnull
    @Override
    public GeoCircle translate(@Nonnull final GeoVector vector) {
        return new GeoCircle(center.translate(vector), radiusMeters);
    }

    @Nonnull
    @Override
    public GeoCircle moveTo(@Nonnull final GeoPoint origin) {
        return new GeoCircle(origin, radiusMeters);
    }

    @Override
    @Nonnull
    public Collection<GeoRectangle> pixelate() {
        final Collection<GeoRectangle> rects = new ArrayList<>();
        rects.addAll(boundingBox().pixelate());
        assert rects.size() >= 1;
        return rects;
    }

    /**
     * The inner bounding box of a circle is the rectangle that lies entirely within the circle and touches it at 4
     * points.
     *
     * @return Inner bounding box.
     */
    @Nonnull
    public GeoRectangle innerBoundingBox() {
        final double cos45 = 0.707106781186548d;
        return calcBoundingBox(cos45);
    }

    @Nonnull
    private GeoRectangle calcBoundingBox(final double c) {
        final double lowerLeftLat = center.getLat() - (Geo.metersToDegreesLat(radiusMeters) * c);
        final double lowerLeftLon = center.getLon() - (Geo.metersToDegreesLonAtLat(radiusMeters, center.getLat()) * c);
        final double upperRightLat = center.getLat() + (Geo.metersToDegreesLat(radiusMeters) * c);
        final double upperRightLon = center.getLon() + Geo.metersToDegreesLonAtLat(radiusMeters, center.getLat() * c);
        return new GeoRectangle(
                new GeoPoint(lowerLeftLat, lowerLeftLon),
                new GeoPoint(upperRightLat, upperRightLon));
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof GeoCircle;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoCircle)) {
            final GeoCircle that = (GeoCircle) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && center.equals(that.center);
            eq = eq && radiusMeters.equals(that.radiusMeters);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(center, radiusMeters);
    }
}
