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

package com.tomtom.speedtools.gpstrace;

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Objects;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TracePoint represents a position at a specific time and is used for tracking.
 *
 * Constructor: {@link #GpsTracePoint}
 */
public final class GpsTracePoint implements JsonRenderable {
    @Nonnull
    private final DateTime time;
    @Nonnull
    private final GeoPoint position;

    public GpsTracePoint(
            @Nonnull final DateTime time,
            @Nonnull final GeoPoint position) {
        super();
        assert time != null;
        assert position != null;
        this.time = time;
        this.position = position;
    }


    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    private GpsTracePoint() {
        super();
        time = null;
        position = null;
    }

    /**
     * Retrieve time.
     *
     * @return Time.
     */
    @Nonnull
    public DateTime getTime() {
        return time;
    }

    /**
     * Retrieve position.
     *
     * @return Position.
     */
    @Nonnull
    public GeoPoint getPosition() {
        return position;
    }


    /**
     * Setter for {@link #getTime()}
     *
     * @param time New time.
     * @return New immutable object.
     */
    @Nonnull
    public GpsTracePoint withTime(@Nonnull final DateTime time) {
        assert time != null;
        return new GpsTracePoint(time, position);
    }

    /**
     * Setter for {@link #getPosition()}
     *
     * @param position New position.
     * @return New immutable object.
     */
    @Nonnull
    public GpsTracePoint withPosition(@Nonnull final GeoPoint position) {
        assert position != null;
        return new GpsTracePoint(time, position);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof GpsTracePoint;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GpsTracePoint)) {
            final GpsTracePoint that = (GpsTracePoint) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && time.equals(that.time);
            eq = eq && position.equals(that.position);

        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(time, position);
    }

    @Override
    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
