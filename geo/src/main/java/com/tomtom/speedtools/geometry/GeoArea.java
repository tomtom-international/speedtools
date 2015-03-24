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

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnull;

import java.util.Collection;

/**
 * A geo area is an expression which has the following format:
 * <p/>
 * <pre>
 *     GeoArea      := Primitive
 *                  |  Inverse
 *                  |  Union
 *                  |  Difference
 *
 *     Primitive    := GeoRectangle
 *                  |  GeoCircle
 *
 *     Inverse      := invert(GeoArea)
 *
 *     Union        := add(GeoArea, GeoArea)
 *
 *     Difference   := subtract(GeoArea, GeoArea)
 *
 *     Intersection := intersect(GeoArea, GeoArea)
 * </pre>
 * <p/>
 * Primitives are specified using GeoPoint objects, which can contain latitude/longitude values. As latitudes/longitudes
 * do not project easily to a Carthesian coordinates, the operations for union, intersect, contains, etc. are subject to
 * the following known restrictions:
 * <p/>
 * They are only accurate for relatively small areas, such as 200x200km.
 * <p/>
 * They are may do NOT work properly around the 180th degree longitude (http://en.wikipedia.org/wiki/180th_meridian).
 * <p/>
 * Definitions of operators:
 * <p/>
 * Union({1, 2, 3}, {3, 4, 5}) == {1, 2, 3, 4, 5}
 * <p/>
 * Difference({ 1, 2, 3}, {3, 4, 5}) == {1, 2}
 * <p/>
 * Intersection({ 1, 2, 3}, {3, 4, 5}) == {3}
 */
@Immutable
public abstract class GeoArea extends GeoObject {

    @Nonnull
    @Override
    public GeoPoint getOrigin() {
        return boundingBox().getSouthWest();
    }

    @Override
    @Nonnull
    public GeoPoint getCenter() {
        final GeoLine southNorth = new GeoLine(
                new GeoPoint(boundingBox().getSouthWest().getLat(), 0.0),
                new GeoPoint(boundingBox().getNorthEast().getLat(), 0.0));

        final GeoLine westEast = new GeoLine(
                new GeoPoint(0.0, boundingBox().getSouthWest().getLon()),
                new GeoPoint(0.0, boundingBox().getNorthEast().getLon()));

        final double centerLat = southNorth.getCenter().getLat();
        final double centerLon = westEast.getCenter().getLon();
        return new GeoPoint(centerLat, centerLon);
    }

    /**
     * Convenience function to create a GeoArea from a list of areas. The areas will be Union-ed. This function can be
     * used in combination with pixelate(), which produces a list of GeoRectangles as an approximation for the GeoArea
     * surface.
     *
     * @param areas Collection of areas to union. The list must contain at least 1 area.
     */
    @Nonnull
    public static GeoArea fromAreas(@Nonnull final Collection<? extends GeoArea> areas) {
        assert areas != null;
        assert !areas.isEmpty();
        GeoArea result = null;
        for (final GeoArea area : areas) {
            if (result == null) {
                result = area;
            } else {
                result = result.add(area);
            }
        }
        assert result != null;
        return result;
    }

    /**
     * Binary operator: add(this, other).
     *
     * @param op Area to add to 'this'.
     * @return Union of 'this' and other geo area.
     */
    @Nonnull
    public final GeoArea add(@Nonnull final GeoArea op) {
        assert op != null;
        return new Union(this, op).optimize();
    }

    /**
     * Returns whether 'this' geo area overlaps with another geo area. Area B overlaps with A if there are points within
     * B that are als contained in A.
     * <p/>
     * Note that this may be an approximation! For example, 2 circles close together may return overlap=true where they
     * actually do not really overlap.
     * <p/>
     * Overlap is symmetric: a.overlap(b) == b.overlaps(a).
     */
    public abstract boolean overlaps(@Nonnull final GeoArea area);

    /**
     * Returns whether another geo area is fully contained within 'this' geo area. Area B is contained within A if all
     * points that are contained in B are also contained in A.
     * <p/>
     * The following holds: a.contains(b) AND b.contains(a) iff a.equals(b)
     */
    public abstract boolean contains(@Nonnull final GeoArea area);

    /**
     * See {@link #contains(GeoArea)}.
     *
     * @param point Point.
     * @return True if points is contained within area.
     */
    public abstract boolean contains(@Nonnull final GeoPoint point);

    /**
     * Return a rectangle bounding box for the entire expression.
     *
     * @return Rectangular bounding box with a surface &gt; 0.
     */
    @Nonnull
    public abstract GeoRectangle boundingBox();

    /**
     * Return a flat list of GeoRectangles that approximate the shape of the GeoArea, much like pixelation of a shape.
     * The resolution of the pixelation is fixed. The individual rectangles may or may not overlap.
     * <p/>
     * The individual "pixels" (GeoRectangles) are never "wrapped", meaning their south-west longitude is always &lt;=
     * north-east longitude.
     * <p/>
     * Wrapped shapes in the area will be divided into separate GeoRectangles by this function. This makes it easier
     * for, for example, database to execute queries on them.
     *
     * @return List of 'pixelation rectangles' for this area.
     */
    @Nonnull
    public abstract Collection<GeoRectangle> pixelate();

    @Nonnull
    @Override
    public abstract GeoArea translate(@Nonnull final GeoVector vector);

    /**
     * Move a geo object to specific origin. Note that the definition of the origin of an object is defined in derived
     * classes.
     *
     * @param origin New origin of object.
     */
    @Override
    @Nonnull
    public abstract GeoArea moveTo(final GeoPoint origin);

    /**
     * Return whether this area is singular of compound.
     *
     * @return True if compound.
     */
    public abstract boolean isCompound();

    /**
     * Return an optimized version of 'this' geo area. For example, if an expression consists of 2 unioned rectangles A
     * and B, and B is fully contained within A, then the flattened version of 'this' would be A.
     * <p/>
     * Note that the implementation of flatten is not REQUIRED to produce an optimized expression. It IS required to
     * produce an expression with exactly the same geo characteristics though.
     *
     * @return Normalized expression.
     */
    @Nonnull
    protected GeoArea optimize() {
        return this;
    }
}
