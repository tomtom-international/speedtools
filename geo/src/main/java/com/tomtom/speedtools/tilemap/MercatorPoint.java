/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.tilemap;

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Mercator point. Specified in X and Y with normalized values of 0..1 (top-left to bottom-right).
 *
 * Constructor: {@link #MercatorPoint}
 */
@Immutable
public final class MercatorPoint {
    private final double mercX;
    private final double mercY;

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

    public double getMercX() {
        return mercX;
    }

    public double getMercY() {
        return mercY;
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

        final double geoX = (mercX - 0.5) * MapConst.WORLD_SIZE;
        final double geoY = (mercY - 0.5) * -MapConst.WORLD_SIZE;
        final double lat = ((Math.atan(Math.exp(geoY / MapConst.WORLD_RADIUS)) / Math.PI) * 360.0) - 90.0;
        final double lon = ((geoX / MapConst.WORLD_RADIUS) / Math.PI) * 180.0;
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
        final double lat = MathUtils.limitTo(point.getLat(), MapConst.LATITUDE_MIN, MapConst.LATITUDE_MAX);
        final double lon = point.getLon();
        final double geoX = MapConst.WORLD_RADIUS * ((lon * Math.PI) / 180.0);
        final double geoY = MapConst.WORLD_RADIUS * Math.log(Math.tan(Math.PI * ((lat + 90.0) / 360.0)));
        final double mercX = Math.min(1.0, Math.max(0.0, (geoX / MapConst.WORLD_SIZE) + 0.5));
        final double mercY = Math.min(1.0, Math.max(0.0, 1.0 - ((geoY / MapConst.WORLD_SIZE) + 0.5)));
        assert MathUtils.isBetween(mercX, 0.0, 1.0) : mercX + ", " + point;
        assert MathUtils.isBetween(mercY, 0.0, 1.0) : mercY + ", " + point;
        return new MercatorPoint(mercX, mercY);
    }

    @Nonnull
    public static MercatorPoint latLonToMercs(final double lat, final double lon) {
        return latLonToMercs(new GeoPoint(lat, lon));
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

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
