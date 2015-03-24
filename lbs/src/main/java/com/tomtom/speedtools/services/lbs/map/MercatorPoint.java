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

package com.tomtom.speedtools.services.lbs.map;

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;

import com.tomtom.speedtools.services.lbs.Lbs;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.utils.MathUtils;

/**
 * Mercator point. Specified in X and Y with normalized values of 0..1 (top-left to bottom-right).
 *
 * Constructor: {@link #MercatorPoint}
 */
@Immutable
final class MercatorPoint {
    public final double mercX;
    public final double mercY;

    /**
     * Create a normalized Mercator pair.
     *
     * @param mercX X coordinate of Mercator pair. Range 0..1, 0 = lon -180, 1 = lon 180.
     * @param mercY Y coordinate of Mercator pair. Range 0..1, 0 = lat 90, 1 = lat -90.
     */
    MercatorPoint(
            final double mercX,
            final double mercY) {
        super();
        assert MathUtils.isBetween(mercX, 0.0, 1.0) : mercX;
        assert MathUtils.isBetween(mercY, 0.0, 1.0) : mercY;
        this.mercX = mercX;
        this.mercY = mercY;
    }

    /**
     * Return lat/lon, given normalized Mercator points. Mercator points are defined as (x, y), where x = 0..1
     * (left/lon=-180 to right/lon=180) and y = 0..1 (top/lat=90 to bottom/lat=-90).
     *
     * @param mercX X, 0..1 (lon -180..180).
     * @param mercY Y, 0..1 (lat 90..-90).
     * @return Lat/lon.
     */
    @Nonnull
    public static GeoPoint mercsToLatLon(final double mercX, final double mercY) {
        assert MathUtils.isBetween(mercX, 0.0, 1.0) : mercX;
        assert MathUtils.isBetween(mercY, 0.0, 1.0) : mercY;

        final double geoX = (mercX - 0.5) * Lbs.WORLD_SIZE;
        final double geoY = (mercY - 0.5) * -Lbs.WORLD_SIZE;
        final double lat = ((Math.atan(Math.exp(geoY / Lbs.WORLD_RADIUS)) / Math.PI) * 360.0) - 90.0;
        final double lon = ((geoX / Lbs.WORLD_RADIUS) / Math.PI) * 180.0;
        return new GeoPoint(lat, lon);
    }

    @Nonnull
    public static GeoPoint mercsToLatLon(@Nonnull final MercatorPoint mercs) {
        return mercsToLatLon(mercs.mercX, mercs.mercY);
    }

    /**
     * Return normalized Mercator points (0..1) for a latitude/longitude. Mercator points are defined as (x, y), where x
     * = 0..1 (left/lon=-180 to right/lon=180) and y = 0..1 (top/lat=90 to bottom/lat=-90).
     *
     * @param point lat/lon.
     * @return Normalized Mercator points.
     */
    @Nonnull
    public static MercatorPoint latLonToMercs(@Nonnull final GeoPoint point) {
        final double lat = MathUtils.limitTo(point.getLat(), Lbs.LATITUDE_MIN, Lbs.LATITUDE_MAX);
        final double lon = point.getLon();
        final double geoX = Lbs.WORLD_RADIUS * ((lon * Math.PI) / 180.0);
        final double geoY = Lbs.WORLD_RADIUS * Math.log(Math.tan(Math.PI * ((lat + 90.0) / 360.0)));
        final double mercX = Math.max(0.0, (geoX / Lbs.WORLD_SIZE) + 0.5);
        final double mercY = Math.max(0.0, 1.0 - ((geoY / Lbs.WORLD_SIZE) + 0.5));
        assert MathUtils.isBetween(mercX, 0.0, 1.0) : mercX + ", " + point;
        assert MathUtils.isBetween(mercY, 0.0, 1.0) : mercY + ", " + point;
        return new MercatorPoint(mercX, mercY);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof MercatorPoint;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof MercatorPoint)) {
            final MercatorPoint that = (MercatorPoint) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && (Double.compare(mercX, that.mercX) == 0);
            eq = eq && (Double.compare(mercY, that.mercY) == 0);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Double[]{mercX, mercY});
    }
}
