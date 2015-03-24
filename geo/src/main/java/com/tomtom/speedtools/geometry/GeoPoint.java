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

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.tomtom.speedtools.utils.MathUtils;

/**
 * Geometric point. A geometric point is specified in latitude, longitude.
 *
 * Constructor: {@link #GeoPoint}
 */
@Immutable
public final class GeoPoint extends GeoObject {

    @Nonnull
    private final Double lat;
    @Nonnull
    private final Double lon;

    /**
     * Create a point.
     *
     * @param lat Latitude (North/South), any range, will be wrapped to [-180, 180).
     * @param lon Longitude (West/East), must be [-90, 90].
     */
    public GeoPoint(
            @Nonnull final Double lat,
            @Nonnull final Double lon) {
        super();
        assert lat != null;
        assert lon != null;
        assert MathUtils.isBetween(lat, -90.0, 90.0) : "Latitude not in [-90, 90]: " + lat;
        this.lat = lat;
        this.lon = Geo.mapToLon(lon);
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
     * Setter for {@link #getLat()}.
     */
    @Nonnull
    public GeoPoint withLat(@Nonnull final Double lat) {
        return new GeoPoint(lat, lon);
    }

    /**
     * Setter for {@link #getLon()}.
     */
    @Nonnull
    public GeoPoint withLon(@Nonnull final Double lon) {
        return new GeoPoint(lat, lon);
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
        return new GeoPoint(newLat, newLon);
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
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper((Object[]) new Double[]{lat, lon});
    }
}
