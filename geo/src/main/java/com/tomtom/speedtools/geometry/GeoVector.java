/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

import com.tomtom.speedtools.objects.Objects;
import com.tomtom.speedtools.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Geometric vector which contains an Northing and Easting component.
 *
 * Constructor: {@link #GeoVector}
 */
@Immutable
public final class GeoVector {

    @Nonnull
    private final Double northing;
    @Nonnull
    private final Double easting;
    @Nonnull
    private final Double elevationMeters;

    /**
     * Create a vector.
     *
     * @param northing Northing, must be in range [-180, 180].
     * @param easting  Easting, in range [-360, 360].
     * @param elevationMeters Elevation difference. No elevation change can be specified as 0.0, null or NaN, but is always returned as 0.0.
     */
    public GeoVector(
            @Nonnull final Double northing,
            @Nonnull final Double easting,
            @Nullable final Double elevationMeters) {
        super();
        assert northing != null;
        assert easting != null;
        assert MathUtils.isBetween(northing, -180.0, 180.0) : "Northing not in [-180, 180]: " + northing;
        assert MathUtils.isBetween(easting, -360.0, 360.0) : "Easting not in [-360, 360]: " + easting;
        this.northing = northing;
        this.easting = easting;
        this.elevationMeters = (elevationMeters == null) ? 0.0 : (elevationMeters.isNaN() ? 0.0 : elevationMeters);
    }

    /**
     * Create a vector. No elevation change is supplied (which is assumed 0.0).
     *
     * @param northing Northing, must be in range [-180, 180].
     * @param easting  Easting, in range [-360, 360].
     */
    public GeoVector(
            @Nonnull final Double northing,
            @Nonnull final Double easting) {
        this(northing, easting, 0.0);
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private GeoVector() {
        super();
        northing = null;
        easting = null;
        elevationMeters = null;
    }

    @Nonnull
    public Double getNorthing() {
        return northing;
    }

    @Nonnull
    public Double getEasting() {
        return easting;
    }

    @Nonnull
    public Double getElevationMeters() {
        return elevationMeters;
    }

    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof GeoVector;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoVector)) {
            final GeoVector that = (GeoVector) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && northing.equals(that.northing);
            eq = eq && easting.equals(that.easting);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode((Object[]) new Double[]{northing, easting});
    }
}
