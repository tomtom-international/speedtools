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

/**
 * Represents an error that occurred while mapping database data to a java field. Note that the field name denotes the
 * name of the java property (using JavaBean conventions), NOT the name of the database field.
 */
public class MapperError {
    @Nullable
    private EntityMapper<?> entityMapper;
    @Nullable
    private String fieldName;
    @Nullable
    private final String error;
    @Nullable
    private final Exception cause;

    /**
     * Constructor.
     *
     * @param entityMapper Entity mapper.
     * @param fieldName    Field name.
     * @param error        Error message.
     */
    public MapperError(
            @Nullable final EntityMapper<?> entityMapper,
            @Nullable final String fieldName,
            @Nullable final String error) {
        this(entityMapper, fieldName, error, null);
    }

    /**
     * Constructor.
     *
     * @param entityMapper Entity mapper.
     * @param fieldName    Field name.
     * @param error        Error message.
     * @param cause        Cause of the error.
     */
    public MapperError(
            @Nullable final EntityMapper<?> entityMapper,
            @Nullable final String fieldName,
            @Nullable final String error,
            @Nullable final Exception cause) {
        assert (fieldName == null) || !fieldName.trim().isEmpty();
        this.entityMapper = entityMapper;
        this.fieldName = fieldName;
        this.error = error;
        this.cause = cause;
    }

    /**
     * Java class of the field that could not be mapped onto.
     *
     * @return Mapper that caused this error.
     */
    @Nullable
    public EntityMapper<?> getEntityMapper() {
        return entityMapper;
    }

    /**
     * Field name (name of the java bean property) that could not be mapped onto.
     *
     * @return Name of the field.
     */
    @Nullable
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Error message.
     *
     * @return Error message.
     */
    @Nullable
    public String getError() {
        return error;
    }

    /**
     * @return Cause.
     */
    @Nullable
    public Exception getCause() {
        return cause;
    }

    @Nonnull
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (entityMapper != null) {
            final Class<?> entityType = entityMapper.getEntityType();
            assert entityType != null;
            builder.append(entityType.getSimpleName());
        }
        if (fieldName != null) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(fieldName);
        }
        if (error != null) {
            if (builder.length() > 0) {
                builder.append(": ");
            }
            builder.append(error);
        } else if (cause != null) {
            if (builder.length() > 0) {
                builder.append(": ");
            }
            builder.append(cause.getMessage());

        }
        return builder.toString();
    }

    void setSource(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String fieldName) {
        assert entityMapper != null;
        assert fieldName != null;
        if (this.entityMapper == null) {
            this.entityMapper = entityMapper;
        }
        if (this.fieldName == null) {
            this.fieldName = fieldName;
        }
    }
}
