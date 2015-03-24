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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.util.Date;


public final class DateAdapter {

    private static final DateTimeAdapter.XMLAdapterWithSecondsResolution XML_DATE_TIME_ADAPTER = new DateTimeAdapter.XMLAdapterWithSecondsResolution();
    private static final DateTimeAdapter.JsonSerializerWithSecondsResolution JSON_DATE_TIME_SERIALIZER = new DateTimeAdapter.JsonSerializerWithSecondsResolution();
    private static final DateTimeAdapter.JsonDateTimeStringDeserializer JSON_DATE_TIME_DESERIALIZER = new DateTimeAdapter.JsonDateTimeStringDeserializer();

    private DateAdapter() {
        // Prevent instantiation.
    }

    /**
     * The class DateAdapter.XMLAdapter can be used as an XmlAdapter for JAX-B annotations. It marshals/unmarshals a date to/from a normalized UTC string format. This format should be used in all communications.
     *
     * Example of using the Xml adapter for a JAX-B annotated resource:
     *
     * <pre>
     * XmlRootElement XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
     * public class MyClass {
     *     XmlJavaTypeAdapter(value = DateAdapter.class) private Date issueDate;
     *     ...
     * }
     * </pre>
     */
    public static class XMLAdapter extends XmlAdapter<String, Date> {

        /**
         * Unmarshall a Date object from the ISO8601 standard.
         *
         * @param v Valid date string of ISO8601 format.
         * @return Date Valid Date object, or null if the input was null.
         * @see <a href="http://tools.ietf.org/html/rfc3339">http://tools.ietf.org/html/rfc3339</a>
         */
        @Nullable
        @Override
        public Date unmarshal(@Nullable final String v) {
            final DateTime dateTime = XML_DATE_TIME_ADAPTER.unmarshal(v);
            if (dateTime == null) {
                return null;
            }
            return dateTime.toDate();
        }

        /**
         * Marshall a Date object into the format: YYYY-MM-DDTHH:mm:ssZ, in UTC time.
         *
         * @param v Valid date.
         * @return Date string, formatted: YYYY-MM-DDTHH:mm:ssZ (UTC time), or null if the input was null.
         */
        @Nullable
        @Override
        public String marshal(@Nullable final Date v) {
            if (v == null) {
                return null;
            }
            return XML_DATE_TIME_ADAPTER.marshal(new DateTime(v));
        }
    }

    public static class JsonDateSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(@Nonnull final Date value, @Nonnull final JsonGenerator jgen, @Nullable final SerializerProvider provider) throws IOException {
            JSON_DATE_TIME_SERIALIZER.serialize(new DateTime(value), jgen, provider);
        }
    }

    public static class JsonDateDeserializer extends JsonDeserializer<Date> {

        @Nullable
        @Override
        public Date deserialize(@Nonnull final JsonParser jp, @Nullable final DeserializationContext ctxt) throws IOException {
            final DateTime dateTime = JSON_DATE_TIME_DESERIALIZER.deserialize(jp, ctxt);
            if (dateTime == null) {
                return null;
            }
            return dateTime.toDate();
        }
    }
}
