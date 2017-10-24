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

import com.tomtom.speedtools.objects.Immutables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;

/**
 * Geometric line between a number of points.
 *
 * Constructor: {@link #GeoPolyLine}
 */
@Immutable
public final class GeoPolyLine extends GeoObject {

    @Nonnull
    private final List<GeoPoint> points;

    /**
     * Create a polyline. A polyline is defined as a series of (shortest) lines between consecutive points.
     *
     * @param points Points, to be connected by polyline, via shortest line (so not across the long side of the
     *               Earth). Must contain at least 2 points.
     */
    public GeoPolyLine(@Nonnull final List<GeoPoint> points) {
        super();
        assert points != null;
        assert points.size() >= 2;
        this.points = Immutables.listOf(points);
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private GeoPolyLine() {
        super();
        points = null;
    }

    /**
     * Return origin == first point.
     *
     * @return Origin.
     */
    @Nonnull
    @Override
    public GeoPoint getOrigin() {
        return points.get(0);
    }

    /**
     * Return center == Mid point of bounding box of polyline.
     *
     * @return Center.
     */
    @Nonnull
    @Override
    public GeoPoint getCenter() {
        GeoPoint southWest = points.get(0);
        GeoPoint northEast = points.get(0);
        for (final GeoPoint point : points) {
            final GeoLine sw = new GeoLine(southWest, point);
            if (sw.isWrappedOnLongSide()) {
                southWest = southWest.withLon(point.getLon());
            } else {
                final GeoLine ne = new GeoLine(northEast, point);
                if (!ne.isWrappedOnLongSide()) {
                    northEast = northEast.withLon(point.getLon());
                }
            }
            if (point.getLat() < southWest.getLat()) {
                southWest = southWest.withLat(point.getLat());
            } else {
                if (point.getLat() > northEast.getLat()) {
                    northEast = northEast.withLat(point.getLat());
                }
            }
        }
        final GeoPoint center = new GeoPoint(
                (southWest.getLat() + northEast.getLat()) / 2.0,
                (southWest.getLon() + northEast.getLon()) / 2.0);
        return center;
    }

    /**
     * Get number of points in polyline.
     *
     * @return Number of points, always &gt; 2.
     */
    public int size() {
        return points.size();
    }

    /**
     * Get point.
     *
     * @param i Index, must be in [0, size - 1].
     * @return Point.
     */
    @Nonnull
    public GeoPoint get(final int i) {
        assert (0 <= i) && (i <= (points.size() - 1));
        return points.get(i);
    }

    /**
     * Get the polyline as a series of connected lines.
     *
     * @return Line segments.
     */
    @Nonnull
    public List<GeoLine> asLines() {
        final List<GeoLine> lines = new ArrayList<>(points.size() - 1);
        GeoPoint from = points.get(0);
        for (int i = 1; i < points.size(); ++i) {
            final GeoPoint to = points.get(i);
            lines.add(new GeoLine(from, to));
            from = to;
        }
        assert lines.size() == (points.size() - 1);
        return lines;
    }

    /**
     * Get a single line segment.
     *
     * @param i Index, must be in [0, size - 2].
     * @return Line segment from index to next point.
     */
    @Nonnull
    public GeoLine getLine(final int i) {
        assert (0 <= i) && (i <= (points.size() - 2));
        return new GeoLine(points.get(i), points.get(i + 1));
    }

    @Nonnull
    @Override
    public GeoPolyLine translate(@Nonnull final GeoVector vector) {
        assert vector != null;
        final List<GeoPoint> translatedPoints = new ArrayList<>(points.size());
        for (final GeoPoint point : points) {
            translatedPoints.add(point.translate(vector));
        }
        return new GeoPolyLine(translatedPoints);
    }

    @Nonnull
    @Override
    public GeoPolyLine moveTo(@Nonnull final GeoPoint origin) {
        assert origin != null;
        final GeoLine line = new GeoLine(points.get(0), origin);
        final double northing = line.getNorthing() * ((origin.getLat() >= points.get(0).getLat()) ? 1 : -1);
        final GeoVector vector = new GeoVector(northing, line.getEasting());
        return translate(vector);
    }

    /**
     * Return length of line in meters (approximation).
     *
     * @return Length in meters.
     */
    public double getLengthMeters() {
        double meters = 0.0;
        GeoPoint prevPoint = points.get(0);
        for (final GeoPoint point : points) {
            meters += new GeoLine(prevPoint, point).getLengthMeters();
            prevPoint = point;
        }
        return meters;
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof GeoPolyLine;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoPolyLine)) {
            final GeoPolyLine that = (GeoPolyLine) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && points.equals(that.points);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(points);
    }
}
