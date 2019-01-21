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

import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static com.tomtom.speedtools.geometry.Geo.*;
import static java.lang.Double.compare;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantMathCall")
public class GeoTest {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(GeoTest.class);

    private static final double DELTA = 0.000001;

    @Test
    public void testDegreesLatToMeters() {
        LOG.info("testDegreesLatToMeters");

        Assert.assertEquals(0, Double.compare(0, Geo.degreesLatToMeters(0)));
        Assert.assertEquals(0, Double.compare(Geo.METERS_PER_DEGREE_LAT / 2.0, Geo.degreesLatToMeters(0.5)));
        Assert.assertEquals(0, Double.compare(Geo.METERS_PER_DEGREE_LAT, Geo.degreesLatToMeters(1)));
        Assert.assertEquals(0, Double.compare(Geo.METERS_PER_DEGREE_LAT * 90, Geo.degreesLatToMeters(90)));
        Assert.assertEquals(0, Double.compare(-Geo.METERS_PER_DEGREE_LAT * 90, Geo.degreesLatToMeters(-90)));
    }

    @Test
    public void testDegreesLonToMeters() {
        LOG.info("testDegreesLonToMeters");

        assertEquals(0, compare(0, degreesLonToMetersAtLat(0, 0)));
        assertEquals(0, compare(METERS_PER_DEGREE_LON_EQUATOR / 2.0, degreesLonToMetersAtLat(0.5, 0)));
        assertEquals(0, compare(METERS_PER_DEGREE_LON_EQUATOR, degreesLonToMetersAtLat(1, 0)));
        assertEquals(0, compare(METERS_PER_DEGREE_LON_EQUATOR * 180, degreesLonToMetersAtLat(180, 0)));
        assertEquals(0, compare(-METERS_PER_DEGREE_LON_EQUATOR * 180, degreesLonToMetersAtLat(-180, 0)));
        assertEquals(METERS_PER_DEGREE_LON_EQUATOR / 2.0, degreesLonToMetersAtLat(1, 60), DELTA);
        assertEquals(METERS_PER_DEGREE_LON_EQUATOR / 2.0, degreesLonToMetersAtLat(1, -60), DELTA);
    }

    @Test
    public void testMetersToDegreesLat() {
        LOG.info("testMetersToDegreesLat");

        Assert.assertEquals(0, Double.compare(0, Geo.metersToDegreesLat(0)));
        Assert.assertEquals(0, Double.compare(0.5, Geo.metersToDegreesLat(Geo.METERS_PER_DEGREE_LAT / 2)));
        Assert.assertEquals(0, Double.compare(1, Geo.metersToDegreesLat(Geo.METERS_PER_DEGREE_LAT)));
        Assert.assertEquals(0, Double.compare(90, Geo.metersToDegreesLat(Geo.METERS_PER_DEGREE_LAT * 90)));
        Assert.assertEquals(0, Double.compare(-90, Geo.metersToDegreesLat(Geo.METERS_PER_DEGREE_LAT * -90)));
    }

    @Test
    public void testMetersToDegreesLon() {
        LOG.info("testMetersToDegreesLon()");

        assertEquals(0, compare(0, metersToDegreesLonAtLat(0, 0)));
        assertEquals(0, compare(0.5, metersToDegreesLonAtLat(METERS_PER_DEGREE_LON_EQUATOR / 2, 0)));
        assertEquals(0, compare(1, metersToDegreesLonAtLat(METERS_PER_DEGREE_LON_EQUATOR, 0)));
        assertEquals(0, compare(180, metersToDegreesLonAtLat(METERS_PER_DEGREE_LON_EQUATOR * 180, 0)));
        assertEquals(0, compare(-180, metersToDegreesLonAtLat(METERS_PER_DEGREE_LON_EQUATOR * -180, 0)));
        assertEquals(2.0, metersToDegreesLonAtLat(METERS_PER_DEGREE_LON_EQUATOR, 60), DELTA);
        assertEquals(2.0, metersToDegreesLonAtLat(METERS_PER_DEGREE_LON_EQUATOR, -60), DELTA);
    }

    @Test
    public void testDistanceInMeters() {
        LOG.info("testDistanceInMeters");

        assertEquals(METERS_PER_DEGREE_LAT, distanceInMeters(
                new GeoPoint(-0.5, 0.0), new GeoPoint(0.5, -0.0)), DELTA);
        assertEquals(METERS_PER_DEGREE_LAT, distanceInMeters(
                new GeoPoint(80.0, 0.0), new GeoPoint(81.0, 0.0)), DELTA);
        assertTrue((abs(METERS_PER_DEGREE_LAT * 2) - distanceInMeters(
                new GeoPoint(0.0, 59.0), new GeoPoint(0.0, 61.0))) < DELTA);
        assertTrue((abs(METERS_PER_DEGREE_LAT * 1.4142135623731) - distanceInMeters(
                new GeoPoint(-1.0, -1.0), new GeoPoint(1.0, 1.0))) < DELTA);

        assertEquals(METERS_PER_DEGREE_LON_EQUATOR, distanceInMeters(
                new GeoPoint(0.0, -0.5), new GeoPoint(0.0, 0.5)), DELTA);
        assertEquals(METERS_PER_DEGREE_LON_EQUATOR, distanceInMeters(
                new GeoPoint(0.0, 80.0), new GeoPoint(0.0, 81.0)), DELTA);
        assertEquals(METERS_PER_DEGREE_LON_EQUATOR / 2.0, distanceInMeters(
                new GeoPoint(60.0, 80.0), new GeoPoint(60.0, 81.0)), DELTA);

        assertEquals(METERS_PER_DEGREE_LON_EQUATOR * 2, distanceInMeters(
                new GeoPoint(0.0, -1.0), new GeoPoint(0.0, 1.0)), DELTA);

        // Test latitude, longitude and elevation length.
        assertEquals(METERS_PER_DEGREE_LAT, distanceInMeters(new GeoPoint(-0.5, 0.0), new GeoPoint(0.5, 0.0)), DELTA);
        assertEquals(METERS_PER_DEGREE_LON_EQUATOR, distanceInMeters(new GeoPoint(0.0, -0.5), new GeoPoint(0.0, 0.5)), DELTA);
        assertEquals(2.0, distanceInMeters(new GeoPoint(0.0, 0.0, 0.0), new GeoPoint(0.0, 0.0, 2.0)), DELTA);

        assertEquals(sqrt((METERS_PER_DEGREE_LAT * METERS_PER_DEGREE_LAT) + (METERS_PER_DEGREE_LON_EQUATOR * METERS_PER_DEGREE_LON_EQUATOR)),
                distanceInMeters(new GeoPoint(-0.5, -0.5), new GeoPoint(0.5, 0.5)), DELTA);
        assertEquals(sqrt((METERS_PER_DEGREE_LAT * METERS_PER_DEGREE_LAT) + (METERS_PER_DEGREE_LON_EQUATOR * METERS_PER_DEGREE_LON_EQUATOR) + (2.0 * 2.0)),
                distanceInMeters(new GeoPoint(-0.5, -0.5, 0.0), new GeoPoint(0.5, 0.5, 2.0)), DELTA);

        // Test wrapping.
        assertTrue((distanceInMeters(new GeoPoint(0.0, -1.0), new GeoPoint(0.0, 1.0)) -
                distanceInMeters(new GeoPoint(0.0, 1.0), new GeoPoint(0.0, -1.0))) < DELTA);
        assertTrue((distanceInMeters(new GeoPoint(0.0, -180.0), new GeoPoint(0.0, LON180)) -
                distanceInMeters(new GeoPoint(0.0, LON180), new GeoPoint(0.0, -180.0))) < DELTA);
        assertTrue((distanceInMeters(new GeoPoint(0.0, -180.0), new GeoPoint(0.0, 0.0)) -
                distanceInMeters(new GeoPoint(0.0, -180.0), new GeoPoint(0.0, 0.0))) < DELTA);
    }

    @Test
    public void testMapToLat() {
        LOG.info("testMapToLat");
        Assert.assertEquals(0, Double.compare(0, Geo.mapToLat(0)));
        Assert.assertEquals(0, Double.compare(-1, Geo.mapToLat(-1)));
        Assert.assertEquals(0, Double.compare(-90, Geo.mapToLat(-90)));
        Assert.assertEquals(0, Double.compare(90, Geo.mapToLat(90)));
        Assert.assertEquals(0, Double.compare(-90, Geo.mapToLat(-100)));
        Assert.assertEquals(0, Double.compare(90, Geo.mapToLat(100)));
    }

    @Test
    public void testMapToLon() {
        LOG.info("testMapToLon");
        assertEquals(0, mapToLon(0), DELTA);
        assertEquals(-1, mapToLon(-1), DELTA);
        assertEquals(-180, mapToLon(-180), DELTA);
        assertEquals(LON180, mapToLon(LON180), DELTA);
        assertEquals(-180, mapToLon(180), DELTA);
        assertEquals(-180, mapToLon(-180), DELTA);
        assertEquals(-160, mapToLon(200), DELTA);
        assertEquals(160, mapToLon(-200), DELTA);
        assertTrue(abs(mapToLon(-360)) < DELTA);
        assertTrue(abs(mapToLon(360)) < DELTA);
    }

    @Test
    public void testEstimatedMinimumTravelTime() {
        LOG.info("testEstimatedMinimumTravelTime");

        // Using these figures (do NOT use the table from Geo directly):
        //    0, 10,      //  0 ..  1 km  --> 10 km/h
        //    1, 20,      //  1 ..  2 km  --> 20 km/h
        //    2, 30,      //  2 ..  3 km  --> 30 km/h
        //    3, 35,      //  3 ..  4 km  --> 35 km/h
        //    4, 40,      //  4 ..  6 km  --> 40 km/h
        //    6, 45,      //  6 .. 10 km  --> 45 km/h
        //    10, 50,     // 10 .. 15 km  --> 50 km/h
        //    15, 60,     // 15 .. 25 km  --> 60 km/h
        //    25, 65,     // 25 .. 50 km  --> 65 km/h
        //    50, 70,     // 50 .. 100 km --> 70 km/h
        //    100, 90,    // >100 km      --> 90 km/h

        final GeoPoint from = new GeoPoint(0.0, 0.0);
        final GeoPoint to0m = from;
        final GeoPoint to500m = from.translate(0, 500);
        final GeoPoint to1000m = from.translate(0, 1000);
        final GeoPoint to1200m = from.translate(0, 1200);
        final GeoPoint to1500m = from.translate(0, 1500);
        final GeoPoint to2000m = from.translate(0, 2000);
        final GeoPoint to2500m = from.translate(0, 2500);
        final GeoPoint to99km = from.translate(0, 99000);
        final GeoPoint to100km = from.translate(0, 100000);

        final Duration sec0m = Duration.standardSeconds(0);
        final Duration sec500m = Duration.standardSeconds(120);
        final Duration sec1000m = Duration.standardSeconds(240);
        final Duration sec1200m = Duration.standardSeconds(240 + 36);
        final Duration sec1500m = Duration.standardSeconds(240 + 90);
        final Duration sec2000m = Duration.standardSeconds(240 + 180);
        final Duration sec2500m = Duration.standardSeconds(240 + 180 + 60);
        final Duration sec99km = Duration.standardSeconds(6007);
        final Duration sec100km = Duration.standardSeconds(6059);

        Assert.assertEquals(sec0m.getStandardSeconds(), Geo.estimatedMinTravelTime(from, to0m, 1).getStandardSeconds());
        Assert.assertEquals(sec500m.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to500m, 1).getStandardSeconds());
        Assert.assertEquals(sec1000m.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to1000m, 1).getStandardSeconds());
        Assert.assertEquals(sec1200m.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to1200m, 1).getStandardSeconds());
        Assert.assertEquals(sec1500m.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to1500m, 1).getStandardSeconds());
        Assert.assertEquals(sec2000m.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to2000m, 1).getStandardSeconds());
        Assert.assertEquals(sec2500m.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to2500m, 1).getStandardSeconds());
        Assert.assertEquals(sec99km.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to99km, 1).getStandardSeconds());
        Assert.assertEquals(sec100km.getStandardSeconds(),
                Geo.estimatedMinTravelTime(from, to100km, 1).getStandardSeconds());
    }
}
