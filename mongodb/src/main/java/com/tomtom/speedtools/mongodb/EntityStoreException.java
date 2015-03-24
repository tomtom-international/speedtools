/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
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

import javax.annotation.Nonnull;

/**
 * Thrown when an entity could not be stored to the database. There is nothing wrong with the database connection, but,
 * for example, the data of entity could not be mapped onto a mongo structure.
 */
public class EntityStoreException extends DaoException {

    public EntityStoreException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    public EntityStoreException(@Nonnull final String message, final @Nonnull Exception cause) {
        super(message, cause);
        assert message != null;
        assert cause != null;
    }
}
