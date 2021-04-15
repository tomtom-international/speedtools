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

import com.tomtom.speedtools.utils.MathUtils;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for geometry package. It provides a number of geo-relevant constants as well as
 * methods to convert degrees to distances, extend geo-areas (to include specific points, for
 * \example) and estimate travel times from one point to another.
 */
public final class Geo {

    // Radius of Earth, as used by WGS84.
    public static final double EARTH_RADIUS_X_METERS = 6378137.0;
    public static final double EARTH_RADIUS_Y_METERS = 6356752.3142;

    // Circumference of Earth.
    public static final double EARTH_CIRCUMFERENCE_X = EARTH_RADIUS_X_METERS * 2.0 * Math.PI;
    public static final double EARTH_CIRCUMFERENCE_Y = EARTH_RADIUS_Y_METERS * 2.0 * Math.PI;

    // Meters per degree latitude is fixed. For longitude: use factor * cos(midpoint of two degree latitudes).
    public static final double METERS_PER_DEGREE_LAT = EARTH_CIRCUMFERENCE_Y / 360.0;
    public static final double METERS_PER_DEGREE_LON_EQUATOR = EARTH_CIRCUMFERENCE_X / 360.0; // * cos(deg(lat)).

    // Almost, but not quite longitude 180 (which wraps to -180).
    public static final double LON180 = 179.999999999999;

    /**
     * Private constructor. Utility class cannot be instantiated.
     */
    private Geo() {
        super();
        assert false;
    }

    /**
     * Grow a rectangle to contain a(n additional) point.
     *
     * @param rectangle Rectangle, or null. If null, the resulting rectangle contains only the specified point.
     *                  This makes it easy to create a rectangle containing a number of points, starting with
     *                  an initial rectangle of 'null' and adding all points consecutively.
     * @param point     Point to add.
     * @return Minimal rectangle containing specified rectangle and point.
     */
    @Nonnull
    public static GeoRectangle grow(@Nullable final GeoRectangle rectangle, @Nonnull final GeoPoint point) {
        if (rectangle == null) {
            return new GeoRectangle(point, point);
        }
        return rectangle.grow(point);
    }

    /**
     * Convert a number of degrees latitude to meters. (This is independent of a longitude.)
     *
     * @param latDegrees Degrees in latitude to convert.
     * @return Meters.
     */
    public static double degreesLatToMeters(final double latDegrees) {
        return latDegrees * METERS_PER_DEGREE_LAT;
    }

    /**
     * Given a reference latitude, convert a number of meters to degrees longitude.
     *
     * @param lonDegrees Degrees in longitude to convert.
     * @param lat        Latitude at which the degrees need to be converted.
     * @return Meters.
     */
    public static double degreesLonToMetersAtLat(final double lonDegrees, final double lat) {
        return lonDegrees * METERS_PER_DEGREE_LON_EQUATOR * Math.cos(Math.toRadians(lat));
    }

    /**
     * Convert a number of meters pointing North to degrees latitude. This approximation works only for relatively small
     * distances (say, up to 200km).
     *
     * @param northMeters Distance to North in meters.
     * @return Degrees latitude.
     */
    public static double metersToDegreesLat(final double northMeters) {
        return northMeters / METERS_PER_DEGREE_LAT;
    }

    /**
     * Given a reference latitude, convert a number of meters to degrees longitude.
     *
     * @param eastMeters Distance to East in meters.
     * @param lat        Latitude at which the meters are measured to the East.
     * @return Degrees latitude.
     */
    public static double metersToDegreesLonAtLat(final double eastMeters, final double lat) {
        return (eastMeters / METERS_PER_DEGREE_LON_EQUATOR) / Math.cos(Math.toRadians(lat));
    }

    /**
     * Calculate the shortest distance between GeoPoints. This is an approximation, that works fine for relative small
     * distances, say up to 200km. (The difference in elevation between the two points is also taken into account.)
     *
     * @param p1 Point 1.
     * @param p2 Point 2.
     * @return Distance, always &gt;= 0.
     */
    public static double distanceInMeters(@Nonnull final GeoPoint p1, @Nonnull final GeoPoint p2) {
        final boolean wrappedOnLongSide = p1.getLon() > p2.getLon();
        double deltaLonDegrees;
        if (wrappedOnLongSide) {
            deltaLonDegrees = 360.0 - (p1.getLon() - p2.getLon());
        } else {
            deltaLonDegrees = p2.getLon() - p1.getLon();
        }
        if (deltaLonDegrees > 180.0) {
            deltaLonDegrees = 360.0 - deltaLonDegrees;
        }

        assert MathUtils.isBetween(deltaLonDegrees, 0.0, 180.0) :
                "|p1.lon - p2.lon| should be be in [0, 180], but is " + deltaLonDegrees;

        final double deltaLatDegrees = Math.abs(p1.getLat() - p2.getLat());
        assert MathUtils.isBetween(deltaLatDegrees, 0.0, 180.0) :
                "|p1.lat - p2.lat| should be be in [0, 180], but is " + deltaLatDegrees;

        // Calculate mid point of 2 latitudes.
        final double avgLat = p1.getLat() + ((p2.getLat() - p1.getLat()) / 2.0);

        // Meters per longitude is fixed; per latitude requires * cos(avg(lat)).
        final double deltaXMeters = degreesLonToMetersAtLat(deltaLonDegrees, avgLat);
        final double deltaYMeters = degreesLatToMeters(deltaLatDegrees);
        Double deltaElevationMeters = p1.getElevationMetersOrNaN() - p2.getElevationMetersOrNaN();
        if (deltaElevationMeters.isNaN()) {
            deltaElevationMeters = 0.0;
        }

        // Calculate length through Earth. This is an approximation, but works fine for short distances.
        final double len = Math.sqrt((deltaXMeters * deltaXMeters) + (deltaYMeters * deltaYMeters) +
                (deltaElevationMeters * deltaElevationMeters));
        assert len >= 0.0;
        return len;
    }

    /**
     * Constrain a value to legal values of latitude, -90..90.
     *
     * @param lat Latitude. Values outside this range are simply cut off to the north/south pole latitudes.
     * @return Latitude within range -90..90.
     */
    public static double mapToLat(final double lat) {
        return MathUtils.limitTo(lat, -90.0, 90.0);
    }

    /**
     * Map a longitude to [-180, 180). Values outside this range are wrapped to this range.
     *
     * @param value Longitude, any range.
     * @return Mapped to [-180, 180).
     */
    public static double mapToLon(final double value) {
        double lon = (((((value >= 0) ? value : -value) + 180) % 360) - 180) * ((value >= 0) ? 1.0 : -1.0);
        if (Double.compare(lon, 180.0) == 0) {
            lon = -lon;
        }
        assert MathUtils.isBetween(lon, -180.0, LON180) : "Longitude not in [-180, 180): " + lon;
        return lon;
    }

    /**
     * The array CROW_FLIGHT_SPEED_TABLE holds pairs of values: (fromDistance, maxSpeedKmH), where maxSpeed is the max.
     * speed in km/hr over the distance specified in fromDistance. The speed is valid until the next fromDistance.
     */
    private static final double[] CROW_FLIGHT_SPEED_TABLE = {
            0, 15,      //  0 ..  1 km  --> 15 km/h
            1, 20,      //  1 ..  2 km  --> 20 km/h
            2, 30,      //  2 ..  3 km  --> 30 km/h
            3, 35,      //  3 ..  4 km  --> 35 km/h
            4, 40,      //  4 ..  6 km  --> 40 km/h
            6, 45,      //  6 .. 10 km  --> 45 km/h
            10, 50,     // 10 .. 15 km  --> 50 km/h
            15, 60,     // 15 .. 25 km  --> 60 km/h
            25, 65,     // 25 .. 50 km  --> 65 km/h
            50, 70,     // 50 .. 100 km --> 70 km/h
            100, 90,    // >100 km      --> 90 km/h
            Double.MAX_VALUE
    };

    /**
     * Return a best estimate for the minimum travel time required to get from A to B. This method must be optimized for
     * calculation speed and is not allowed to call out to other systems, or databases.
     *
     * @param from From point A.
     * @param to   To point B.
     * @return Estimated minimum travel time.
     */
    @Nonnull
    public static Duration estimatedMinTravelTime(
            @Nonnull final GeoPoint from,
            @Nonnull final GeoPoint to) {
        return estimatedMinTravelTime(from, to, 1);
    }

    /**
     * Return a best estimate for the minimum travel time required to get from A to B. The estimate is based on the
     * distance rounded to the <b>roundToMeters</b> given.  This method must be optimized for calculation speed and is
     * not allowed to call out to other systems, or databases.
     *
     * @param from          From point A.
     * @param to            To point B.
     * @param roundToMeters The number of metres the distance should be rounded to before calculation (must be &gt;= 0).
     * @return Estimated minimum travel time.
     */
    @Nonnull
    public static Duration estimatedMinTravelTime(
            @Nonnull final GeoPoint from,
            @Nonnull final GeoPoint to,
            final int roundToMeters) {
        assert from != null;
        assert to != null;
        assert roundToMeters >= 0;

        //  Round distance in meters to the nearest roundToMeters.
        final int roundToMetersOr1 = (roundToMeters == 0) ? 1 : roundToMeters;
        final double distanceDivByRoundToMeters = distanceInMeters(from, to) / roundToMetersOr1;
        double distance = Math.round(distanceDivByRoundToMeters) * roundToMetersOr1;

        double totSecs = 0.0;
        int i = 0;
        while (distance > 0) {

            // Get crow distance and speed.
            final double crowM = CROW_FLIGHT_SPEED_TABLE[i] * 1000.0;
            ++i;
            final double crowMPerS = (CROW_FLIGHT_SPEED_TABLE[i] * 1000.0) / 3600.0;
            ++i;
            final double nextCrowM = CROW_FLIGHT_SPEED_TABLE[i] * 1000.0;

            // Calculate remaining distance and time required.
            final double distM = Math.min(distance, (nextCrowM - crowM));
            final double secs = distM / crowMPerS;
            totSecs = totSecs + secs;

            // Reduce remaining distance.
            distance = distance - (nextCrowM - crowM);
        }
        return Duration.standardSeconds(Math.round(totSecs));
    }
}
