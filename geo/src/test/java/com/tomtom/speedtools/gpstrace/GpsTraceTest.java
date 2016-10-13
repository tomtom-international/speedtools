/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.testutils.TestUtils;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class GpsTraceTest {
    private static final Logger LOG = LoggerFactory.getLogger(GpsTraceTest.class);

    private final Collection<GpsTracePoint> x1 = Immutables.emptyList();
    private final GpsTrace x = new GpsTrace(x1);

    private Collection<GpsTracePoint> y1 = null;
    private GpsTrace y = null;

    @Before
    public void setUp() {
        y1 = new ArrayList<>();
        y1.add(new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(1.0, 2.0)));
        y = new GpsTrace(y1);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GpsTrace.class).
                withRedefinedSuperclass().
                verify();
    }

    @Test
    public void testWithGpsTracePoints1() {
        LOG.info("testWithGpsTracePoints1");
        Assert.assertTrue(y1.containsAll(x.withGpsTracePoints(y1).getGpsTracePoints()));
        Assert.assertTrue(x.withGpsTracePoints(y1).getGpsTracePoints().containsAll(y1));

        // Limit to 10 seconds, 3 items.
        final Duration d = Duration.standardSeconds(10);
        final int len = 3;
        y1 = new ArrayList<>();
        y = new GpsTrace(y1);
        Assert.assertEquals(0, y.getGpsTracePoints().size());

        y = new GpsTrace();
        Assert.assertEquals(0, y.getGpsTracePoints().size());

        y1 = new ArrayList<>();
        y1.add(new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(1.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(1, y.getGpsTracePoints().size());
        GpsTracePoint p = y.getLastGpsTracePoint();
        assert p != null;
        Assert.assertTrue(p.getPosition().getLat().compareTo(1.0) == 0);

        y1.add(new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(2.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(2, y.getGpsTracePoints().size());
        p = y.getLastGpsTracePoint();
        assert p != null;
        Assert.assertTrue(p.getPosition().getLat().compareTo(2.0) == 0);

        y1.add(new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(3.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(3, y.getGpsTracePoints().size());
        p = y.getLastGpsTracePoint();
        assert p != null;
        Assert.assertTrue(p.getPosition().getLat().compareTo(3.0) == 0);

        y1.add(new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(4.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(3, y.getGpsTracePoints().size());
        p = y.getLastGpsTracePoint();
        assert p != null;
        Assert.assertTrue(p.getPosition().getLat().compareTo(4.0) == 0);

        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(2, y.withGpsTracePoints(
                new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(5.0, 0.0)),
                new GpsTracePoint(TestUtils.getUniqueDateTime(), new GeoPoint(6.0, 0.0))).
                getGpsTracePoints().size());
    }

    @Test
    public void testWithGpsTracePoints2() {
        LOG.info("testWithGpsTracePoints2");

        // Limit to 1 second, 3 items.
        final Duration d = Duration.standardSeconds(1);
        final int len = 3;
        y1 = new ArrayList<>();
        y = new GpsTrace(y1);
        Assert.assertEquals(0, y.getGpsTracePoints().size());

        y = new GpsTrace();
        Assert.assertEquals(0, y.getGpsTracePoints().size());

        DateTime start = TestUtils.getUniqueDateTime();
        y1 = new ArrayList<>();
        y1.add(new GpsTracePoint(start, new GeoPoint(5.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(1, y.getGpsTracePoints().size());

        start = start.plusSeconds(1);
        y1.add(new GpsTracePoint(start, new GeoPoint(6.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(2, y.getGpsTracePoints().size());

        start = start.plusSeconds(1);
        y1.add(new GpsTracePoint(start, new GeoPoint(7.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(2, y.getGpsTracePoints().size());

        start = start.plusSeconds(1);
        y1.add(new GpsTracePoint(start, new GeoPoint(8.0, 0.0)));
        y = new GpsTrace(GpsTrace.limitSize(d, len, y1));
        Assert.assertEquals(3, y.getGpsTracePoints().size());
    }

    @Test
    public void testToJson() {
        LOG.info("testToJson");
        Assert.assertEquals(
                "{\"gpsTracePoints\":[]}",
                TestUtils.replaceUniques(x.toJson()));
        Assert.assertEquals(
                "{\"gpsTracePoints\":[{\"time\":@DATETIME,\"position\":{\"lat\":1.0,\"lon\":2.0}}]}",
                TestUtils.replaceUniques(y.toJson()));
    }
}
