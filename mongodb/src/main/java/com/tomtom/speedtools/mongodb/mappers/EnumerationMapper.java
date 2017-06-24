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

package com.tomtom.speedtools.mongodb.mappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for enumeration mappers. Enumeration mappers map enumeration values to database field.
 */
public abstract class EnumerationMapper<T extends Enum<T>> extends Mapper<T> {

    private final Map<T, String> toDb = new HashMap<>();
    private final Map<String, T> fromDb = new HashMap<>();
    private Class<T> enumClass;

    @Nullable
    @Override
    public T fromDb(@Nullable final Object dbValue) throws MapperException {
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof String) {
            final T enumValue = fromDb.get(dbValue.toString());
            if (enumValue == null) {
                throw new MapperException("Invalid enumeration value '" + dbValue + "' for enumeration " + enumClass
                        .getSimpleName());
            }
            return enumValue;
        }
        throw new MapperException("Number expected, " +
                "got a value of type: " + dbValue.getClass().getCanonicalName());
    }

    @Nullable
    @Override
    public String toDb(@Nullable final T value) {
        if (value == null) {
            return null;
        }
        return toDb.get(value);
    }

    @Override
    protected void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
        assert registry != null;

        // Check enum values.
        enumClass = null;
        for (final T value : toDb.keySet()) {
            if (enumClass == null) {
                enumClass = value.getDeclaringClass();
            } else {
                if (enumClass != value.getDeclaringClass()) {
                    throw new SchemaException(
                            "Not all values belong to the same enumeration in: " + getClass().getName());
                }
            }
        }

        // There should be at least one value mapped.
        if (enumClass == null) {
            throw new SchemaException("There should at least one value mapping in: " + getClass().getName());
        }

        // Check whether all values have been mapped.
        assert enumClass.isEnum();
        for (final T value : enumClass.getEnumConstants()) {
            final String dbValue = toDb.get(value);
            if (dbValue == null) {
                throw new SchemaException("Not all enumeration values are mapped in: " + getClass().getName());
            }
            if (!fromDb.get(dbValue).equals(value)) {
                throw new SchemaException(
                        "Not all enumeration values have a unique mapping in: " + getClass().getName());
            }
        }
    }

    @Nonnull
    protected Value value(@Nonnull final T enumValue, @Nonnull final String dbValue) {
        assert enumValue != null;
        assert dbValue != null;
        return new Value(enumValue, dbValue);
    }


    public class Value {
        @Nonnull
        private final T enumValue;
        @Nonnull
        private final String dbValue;

        public Value(@Nonnull final T enumValue, @Nonnull final String dbValue) {
            assert enumValue != null;
            assert dbValue != null;
            this.enumValue = enumValue;
            this.dbValue = dbValue;
            toDb.put(enumValue, dbValue);
            fromDb.put(dbValue, enumValue);
        }

        @Nonnull
        public T getEnumValue() {
            return enumValue;
        }

        @Nonnull
        public String getDbValue() {
            return dbValue;
        }
    }
}

