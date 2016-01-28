/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.mongodb;

import com.mongodb.BasicDBObject;
import com.tomtom.speedtools.json.SimpleJsonSerializer;
import org.bson.types.ObjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Simple POJO to MongoDB DBObject serializer/deserializer.
 */
public final class SimpleMongoDBSerializer extends SimpleJsonSerializer {

    /**
     * Singleton instance of this class.
     */
    private static final SimpleMongoDBSerializer INSTANCE = new SimpleMongoDBSerializer();

    /**
     * Prevent instantiation of this class by others, Use {@link #getInstance()} instead.
     */
    private SimpleMongoDBSerializer() {
        super();
    }

    /**
     * Get the serializer/deserializer instance.
     *
     * @return Serializer/deserializer.
     */
    @Nonnull
    public static SimpleMongoDBSerializer getInstance() {
        return INSTANCE;
    }

    /**
     * Overridden to create MongoDB JSON mapped object, instead of Java Map objects.
     *
     * @return New MongoDB JSON mapped object, used by super class.
     */
    @Override
    @Nonnull
    protected Map<String, Object> createJsonMappedObject() {
        return new BasicDBObject();
    }

    /**
     * Provide a mapper for BSON objects.
     *
     * @param rawType Object type.
     * @return Object mapper.
     */
    @Override
    @Nullable
    protected Mapper determineMapperFromRawType(@Nonnull final Class<?> rawType) {
        assert rawType != null;

        /**
         * Now, for every known type, return its object mapper.
         */
        if (ObjectId.class.isAssignableFrom(rawType)) {
            return new IdentityMapper(rawType);
        }
        return null;
    }
}
