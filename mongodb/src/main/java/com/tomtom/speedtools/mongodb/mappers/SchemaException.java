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

/**
 * Exception that indicates an error in a schema.
 */
public class SchemaException extends Exception {

    public SchemaException(@Nonnull final String message) {
        this(null, null, message);
        assert message != null;
    }

    public SchemaException(@Nonnull final String message, @Nonnull final Exception cause) {
        this(null, null, message, cause);
        assert message != null;
        assert cause != null;
    }

    public SchemaException(@Nullable final EntityMapper<?> entityMapper, @Nonnull final String message) {
        this(entityMapper, null, message);
        assert message != null;
    }

    public SchemaException(@Nullable final EntityMapper<?> entityMapper, @Nullable final String fieldName, @Nonnull final String message) {
        this(entityMapper, fieldName, message, null);
        assert message != null;
    }

    public SchemaException(@Nullable final EntityMapper<?> entityMapper, @Nullable final String fieldName, @Nonnull final String message,
                           @Nullable final Exception cause) {
        super(createMessage(entityMapper, fieldName, message), cause);
        assert (fieldName == null) || !fieldName.trim().isEmpty();
        assert message != null;
    }

    private static String createMessage(@Nullable final EntityMapper<?> entityMapper, @Nullable final String fieldName,
                                        @Nonnull final String message) {
        assert message != null;
        final StringBuilder builder = new StringBuilder();
        if ((entityMapper != null)) {
            builder.append("At ").append(entityMapper.getClass().getSimpleName());
            if (fieldName != null) {
                builder.append('.').append(fieldName);
            }
            builder.append(": ");
        }
        builder.append(message);
        return builder.toString();
    }

    @Nonnull
    SchemaException withSource(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String fieldName) {
        assert entityMapper != null;
        assert fieldName != null;
        return new SchemaException(entityMapper, fieldName, getMessage(), this);
    }
}
