/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListMapper<T> extends Mapper<List<T>> {

    @Nullable
    private Class<? extends Mapper<T>> elementMapperType;
    @Nullable
    private Mapper<T> elementMapper;

    @Nonnull
    public static <T> ListMapper<T> create(@Nonnull final Class<? extends Mapper<T>> elementMapperType) {
        assert elementMapperType != null;
        return new ListMapper<>(elementMapperType, null);
    }

    @Nonnull
    public static <T> ListMapper<T> create(@Nonnull final Mapper<T> elementMapper) {
        assert elementMapper != null;
        return new ListMapper<>(null, elementMapper);
    }

    protected ListMapper(@Nullable final Class<? extends Mapper<T>> elementMapperType,
                         @Nullable final Mapper<T> elementMapper) {
        super();
        this.elementMapperType = elementMapperType;
        this.elementMapper = elementMapper;
    }

    @Nonnull
    public Mapper<T> getElementMapper() {
        assert elementMapper != null;
        return elementMapper;
    }

    @Override
    public List<T> fromDb(@Nullable final Object dbValue) throws MapperException {

        // Only one object? Map it to a singleton-set.
        final List<?> collection;
        if (dbValue == null) {
            collection = Collections.emptyList();
        } else if (dbValue instanceof List) {
            collection = (List<?>) dbValue;
        } else {
            collection = Collections.singletonList(dbValue);
        }

        // Gather mapped nested entities.
        final List<T> values = new ArrayList<>();

        // Loop over the collection.
        assert elementMapper != null;
        for (final Object elt : collection) {

            final T value = elementMapper.fromDb(elt);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    @Nullable
    @Override
    public List<Object> toDb(@Nullable final List<T> value) throws MapperException {

        if (value == null) {
            return Collections.emptyList();
        }

        // Convert references to DBObjects.
        assert elementMapper != null;
        final List<Object> dbObjects = new ArrayList<>(value.size());
        for (final T v : value) {
            final Object dbValue = elementMapper.toDb(v);
            dbObjects.add(dbValue);
        }
        return dbObjects;
    }

    @Override
    protected void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
        super.initialize(registry);
        if (elementMapper != null) {
            @SuppressWarnings("unchecked")
            final Class<? extends Mapper<T>> mapperType = (Class<? extends Mapper<T>>) (Class) elementMapper.getClass();
            this.elementMapperType = mapperType;
            registry.register(elementMapper);
        } else {
            assert elementMapperType != null;
            elementMapper = registry.getOrRegisterMapper(elementMapperType);
            assert elementMapper != null;
        }
    }
}
