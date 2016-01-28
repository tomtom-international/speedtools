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

package com.tomtom.speedtools.mongodb.mappers;

import com.tomtom.speedtools.utils.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;


/**
 * Thrown when data found in the database is not valid.
 */
public class MapperException extends Exception {

    @Nonnull
    private final List<MapperError> mapperErrors;

    public MapperException(@Nonnull final List<MapperError> mapperErrors) {
        //noinspection ThrowableResultOfMethodCallIgnored
        super((mapperErrors.size() == 1) ? mapperErrors.get(0).getCause() : null);
        assert mapperErrors != null;
        this.mapperErrors = mapperErrors;
    }


    public MapperException(@Nonnull final String message) {
        this(Collections.singletonList(new MapperError(null, null, message)));
        assert message != null;
    }

    public MapperException(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String message) {
        this(Collections.singletonList(new MapperError(entityMapper, null, message)));
        assert entityMapper != null;
        assert message != null;
    }

    public MapperException(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String message,
                           @Nonnull final Exception cause) {
        super(cause);
        assert entityMapper != null;
        assert message != null;
        assert cause != null;
        this.mapperErrors = Collections.singletonList(new MapperError(entityMapper, null, message, cause));
    }

    public MapperException(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String fieldName,
                           @Nonnull final String message) {
        this(Collections.singletonList(new MapperError(entityMapper, fieldName, message)));
        assert entityMapper != null;
        assert fieldName != null;
        assert message != null;
    }

    public MapperException(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String fieldName,
                           @Nonnull final String message, @Nonnull final Exception cause) {
        super(cause);
        assert entityMapper != null;
        assert fieldName != null;
        assert message != null;
        assert cause != null;
        this.mapperErrors = Collections.singletonList(new MapperError(entityMapper, fieldName, message));
    }

    @Nonnull
    public List<MapperError> getMapperErrors() {
        return mapperErrors;
    }

    @Nonnull
    @Override
    public String getMessage() {
        return StringUtils.mkString("\n  ", mapperErrors);
    }

    void setSource(@Nonnull final EntityMapper<?> entityMapper, @Nonnull final String field) {
        assert entityMapper != null;
        assert field != null;
        for (final MapperError error : mapperErrors) {
            error.setSource(entityMapper, field);
        }
    }
}
