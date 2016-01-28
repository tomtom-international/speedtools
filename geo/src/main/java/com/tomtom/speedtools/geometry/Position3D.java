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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Position3D implements JsonRenderable {
    @Nonnull
    final private GeoPoint geoPoint;
    @Nonnull
    final private Double elevationMeters;

    public Position3D(
            @Nonnull final GeoPoint geoPoint,
            @Nonnull final Double elevationMeters) {
        super();
        assert geoPoint != null;
        assert elevationMeters != null;
        this.geoPoint = geoPoint;
        this.elevationMeters = elevationMeters;
    }

    public Position3D(
            @Nonnull final GeoPoint geoPoint) {
        this(geoPoint, Double.NaN);
    }

    public Position3D(
            @Nonnull final Double lat,
            @Nonnull final Double lon,
            @Nonnull final Double elevationMeters) {
        this(new GeoPoint(lat, lon), elevationMeters);
    }

    public Position3D(
            @Nonnull final Double lat,
            @Nonnull final Double lon) {
        this(lat, lon, Double.NaN);
    }

    @Nonnull
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    @Nonnull
    public Double getElevationMeters() {
        return elevationMeters;
    }

    @Nonnull
    public Position3D withGeoPoint(@Nonnull final GeoPoint geoPoint) {
        return new Position3D(geoPoint, elevationMeters);
    }

    @Nonnull
    public Position3D withElevationMeters(@Nonnull final Double elevationMeters) {
        return new Position3D(geoPoint, elevationMeters);
    }

    @Override
    @Nonnull
    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof Position3D;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Position3D)) {
            final Position3D that = (Position3D) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && geoPoint.equals(that.geoPoint);
            eq = eq && elevationMeters.equals(that.elevationMeters);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(geoPoint, elevationMeters);
    }
}
