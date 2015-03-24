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

package com.tomtom.speedtools.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UTCTimeTest {
    private static final Logger LOG = LoggerFactory.getLogger(UTCTimeTest.class);

    @SuppressWarnings("SSBasedInspection")
    @Test
    public void testNow() {
        LOG.info("testNow");
        final DateTime t1 = DateTime.now().withZone(DateTimeZone.UTC);
        final DateTime t2 = UTCTime.now();
        Assert.assertEquals(t1.getMillis(), t2.getMillis(), 100);
    }

    @SuppressWarnings("SSBasedInspection")
    @Test
    public void testFrom() {
        LOG.info("testFrom");
        final DateTime t1 = DateTime.now();
        final DateTime t2 = UTCTime.from(t1);
        Assert.assertEquals(t1.getMillis(), t2.getMillis());
    }

    @Test
    public void testParse() {
        LOG.info("testParse");
        final DateTime t1 = DateTime.parse("2012-10-04").withZone(DateTimeZone.UTC);
        final DateTime t2 = UTCTime.parse("2012-10-04");
        Assert.assertEquals(t1.getMillis(), t2.getMillis());
    }

    @Test
    public void testParseWithTime() {
        LOG.info("testParseWithTime");
        final DateTime t1 = DateTime.parse("2012-10-04T08:15:30").withZone(DateTimeZone.UTC);
        final DateTime t2 = UTCTime.parse("2012-10-04T08:15:30");
        Assert.assertEquals(t1.getMillis(), t2.getMillis());
    }

    @Test
    public void testParseWithTimeZone() {
        LOG.info("testParseWithTimeZone");
        final DateTime t1 = DateTime.parse("2012-10-04T08:15:30-05:00").withZone(DateTimeZone.UTC);
        final DateTime t2 = UTCTime.parse("2012-10-04T08:15:30-05:00");
        Assert.assertEquals(t1.getMillis(), t2.getMillis());
    }

    @Test
    public void testDateTimeToUTCTimestamp() {
        LOG.info("testDateTimeToUTCTimestamp");

        // Valid values.
        final DateTime dt1 = DateTime.parse("1960-10-04T08:15:30-05:00").withZone(DateTimeZone.UTC);
        final int t1 = UTCTime.dateTimeToUTCTimestamp(dt1);
        Assert.assertEquals(-291638670, t1);

        final DateTime dt2 = DateTime.parse("2000-10-04T08:15:30-05:00").withZone(DateTimeZone.UTC);
        final int t2 = UTCTime.dateTimeToUTCTimestamp(dt2);
        Assert.assertEquals(970665330, t2);

        final DateTime dt3 = DateTime.parse("2020-10-04T08:15:30-05:00").withZone(DateTimeZone.UTC);
        final int t3 = UTCTime.dateTimeToUTCTimestamp(dt3);
        Assert.assertEquals(1601817330, t3);

        final DateTime dt4 = UTCTime.MIN_TIMESTAMP_DATE;
        final int t4 = UTCTime.dateTimeToUTCTimestamp(dt4);
        Assert.assertEquals(-2147483646, t4);

        final DateTime dt5 = UTCTime.MAX_TIMESTAMP_DATE;
        final int t5 = UTCTime.dateTimeToUTCTimestamp(dt5);
        Assert.assertEquals(2147483647, t5);

        try {
            final DateTime dt6 = UTCTime.MIN_TIMESTAMP_DATE.minusSeconds(1);
            UTCTime.dateTimeToUTCTimestamp(dt6);
            Assert.fail("Expected to fail because it is less than minimum allowed date");
        }
        catch (final IllegalArgumentException ignored) {
            // Expected.
        }

        try {
            final DateTime dt7 = UTCTime.MAX_TIMESTAMP_DATE.plusSeconds(1);
            UTCTime.dateTimeToUTCTimestamp(dt7);
            Assert.fail("Expected to fail because it is more than maximum allowed date");
        }
        catch (final IllegalArgumentException ignored) {
            // Expected.
        }
    }
}
