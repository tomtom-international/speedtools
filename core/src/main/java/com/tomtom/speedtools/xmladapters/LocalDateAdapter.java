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

package com.tomtom.speedtools.xmladapters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;

/**
 * Adapter class for serializing and deserializing local date/time values (timezone-less) in XML/JSON.
 */
public final class LocalDateAdapter {

    // A DateTimeFormatter is thread-safe, so it can safely be declared static here.
    @Nonnull
    private static final DateTimeFormatter READ = ISODateTimeFormat.dateTimeParser();
    @Nonnull
    private static final DateTimeFormatter WRITE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private LocalDateAdapter() {
        // Prevent instantiation.
    }

    public static class XMLAdapter extends XmlAdapter<String, LocalDate> {

        @Nullable
        @Override
        public LocalDate unmarshal(@Nullable final String v) {
            if (v == null) {
                return null;
            }
            return READ.parseLocalDate(v);
        }

        @Nullable
        @Override
        public String marshal(@Nullable final LocalDate v) {
            if (v == null) {
                return null;
            }
            return WRITE.print(v);
        }
    }

    public static class JsonLocalDateSerializer extends JsonSerializer<LocalDate> {

        @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
        @Override
        public void serialize(
                @Nonnull final LocalDate t,
                @Nonnull final JsonGenerator jsonGenerator,
                @Nonnull final SerializerProvider serializerProvider)
                throws IOException {
            assert t != null;
            assert jsonGenerator != null;
            assert serializerProvider != null;
            final String result = WRITE.print(t);
            jsonGenerator.writeString(result);
        }
    }

    public static class JsonLocalDateDeserializer extends JsonDeserializer<LocalDate> {

        @Nullable
        @SuppressWarnings({"OverlyBroadThrowsClause", "SuppressionAnnotation"})
        @Override
        public LocalDate deserialize(
                @Nonnull final JsonParser jsonParser,
                @Nonnull final DeserializationContext deserializationContext) throws IOException {
            assert jsonParser != null;
            assert deserializationContext != null;
            final String text = jsonParser.getText();
            if (text == null) {
                return null;
            }
            return READ.parseLocalDate(text);
        }
    }
}
