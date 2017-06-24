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

package com.tomtom.speedtools.xmladapters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;

/**
 * Adapter class for serializing and deserializing date/time values in XML/JSON.
 */
public final class DateTimeAdapter {

    // A DateTimeFormatter is thread-safe, so it can safely be declared static here.
    @Nonnull
    private static final DateTimeFormatter READ = ISODateTimeFormat.dateTimeParser();
    @Nonnull
    private static final DateTimeFormatter WRITE_WITH_SECONDS_RESOLUTION = ISODateTimeFormat.dateTimeNoMillis();
    // The ISODateTimeFormat.dateTime() formatter uses a colon in the timezone offset, e.g. +02:00. Use pattern instead.
    @Nonnull
    private static final DateTimeFormatter WRITE_WITH_MILLIS_RESOLUTION = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private DateTimeAdapter() {
        // Prevent instantiation.
    }

    /**
     * The class DateTimeAdapter.XMLAdapterWithSecondsResolution can be used as an XmlAdapter for JAX-B annotations. It marshals/unmarshals a date to/from a normalized UTC string format. This format should be used in all communications.
     *
     * Example of using the Xml adapter for a JAX-B annotated resource:
     *
     * <pre>
     * XmlRootElement XmlAccessorType(XmlAccessType.FIELD)
     * public class MyClass {
     *     XmlJavaTypeAdapter(value = DateTimeAdapter.class) protected DateTime issueDateTime;
     *     ...
     * }
     * </pre>
     */
    public static class XMLAdapterWithSecondsResolution extends XmlAdapter<String, DateTime> {

        /**
         * Unmarshall a DateTime object from the format: YYYY-MM-DDTHH:mm:ssZ, in UTC time.
         *
         * @param v Valid date string of format YYYY-MM-DDTHH:mm:ssZ (UTC time).
         * @return DateTime Valid DateTime object, or null if the input was null.
         */
        @Nullable
        @Override
        public DateTime unmarshal(@Nullable final String v) {
            if (v == null) {
                return null;
            }
            return READ.parseDateTime(v);
        }

        /**
         * Marshall a DateTime object into the format: YYYY-MM-DDTHH:mm:ssZ, in UTC time.
         *
         * @param v Valid date.
         * @return DateTime string, formatted: YYYY-MM-DDTHH:mm:ssZ (UTC time), or null if the input was null.
         */
        @Nullable
        @Override
        public String marshal(@Nullable final DateTime v) {
            if (v == null) {
                return null;
            }
            final DateTime utc = UTCTime.from(v);
            return WRITE_WITH_SECONDS_RESOLUTION.print(utc);
        }
    }

    public static class XMLAdapterWithMillisResolution extends XMLAdapterWithSecondsResolution {
        @Nullable
        @Override
        public String marshal(@Nullable final DateTime v) {
            if (v == null) {
                return null;
            }
            final DateTime utc = UTCTime.from(v);
            return WRITE_WITH_MILLIS_RESOLUTION.print(utc);
        }
    }

    public static class JsonSerializerWithSecondsResolution extends JsonSerializer<DateTime> {

        @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
        @Override
        public void serialize(@Nonnull final DateTime t, @Nonnull final JsonGenerator jsonGenerator, @Nullable final SerializerProvider serializerProvider) throws IOException {
            final String result = WRITE_WITH_SECONDS_RESOLUTION.print(t);
            jsonGenerator.writeString(result);
        }
    }

    public static class JsonSerializerWithMillisResolution extends JsonSerializer<DateTime> {

        @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
        @Override
        public void serialize(@Nonnull final DateTime t, @Nonnull final JsonGenerator jsonGenerator, @Nullable final SerializerProvider serializerProvider) throws IOException {
            final String result = WRITE_WITH_MILLIS_RESOLUTION.print(t);
            jsonGenerator.writeString(result);
        }
    }

    public static class JsonDateTimeStringDeserializer extends JsonDeserializer<DateTime> {

        @Nullable
        @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
        @Override
        public DateTime deserialize(@Nonnull final JsonParser jsonParser, @Nullable final DeserializationContext deserializationContext) throws IOException {
            final String text = jsonParser.getText();
            if (text == null) {
                return null;
            }
            return READ.parseDateTime(text).withZone(DateTimeZone.UTC);
        }
    }
}
