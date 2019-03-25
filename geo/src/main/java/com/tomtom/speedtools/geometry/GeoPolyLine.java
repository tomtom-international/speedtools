/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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
     * Either all points in the polyline have an elevation, or none have. If at least one point has an
     * elevation, points with missing elevations will have an elevation added to them, which is the same
     * elevation as one of their neighbors.
     *
     * @param points Points, to be connected by polyline, via shortest line (so not across the long side of the
     *               Earth). Must contain at least 2 points.
     */
    public GeoPolyLine(@Nonnull final List<GeoPoint> points) {
        super();
        assert points != null;
        assert points.size() >= 2;

        // Check the need to correct the elevation in a list of points.
        final List<GeoPoint> elevatedPoints;
        final boolean noneElevated = points.stream().noneMatch(x -> (x.getElevationMeters() != null));
        final boolean allElevated = points.stream().noneMatch(x -> (x.getElevationMeters() == null));

        if (allElevated || noneElevated) {

            // Don't need to create a new list.
            elevatedPoints = points;
        } else {

            // Find the first elevation, or use NaN.
            double elevationMeters = points.stream().filter(point -> point.getElevationMeters() != null).findFirst().
                    orElse(new GeoPoint(0.0, 0.0, Double.NaN)).
                    getElevationMetersOrNaN();

            // Create a new list of points, append first elevation if needed.
            elevatedPoints = new ArrayList<>(points.size());
            for (final GeoPoint point : points) {

                // Use new elevation if available.
                if (point.getElevationMeters() != null) {
                    elevationMeters = point.getElevationMeters();
                }

                // Add point, with elevation added (might be its own elevation.
                elevatedPoints.add(point.withElevationMeters(elevationMeters));
            }
        }
        this.points = Immutables.listOf(elevatedPoints);
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
     * The elevation of the center point is defined as the average of all elevations of its points.
     * if any of the elevations is undefined, this one will be too.
     *
     * @return Center.
     */
    @Nonnull
    @Override
    public GeoPoint getCenter() {
        assert !points.isEmpty();
        double elevationMeters = 0.0;
        GeoPoint southWest = points.get(0);
        GeoPoint northEast = points.get(0);
        for (final GeoPoint point : points) {
            elevationMeters = elevationMeters + point.getElevationMetersOrNaN();
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
                (southWest.getLon() + northEast.getLon()) / 2.0,
                elevationMeters / points.size());
        return center;
    }

    /**
     * Return an (interpolated) point on a polyline, given an offset in meters.
     * The interpolated point is capped to the polyline bounds.
     * A negative offset interpolates the point from the end rather than the start
     * of the polyline.
     *
     * @param offsetInMeters Offset in meters, if positive (or 0.0) from the start of the polyline,
     *                       if negative, from the end of the polyline.
     * @return Interpolated point on polyline, capped to the end points of the polyline.
     */
    @Nonnull
    public GeoPoint getPointAtOffset(final double offsetInMeters) {
        final List<GeoLine> lines = asLines();
        assert !lines.isEmpty();
        int index = 0;
        GeoLine line;
        double nextOffset;
        if (offsetInMeters < 0.0) {
            nextOffset = Math.max(0.0, getLengthMeters() + offsetInMeters);
        } else {
            nextOffset = Math.min(getLengthMeters(), offsetInMeters);
        }
        double offset;
        do {
            offset = nextOffset;
            line = lines.get(index);
            nextOffset = offset - line.getLengthMeters();
            ++index;
        } while ((nextOffset > 0.0) && (index < lines.size()));
        assert (0.0 <= offset) && (offset <= line.getLengthMeters());
        final double ratio = offset / line.getLengthMeters();
        final double northing = line.getNorthing() * ratio;
        final double easting = line.getEasting() * ratio;
        final GeoPoint point = line.getOrigin().translate(new GeoVector(northing, easting));
        return point;
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
