/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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
 * Abstract notion of type mappers. It copies values to it's database representation and visa versa.
 */
public abstract class Mapper<T> {

    /**
     * Transform a typed Java object into its database representation.
     *
     * @param value Value to transform.
     * @return Database representation of value, or null if value was null.
     * @throws MapperException Thrown if the value could not be mapped.
     */
    @Nullable
    public abstract Object toDb(@Nullable T value) throws MapperException;

    /**
     * Transform a database value to a typed Java object.
     *
     * @param dbValue Database value to be transformed.
     * @return Java object, or null of dbValue was null.
     * @throws MapperException Thrown if the database value could not be transformed to a Java object.
     */
    @Nullable
    public abstract T fromDb(@Nullable Object dbValue) throws MapperException;

    /**
     * Initialize the database mappers.
     *
     * @param registry Registry with all mappers.
     * @throws SchemaException Thrown if an error is encountered in one of the mappers. This prevents the system from
     *                         booting with an incorrect mapper configuration.
     */
    protected void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
        // Ok.
    }
}
