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

package com.tomtom.speedtools.gpstrace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tomtom.speedtools.utils.MathUtils;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.objects.Objects;

/**
 * Trace represents a trace of GPS point.
 *
 * Constructor: {@link #GpsTrace}
 */
public final class GpsTrace implements JsonRenderable {
    @Nonnull
    private final List<GpsTracePoint> gpsTracePoints;

    /**
     * Create a GPS trace.
     *
     * @param gpsTracePoints Trace points.
     */
    public GpsTrace(
            @Nonnull final Collection<GpsTracePoint> gpsTracePoints) {
        super();
        this.gpsTracePoints = Immutables.listOf(gpsTracePoints);
    }

    /**
     * Same as {@link #GpsTrace}, for an empty list.
     */
    public GpsTrace() {
        super();
        this.gpsTracePoints = new ArrayList<GpsTracePoint>(0);
    }

    /**
     * Retrieve trace data.
     *
     * @return List of trace data points.
     */
    @Nonnull
    public List<GpsTracePoint> getGpsTracePoints() {
        return gpsTracePoints;
    }

    /**
     * Setter for {@link #getGpsTracePoints}
     *
     * @param gpsTracePoints1 New trace data.
     * @param gpsTracePoints2 New trace data.
     * @return New immutable object.
     */
    @Nonnull
    public GpsTrace withGpsTracePoints(@Nonnull final Collection<GpsTracePoint> gpsTracePoints1,
                                       @Nonnull final GpsTracePoint... gpsTracePoints2) {
        return new GpsTrace(Immutables.copyOf(gpsTracePoints1, gpsTracePoints2));
    }

    @Nonnull
    public GpsTrace withGpsTracePoints(@Nonnull final GpsTracePoint... gpsTracePoints) {
        return withGpsTracePoints(Immutables.listOf(gpsTracePoints));
    }

    /**
     * Get latest, most recent GPS point.
     *
     * @return Latest, most recent GPS point, or null, if none available.
     */
    @Nullable
    public GpsTracePoint getLastGpsTracePoint() {
        final int size = gpsTracePoints.size();
        if (size > 0) {
            return gpsTracePoints.get(size - 1);
        } else {
            return null;
        }
    }

    /**
     * Return a limited list of GPS points.
     *
     * @param maxAge          Maximum age difference between oldest and newest point.
     * @param maxSize         Maximum number of points.
     * @param gpsTracePoints1 Original list 1.
     * @param gpsTracePoints2 Original list 2.
     * @return Limited list.
     */
    @Nonnull
    public static Collection<GpsTracePoint> limitSize(
            @Nonnull final Duration maxAge,
            @Nonnull final Integer maxSize,
            @Nonnull final Collection<GpsTracePoint> gpsTracePoints1,
            @Nonnull final GpsTracePoint... gpsTracePoints2) {
        assert maxAge != null;
        assert maxSize != null;
        assert maxSize >= 0;
        assert gpsTracePoints1 != null;
        assert gpsTracePoints2 != null;

        final Collection<GpsTracePoint> fullList = Immutables.copyOf(gpsTracePoints1, gpsTracePoints2);
        final int start = Math.max(0, fullList.size() - maxSize);
        final int end = Math.min(fullList.size(), start + maxSize);
        final int size = end - start;
        assert fullList.isEmpty() || MathUtils.isBetween(start, 0, fullList.size() - 1);
        assert MathUtils.isBetween(end, 0, fullList.size()) : end;
        assert (end > 0) || fullList.isEmpty();
        assert end >= start;
        assert size >= 0;
        assert (fullList.size() <= maxSize) || (size == maxSize);

        // Copy last N data points only.
        final ArrayList<GpsTracePoint> newList = new ArrayList<GpsTracePoint>(size);
        final GpsTracePoint[] points = fullList.toArray(new GpsTracePoint[fullList.size()]);
        final DateTime oldest = (size == 0) ? null : points[size - 1].getTime().minus(maxAge);
        int i = start;
        while (i < end) {
            final GpsTracePoint point = points[i];

            // Only copy points that are not too old.
            final DateTime time = point.getTime();
            if (!time.isBefore(oldest)) {
                newList.add(point);
            }
            ++i;
        }
        return newList;
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof GpsTrace;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GpsTrace)) {
            final GpsTrace that = (GpsTrace) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && gpsTracePoints.equals(that.gpsTracePoints);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(gpsTracePoints);
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
