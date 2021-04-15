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

package com.tomtom.speedtools.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.tomtom.speedtools.json.ImageSerializer.FromBytesDeserializer;
import com.tomtom.speedtools.json.ImageSerializer.FromBytesDeserializerForBufferedImage;
import com.tomtom.speedtools.json.ImageSerializer.ToBytesSerializer;
import com.tomtom.speedtools.xmladapters.DateTimeAdapter.JsonDateTimeStringDeserializer;
import com.tomtom.speedtools.xmladapters.DateTimeAdapter.JsonSerializerWithMillisResolution;
import com.tomtom.speedtools.xmladapters.DateTimeZoneAdapter.JsonDateTimeZoneDeserializer;
import com.tomtom.speedtools.xmladapters.DateTimeZoneAdapter.JsonDateTimeZoneSerializer;
import com.tomtom.speedtools.xmladapters.LocalDateAdapter.JsonLocalDateDeserializer;
import com.tomtom.speedtools.xmladapters.LocalDateAdapter.JsonLocalDateSerializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;

/**
 * Simple Json formatter class. The functions in this class have 2 variants. One with a default configured Json object
 * mapper and another one which allows you to pass a mapper yourself.
 */
public final class Json {
    private static final Logger LOG = LoggerFactory.getLogger(Json.class);

    // Define our default Jackson object mapper.
    @Nonnull
    private static final ObjectMapper OBJECT_MAPPER;
    @Nonnull
    private static final ObjectMapper STRING_MAPPER;

    /**
     *  Initialize Jackson mapper to allows access to private fields and empty beans.
     *  Also add mix-in annotation for Joda time classes to keep them human readable (as of Jackson 2.0).
     */
    static {
        OBJECT_MAPPER = JsonObjectMapperFactory.createJsonObjectMapper();
        STRING_MAPPER = JsonObjectMapperFactory.createJsonObjectMapper();

        // Add annotation inspectors for JAXB, like @XmlElement, required for RestEasy serialization/deserialization.
        OBJECT_MAPPER.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()),
                        new JacksonAnnotationIntrospector()));
        STRING_MAPPER.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()),
                        new JacksonAnnotationIntrospector()));

        // Add custom mappers.
        final SimpleModule module = new SimpleModule("EnhancedJsonMappers", new Version(0, 1, 0, "", "", ""));
        module
                .addSerializer(DateTime.class, new JsonSerializerWithMillisResolution())
                .addDeserializer(DateTime.class, new JsonDateTimeStringDeserializer())
                .addSerializer(DateTimeZone.class, new JsonDateTimeZoneSerializer())
                .addDeserializer(DateTimeZone.class, new JsonDateTimeZoneDeserializer())
                .addSerializer(LocalDate.class, new JsonLocalDateSerializer())
                .addDeserializer(LocalDate.class, new JsonLocalDateDeserializer())
                .addSerializer(Image.class, new ToBytesSerializer())
                .addDeserializer(Image.class, new FromBytesDeserializer())
                .addSerializer(BufferedImage.class, new ToBytesSerializer())
                .addDeserializer(BufferedImage.class, new FromBytesDeserializerForBufferedImage());

        OBJECT_MAPPER.registerModule(module);
        STRING_MAPPER.registerModule(module);
    }

    /**
     * Private ctor. Prevent instantiation.
     */
    private Json() {
        super();
        assert false;
    }

    /**
     * Return JSON mapper used for JAXB.
     *
     * @return JSON mapper.
     */
    @Nonnull
    public static ObjectMapper getCurrentJsonObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Return single JSON mapper for toString.
     *
     * @return JSON mapper for toString.
     */
    @Nonnull
    public static ObjectMapper getCurrentStringObjectMapper() {
        return STRING_MAPPER;
    }

    @Nonnull
    private static String toMapper(@Nonnull final ObjectMapper mapper, @Nonnull final Object obj) {
        assert mapper != null;
        assert obj != null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (final JsonMappingException e) {
            LOG.error("toMapper: Map exception {} --> JSON, mapper={}, exception={}", obj.getClass().getCanonicalName(),
                    mapper.getClass().getCanonicalName(), e.toString());
        } catch (final IOException e) {
            LOG.error("toMapper: Cannot map {} --> JSON, mapper={}, exception={}", obj.getClass().getCanonicalName(),
                    mapper.getClass().getCanonicalName(), e.toString());
        }
        return "";
    }

    @Nullable
    private static <T> T fromMapper(@Nonnull final ObjectMapper mapper, @Nonnull final String json, @Nonnull final Class<T> type) {
        assert mapper != null;
        assert json != null;
        assert type != null;
        try {
            final StringReader reader = new StringReader(json);
            return mapper.readValue(reader, type);
        } catch (final JsonMappingException e) {
            LOG.error("fromMapper: Cannot map JSON {} --> object, mapper={}, exception={}", json,
                    mapper.getClass().getCanonicalName(), e.toString());
        } catch (final IOException e) {
            LOG.error("fromMapper: Cannot map JSON {} --> object, mapper={}, exception={}", json,
                    mapper.getClass().getCanonicalName(), e.toString());
        }
        return null;
    }

    @Nonnull
    public static String toJson(@Nonnull final Object obj) {
        assert obj != null;
        return toMapper(OBJECT_MAPPER, obj);
    }

    @Nullable
    public static <T> T fromJson(@Nonnull final String json, @Nonnull final Class<T> type) {
        assert json != null;
        assert type != null;
        return fromMapper(OBJECT_MAPPER, json, type);
    }

    /**
     * Convert an object to a JSON String representation. The returned string may NOT be readable as pure JSON. If you
     * want to use real JSON, use toJson instead. This method should be used for human readable debugging and logging
     * purposes only.
     *
     * @param obj Object to convert.
     * @return JSON string representation.
     */
    @Nonnull
    public static String toStringJson(@Nullable final Object obj) {
        if (obj == null) {
            return "null";
        }
        String result;
        try {
            result = STRING_MAPPER.writeValueAsString(obj);
        } catch (final JsonMappingException ignored) {
            // Just ignore. (These occur, for example, as a result of the EqualsVerifier.)
            result = "error";
        } catch (final IOException e) {
            LOG.error("toStringJson: Cannot map {} --> JSON, {}", obj.getClass().getCanonicalName(), e.toString());
            result = "error";
        }
        return result;
    }
}
