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
package com.tomtom.speedtools.mongodb.mappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Mapper where values are equal to db values.
 */
public class IdentityMapper<T> extends Mapper<T> {

    @Nonnull
    private static final IdentityMapper<Object> INSTANCE = new IdentityMapper<>();

    @Nonnull
    public static <T> IdentityMapper<T> getInstance() {
        //noinspection unchecked
        return (IdentityMapper<T>) INSTANCE;
    }

    /**
     * Only used for singleton construction.
     * Use getInstance.
     */
    private IdentityMapper() {
        super();
    }

    @Override
    @Nullable
    public Object toDb(@Nullable final T value) throws MapperException {
        return value;
    }

    @Override
    @Nullable
    public T fromDb(@Nullable final Object dbValue) throws MapperException {
        //noinspection unchecked
        return (T) dbValue;
    }
}
