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

package com.tomtom.speedtools.gpstrace;

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.testutils.TestUtils;
import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import com.tomtom.speedtools.time.UTCTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpsTracePointTest {
    private static final Logger LOG = LoggerFactory.getLogger(GpsTracePointTest.class);

    // x is the simplest, empty instance.
    private final DateTime x1 = new DateTime();
    private final GeoPoint x2 = new GeoPoint(1.0, 2.0);
    private final GpsTracePoint x = new GpsTracePoint(x1, x2);

    // y and z are a filled instances.
    private DateTime y1 = null;
    private GeoPoint y2 = null;
    private GpsTracePoint y = null;

    @Before
    public void setUp() {
        y1 = UTCTime.now();
        y2 = new GeoPoint(-1.0, -2.0);
        y = new GpsTracePoint(y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GpsTracePoint.class).
                withPrefabValues(DateTime.class, new DateTime(0), new DateTime(1)).
                withRedefinedSuperclass().
                verify();
    }

    @Test
    public void testEquals() {
        LOG.info("testEquals");
        final GpsTracePoint s = y.withTime(x1).withPosition(x2);
        final GpsTracePoint t = x.withTime(y1).withPosition(y2);
        Assert.assertTrue(x.equals(s));
        Assert.assertTrue(y.equals(t));
        Assert.assertFalse(x.equals(t));
        Assert.assertFalse(y.equals(s));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(GpsTracePoint.class, new int[]{}, y1, y2);
    }

    @Test
    public void testWithTime() {
        LOG.info("testWithTime");
        Assert.assertTrue(y1.equals(x.withTime(y1).getTime()));
        Assert.assertNotNull(y.withTime(x1).getTime());
    }

    @Test
    public void testWithPosition() {
        LOG.info("testWithPosition");
        Assert.assertTrue(y2.equals(x.withPosition(y2).getPosition()));
        Assert.assertNotNull(y.withPosition(x2).getPosition());
    }

    @Test
    public void testToJson() {
        LOG.info("testToJson");
        Assert.assertEquals(
                "{\"time\":@DATETIME,\"position\":{\"lat\":1.0,\"lon\":2.0}}",
                TestUtils.replaceUniques(x.toJson()));
        Assert.assertEquals(
                "{\"time\":@DATETIME,\"position\":{\"lat\":-1.0,\"lon\":-2.0}}",
                TestUtils.replaceUniques(y.toJson()));
    }
}
