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
 * Geometric point, 2D or 3D. A geometric point is specified in latitude, longitude and
 * an optional elevation. If the elevation is not supplied, it is not assumed to be 0,
 * but it is assumed to be really absent (and will, for example, not be output in
 * string conversions either).
 *
 * Constructor: {@link #GeoPoint}
 */
@Immutable
public final class GeoPoint extends GeoObject {

    @Nonnull
    private final Double lat;
    @Nonnull
    private final Double lon;
    @Nullable
    private final Double elevationMeters;   // Null if absent.

    /**
     * Create a 2D or 3D point.
     *
     * @param lat             Latitude (North/South), any range, will be wrapped to [-90, 90].
     * @param lon             Longitude (West/East), must be [-180, 180).
     * @param elevationMeters Elevation in meters. If null or NaN, no elevation is supplied.
     */
    public GeoPoint(
            @Nonnull final Double lat,
            @Nonnull final Double lon,
            @Nullable final Double elevationMeters) {
        super();
        assert lat != null;
        assert lon != null;
        assert MathUtils.isBetween(lat, -90.0, 90.0) : "Latitude not in [-90, 90]: " + lat;
        this.lat = lat;
        this.lon = Geo.mapToLon(lon);
        this.elevationMeters = (elevationMeters == null) ? null : ((elevationMeters.equals(Double.NaN)) ? null : elevationMeters);
    }

    /**
     * Create a 2D point. Elevation is assumed to be absent.
     *
     * @param lat Latitude (North/South), any range, will be wrapped to [-90, 90].
     * @param lon Longitude (West/East), must be [-180, 180).
     */
    public GeoPoint(
            @Nonnull final Double lat,
            @Nonnull final Double lon) {
        this(lat, lon, null);
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private GeoPoint() {
        super();
        lat = null;
        lon = null;
        elevationMeters = null;
    }

    /**
     * Return origin of point == point.
     *
     * @return Origin.
     */
    @Nonnull
    @Override
    public GeoPoint getOrigin() {
        return this;
    }

    /**
     * Return center of point == point.
     *
     * @return Center.
     */
    @Nonnull
    @Override
    public GeoPoint getCenter() {
        return this;
    }

    /**
     * Get latitude (range [-90, 90]).
     *
     * @return Latitude (North-South angle).
     */
    @Nonnull
    public Double getLat() {
        return lat;
    }

    /**
     * Get longitude (range [-180, 180)).
     *
     * @return Longitude (West-East angle).
     */
    @Nonnull
    public Double getLon() {
        return lon;
    }

    /**
     * Get elevation (in meters), or NaN if the elevation is absent.
     * Note that the return cannot be null (but it can be NaN)!
     * This means you can use this getter in expressions like this:
     *
     * {code}
     * GeoPoint p = p1.getElevationMetersOrNaN() + p2.getElevationMetersOrNan()
     * {code}
     *
     * Using the 'orNan'-getter means 'p' is a valid double, even if 'p1' or 'p2' has no
     * elevation and returns NaN (in which case 'p' will also become NaN).
     *
     * Using the regular getter, you would be forced to checked for 'null' for every
     * elevation.
     *
     * @return Elevation in meters, or NaN if absent. This return value is never null! This allows
     * the caller to "do the math", like adding or averaging elevations, even if they don't exist
     * for all points, because once NaN is used in an expression, the result will be NaN as well.
     */
    @Nonnull
    public Double getElevationMetersOrNaN() {
        return (elevationMeters == null) ? Double.NaN : elevationMeters;
    }

    /**
     * Get elevation (in meters).
     * Normally, you should probably use {@link #getElevationMetersOrNaN()}.
     *
     * @return Elevation in meters, or null, if no elevation is present.
     */
    @Nullable
    public Double getElevationMeters() {
        return elevationMeters;
    }

    /**
     * Setter for {@link #getLat()}.
     *
     * @param lat Latitude.
     * @return New point.
     */
    @Nonnull
    public GeoPoint withLat(@Nonnull final Double lat) {
        return new GeoPoint(lat, lon, elevationMeters);
    }

    /**
     * Setter for {@link #getLon()}.
     *
     * @param lon Longitude.
     * @return New point.
     */
    @Nonnull
    public GeoPoint withLon(@Nonnull final Double lon) {
        return new GeoPoint(lat, lon, elevationMeters);
    }

    /**
     * Setter for {@link #getElevationMetersOrNaN()}.
     *
     * @param elevationMeters Elevation in meters. If null or NaN, the elevation is omitted.
     * @return New point.
     */
    @Nonnull
    public GeoPoint withElevationMeters(@Nullable final Double elevationMeters) {
        return new GeoPoint(lat, lon, elevationMeters);
    }

    @Override
    @Nonnull
    public GeoPoint translate(@Nonnull final GeoVector vector) {
        final double newLat = MathUtils.limitTo(lat + vector.getNorthing(), -90.0, 90.0);
        double newLon = lon + vector.getEasting();
        if (newLon < -180.0) {
            newLon += 360.0;
        } else {
            if (newLon >= 180.0) {
                newLon -= 360.0;
            }
        }
        final double newElevationMeters = getElevationMetersOrNaN() + vector.getElevationMeters();
        return new GeoPoint(newLat, newLon, newElevationMeters);
    }

    /**
     * Translates a geo object in meters. The object's origin's latitude is used to transform meters to a GeoVector.
     */
    @Override
    @Nonnull
    public GeoPoint translate(final double northingMeters, final double eastingMeters, @Nullable final Double elevationMeters) {
        return (GeoPoint) super.translate(northingMeters, eastingMeters, elevationMeters);
    }

    /**
     * Translates a geo object in meters. The object's origin's latitude is used to transform meters to a GeoVector.
     */
    @Override
    @Nonnull
    public GeoPoint translate(final double northingMeters, final double eastingMeters) {
        return (GeoPoint) super.translate(northingMeters, eastingMeters);
    }

    @Nonnull
    @Override
    public GeoPoint moveTo(@Nonnull final GeoPoint origin) {
        return origin;
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof GeoPoint;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoPoint)) {
            final GeoPoint that = (GeoPoint) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && lat.equals(that.lat);
            eq = eq && lon.equals(that.lon);
            if (elevationMeters == null) {
                eq = eq && (that.elevationMeters == null);
            } else {
                eq = eq && elevationMeters.equals(that.elevationMeters);
            }
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper((Object[]) new Double[]{lat, lon, elevationMeters});
    }
}
