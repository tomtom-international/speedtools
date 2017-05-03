/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tomtom.speedtools.xmladapters.LocalDateAdapter.JsonLocalDateDeserializer;
import com.tomtom.speedtools.xmladapters.LocalDateAdapter.JsonLocalDateSerializer;
import com.tomtom.speedtools.xmladapters.LocalDateAdapter.XMLAdapter;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LocalDateAdapterTest {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDateAdapterTest.class);

    @Test
    public void testXmlMarshal() {
        LOG.info("Test: testXmlMarshal");

        final XMLAdapter localDateAdapter = new XMLAdapter();

        // Valid.
        final LocalDate localDate = new LocalDate(1234567890123L, DateTimeZone.UTC);
        Assert.assertEquals("2009-02-13", localDateAdapter.marshal(localDate));

        // Null (valid).
        Assert.assertNull(localDateAdapter.marshal(null));
    }

    @Test
    public void testXmlUnmarshal() {
        LOG.info("Test: testXmlUnmarshal");

        final XMLAdapter localDateAdapter = new XMLAdapter();

        // Valid.
        final LocalDate localDate = new LocalDate(1234567890123L, DateTimeZone.UTC);
        Assert.assertEquals(localDate, localDateAdapter.unmarshal("2009-02-13"));

        // Null (valid).
        Assert.assertNull(localDateAdapter.unmarshal(null));
    }

    @Test
    public void testXmlInvalidUnmarshalValues() {
        LOG.info("Test: testXmlInvalidUnmarshalValues");

        final XMLAdapter localDateAdapter = new XMLAdapter();

        try {
            // Month and day switched.
            localDateAdapter.unmarshal("2009-13-02");
            Assert.fail();
        } catch (final IllegalFieldValueException ignored) {
            //ignored.
        }

        try {
            // Month zero.
            localDateAdapter.unmarshal("2009-00-02");
            Assert.fail();
        } catch (final IllegalFieldValueException ignored) {
            //ignored.
        }

        try {
            // Nonexistent day in February.
            localDateAdapter.unmarshal("2009-02-31");
            Assert.fail();
        } catch (final IllegalFieldValueException ignored) {
            //ignored.
        }
    }

    @Test
    @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
    public void testJsonSerializer() throws IOException {
        LOG.info("testJsonSerializer");

        final JsonLocalDateSerializer localDateSerializer = new JsonLocalDateSerializer();
        final LocalDate localDate = new LocalDate(1234567890123L, DateTimeZone.UTC);

        final JsonGenerator jgen = Mockito.mock(JsonGenerator.class);
        final SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        localDateSerializer.serialize(localDate, jgen, provider);
        Mockito.verify(jgen).writeString("2009-02-13");
    }

    @Test
    @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
    public void testJsonDeserializer() throws IOException {
        LOG.info("testJsonDeserializer");

        final JsonLocalDateDeserializer localDateDeserializer = new JsonLocalDateDeserializer();

        final JsonParser jp = Mockito.mock(JsonParser.class);
        final DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        // Valid date.
        final LocalDate localDate = new LocalDate(1234567890123L, DateTimeZone.UTC);
        Mockito.when(jp.getText()).thenReturn("2009-02-13");
        Assert.assertEquals(localDate, localDateDeserializer.deserialize(jp, ctxt));

        // Null.
        Mockito.reset(jp);
        Assert.assertNull(localDateDeserializer.deserialize(jp, ctxt));

        try {
            // Month and day switched.
            Mockito.when(jp.getText()).thenReturn("2009-13-02");
            localDateDeserializer.deserialize(jp, ctxt);
            Assert.fail();
        } catch (final IllegalFieldValueException ignored) {
            //ignored.
        }

        try {
            // Month zero.
            Mockito.when(jp.getText()).thenReturn("2009-00-02");
            localDateDeserializer.deserialize(jp, ctxt);
            Assert.fail();
        } catch (final IllegalFieldValueException ignored) {
            //ignored.
        }

        try {
            // Nonexistent day in February.
            Mockito.when(jp.getText()).thenReturn("2009-02-31");
            localDateDeserializer.deserialize(jp, ctxt);
            Assert.fail();
        } catch (final IllegalFieldValueException ignored) {
            //ignored.
        }
    }
}
