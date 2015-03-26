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

package com.tomtom.speedtools.xmladapters;

import com.tomtom.speedtools.xmladapters.DateAdapter.XMLAdapter;
import org.joda.time.IllegalFieldValueException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


@SuppressWarnings({"ProhibitedExceptionDeclared", "OverlyBroadThrowsClause", "CallToDateToString"})
public class DateAdapterTest {
    private static final Logger LOG = LoggerFactory.getLogger(DateAdapterTest.class);

    @Test
    public void testXmlMarshal() {
        LOG.info("Test: testXmlMarshal");

        // Valid.
        final Date date = new Date(1234567890123L);
        final XMLAdapter dateAdapter = new XMLAdapter();
        final String marshal = dateAdapter.marshal(date);
        Assert.assertEquals("2009-02-13T23:31:30Z", marshal);

        // Null (valid).
        Assert.assertNull(dateAdapter.marshal(null));
    }

    @Test
    public void testXmlUnmarshal() throws Exception {
        LOG.info("Test: testXmlUnmarshal");

        // Valid.
        final Date date = new Date(1234567890123L);
        final String dateString = date.toString();
        final XMLAdapter dateAdapter = new XMLAdapter();

        // Zulu time. Eg UTC time.
        final Date unmarshal1 = dateAdapter.unmarshal("2009-02-13T23:31:30Z");
        Assert.assertNotNull(unmarshal1);
        Assert.assertEquals(dateString, unmarshal1.toString());

        final Date unmarshal2 = dateAdapter.unmarshal("2009-02-13T23:31:30.000Z");
        Assert.assertNotNull(unmarshal2);
        Assert.assertEquals(dateString, unmarshal2.toString());

        // Time zone - 1 hour.
        final Date unmarshal3 = dateAdapter.unmarshal("2009-02-13T22:31:30-0100");
        Assert.assertNotNull(unmarshal3);
        Assert.assertEquals(dateString, unmarshal3.toString());

        // Time zone + 1 hour.
        final Date unmarshal4 = dateAdapter.unmarshal("2009-02-14T00:31:30+0100");
        Assert.assertNotNull(unmarshal4);
        Assert.assertEquals(dateString, unmarshal4.toString());

        // Time with positive zero offset.
        final Date unmarshal5 = dateAdapter.unmarshal("2009-02-13T23:31:30+0000");
        Assert.assertNotNull(unmarshal5);
        Assert.assertEquals(dateString, unmarshal5.toString());

        // Time with negative zero offset.
        final Date unmarshal6 = dateAdapter.unmarshal("2009-02-13T23:31:30-0000");
        Assert.assertNotNull(unmarshal6);
        Assert.assertEquals(dateString, unmarshal6.toString());

        // Time with milli seconds.
        final Date unmarshal7 = dateAdapter.unmarshal("2009-02-13T22:01:30.0000-0130");
        Assert.assertNotNull(unmarshal7);
        Assert.assertEquals(dateString, unmarshal7.toString());

        // Offset with semi colons.
        final Date unmarshal8 = dateAdapter.unmarshal("2009-02-14T00:31:30+01:00");
        Assert.assertNotNull(unmarshal8);
        Assert.assertEquals(dateString, unmarshal8.toString());
    }

    @Test
    public void testXmlInvalidUnmarshalValues() {
        LOG.info("Test: testXmlInvalidUnmarshalValues");

        final XMLAdapter dateAdapter = new XMLAdapter();

        try {
            // Month and day switched.
            dateAdapter.unmarshal("2009-13-02T23:31:30Z");
            Assert.fail();
        }
        catch (final IllegalFieldValueException ignored) {
            //ignored.
        }

        try {
            // Hour field too large.
            dateAdapter.unmarshal("2009-02-13T24:31:30.000Z");
            Assert.fail();
        }
        catch (final IllegalFieldValueException ignored) {
            //ignored.
        }

        try {
            // No Time separator.
            dateAdapter.unmarshal("2009-02-1324:31:30.000Z");
            Assert.fail();
        }
        catch (final IllegalArgumentException ignored) {
            //ignored.
        }

        try {
            // Illegal Time separator.
            dateAdapter.unmarshal("2009-02-13t24:31:30.000Z");
            Assert.fail();
        }
        catch (final IllegalArgumentException ignored) {
            //ignored.
        }
    }
}
