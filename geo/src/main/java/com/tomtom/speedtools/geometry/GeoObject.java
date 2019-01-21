/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Abstract root class for all things geo.
 *
 * Constructor: {@link #GeoObject}
 */
@Immutable
public abstract class GeoObject implements JsonRenderable {

    /**
     * Protected constructor.
     */
    protected GeoObject() {
        // Empty.
    }

    /**
     * Reference point of the geo object. The origin is the base point for operations like {@link #translate(GeoVector)}
     * and {@link #moveTo(GeoPoint)}. The origin is always one of the used GeoPoints in the object.
     *
     * @return Origin of the geo object.
     */
    @Nonnull
    public abstract GeoPoint getOrigin();

    /**
     * Center point of the geo object. The center is not always the origin of the object.
     * It may also not be a GeoPoint used in the object itself (such as the center point of
     * a GeoLine which is defined by its end points only).
     *
     * The elevation of the center point is an average value of other GeoPoints
     * in the object. It is always defined as being between the minimum and maximum
     * elevations of other points.
     *
     * @return Center of the geo object.
     */
    @Nonnull
    public abstract GeoPoint getCenter();

    /**
     * Translate a geo object with an Easting and Northing value.
     *
     * @param vector Translation vector (along latitude and longitude).
     * @return Translated object.
     */
    @Nonnull
    public abstract GeoObject translate(@Nonnull final GeoVector vector);

    /**
     * Translates a geo object in meters. In fact, the object's origin's latitude is translated
     * by the specified number of meters. The resulting change in latitude and longitude for the
     * origin is then applied to other points as well.
     *
     * @param northingMeters  Meters to North.
     * @param eastingMeters   Meters to East.
     * @param elevationMeters Elevation difference. No elevation change may be specified as 0.0, null or NaN.
     * @return Translated object.
     */
    @Nonnull
    public GeoObject translate(final double northingMeters, final double eastingMeters, @Nullable final Double elevationMeters) {
        final Double lat = getOrigin().getLat();
        return translate(new GeoVector(
                Geo.metersToDegreesLat(northingMeters),
                Geo.metersToDegreesLonAtLat(eastingMeters, lat),
                elevationMeters));
    }

    /**
     * Translates a geo object in meters. In fact, the object's origin's latitude is translated
     * by the specified number of meters. The resulting change in latitude and longitude for the
     * origin is then applied to other points as well.
     *
     * @param northingMeters Meters to North.
     * @param eastingMeters  Meters to East.
     * @return Translated object.
     */
    @Nonnull
    public GeoObject translate(final double northingMeters, final double eastingMeters) {
        return translate(northingMeters, eastingMeters, 0.0);
    }

    /**
     * Move a geo object to specific origin. Note that the definition of the origin of an object is defined in derived
     * classes.
     *
     * @param origin New origin of object.
     * @return Translated object.
     */
    @Nonnull
    public abstract GeoObject moveTo(final GeoPoint origin);

    /**
     * Provide a Json representation of a geo object.
     *
     * @return Json representation of the object.
     */
    @Override
    @Nonnull
    public final String toJson() {
        return Json.toJson(this);
    }

    /**
     * Override of toString() function to provide a Json representation of a geo object.
     *
     * @return Json representation of the object.
     */
    @Override
    @Nonnull
    public final String toString() {
        return Json.toStringJson(this);
    }

    /**
     * Function to provide a hash-code of fields, including the hash-code of the superclass. This class cannot be
     * static, as derived classes will need to include instance variables.
     *
     * @param objects Derived fields. Can be null.
     * @return Hash code.
     */
    protected int hashCodeSuper(@Nullable final Object... objects) {
        return Objects.hashCode(objects);
    }

    protected boolean canEqual(@Nonnull final Object obj) {
        return false;
    }
}
