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

package com.tomtom.speedtools.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * This class defines a Jackson mapper factory. Any annotation inspectors need to be set in the calling classes.
 */
public final class JsonObjectMapperFactory {

    private JsonObjectMapperFactory() {
        super();
        assert false;
    }

    public static ObjectMapper createJsonObjectMapper() {

        // Create a Json factory with customer properties.
        final JsonFactory jsonFactory = new JsonFactory();

        // Json parsing features.
        jsonFactory.
                configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true).
                configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        // Json generation features.
        jsonFactory.
                configure(Feature.QUOTE_FIELD_NAMES, true).
                configure(Feature.WRITE_NUMBERS_AS_STRINGS, false);

        // Create a custom object mapper from the newly created factory. This object mapper will be used by RestEasy.
        final ObjectMapper mapper = new ObjectMapper(jsonFactory);

        // Set generic mapper configuration.
        mapper
                .configure(MapperFeature.USE_ANNOTATIONS, true)
                .configure(MapperFeature.AUTO_DETECT_GETTERS, false)
                .configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false)
                .configure(MapperFeature.AUTO_DETECT_SETTERS, false)
                .configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);

        mapper
                .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                .setSerializationInclusion(Include.NON_NULL)
                .disableDefaultTyping()
                .disable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        // Set deserialization configuration.
        mapper
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);

        // Set serialization configuration.
        mapper
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true)
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false)
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);

        // The annotation inspectors and additional mappers should be set by the caller.
        return mapper;
    }
}
