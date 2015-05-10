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
     * and {@link #moveTo(GeoPoint)} ).
     *
     * @return Origin of the geo object.
     */
    @Nonnull
    public abstract GeoPoint getOrigin();

    /**
     * Center point of the geo object. The center is not always the origin of the object.
     *
     * @return Center of the geo object.
     */
    @Nonnull
    public abstract GeoPoint getCenter();

    /**
     * Translate a geo object with an Easting and Northing value.
     *
     * @param vector Translation vector.
     * @return Translated object.
     */
    @Nonnull
    public abstract GeoObject translate(@Nonnull final GeoVector vector);

    /**
     * Translates a geo object in meters. The object's origin's latitude is used to transform meters to a GeoVector.
     *
     * @param northingMeters Meters to North.
     * @param eastingMeters  Meters to East.
     * @return Translated object.
     */
    @Nonnull
    public GeoObject translate(final double northingMeters, final double eastingMeters) {
        final Double lat = getOrigin().getLat();
        return translate(new GeoVector(
                Geo.metersToDegreesLat(northingMeters),
                Geo.metersToDegreesLonAtLat(eastingMeters, lat)));
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
