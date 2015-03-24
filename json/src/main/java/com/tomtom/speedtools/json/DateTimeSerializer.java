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

package com.tomtom.speedtools.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import java.io.IOException;

public final class DateTimeSerializer {

    // A DateTimeFormatter is thread-safe, so it can safely be declared static here.
    @Nonnull private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private DateTimeSerializer() {
        // Prevent instantiation.
    }

    /**
     * Provide a custom date time serializer for Joda time objects. (Jackson serializes them as very large objects - we
     * only need a UTC time stamp).
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = FromStringDeserializer.class)
    public interface StringMixIn {
        // Empty.
    }

    @JsonSerialize(using = ToLongSerializer.class)
    @JsonDeserialize(using = FromLongDeserializer.class)
    public interface LongMixIn {
        // Empty.
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "DuplicateThrows"})
    public static class ToStringSerializer extends JsonSerializer<DateTime> {

        @Override
        public void serialize(
            @Nonnull final DateTime value,
            @Nonnull final JsonGenerator jgen,
            @Nonnull final SerializerProvider provider)
            throws IOException, JsonProcessingException {
            assert value != null;
            assert jgen != null;
            assert provider != null;
            final String result = FORMATTER.print(value);
            jgen.writeString(result);
        }
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "DuplicateThrows"})
    public static class ToLongSerializer extends JsonSerializer<DateTime> {

        @Override
        public void serialize(
            @Nonnull final DateTime value,
            @Nonnull final JsonGenerator jgen,
            @Nonnull final SerializerProvider provider)
            throws IOException, JsonProcessingException {
            assert value != null;
            assert jgen != null;
            assert provider != null;
            jgen.writeNumber(value.getMillis());
        }
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "DuplicateThrows"})
    public static class FromStringDeserializer extends JsonDeserializer<DateTime> {
        @Override
        @Nonnull
        public DateTime deserialize(
            @Nonnull final JsonParser jp,
            @Nonnull final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
            assert jp != null;
            assert ctxt != null;
            final String text = jp.getText();
            return FORMATTER.parseDateTime(text).withZone(DateTimeZone.UTC);
        }
    }

    @SuppressWarnings({"OverlyBroadThrowsClause", "DuplicateThrows"})
    public static class FromLongDeserializer extends JsonDeserializer<DateTime> {
        @Override
        @Nonnull
        public DateTime deserialize(
            @Nonnull final JsonParser jp,
            @Nonnull final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
            assert jp != null;
            assert ctxt != null;
            return new DateTime(jp.getLongValue()).withZone(DateTimeZone.UTC);
        }
    }
}
