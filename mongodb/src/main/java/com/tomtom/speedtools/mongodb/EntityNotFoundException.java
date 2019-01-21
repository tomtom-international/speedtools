/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.mongodb;

import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * Thrown when an entity that is expected to exist could not be retrieved from the database.
 */
public class EntityNotFoundException extends DaoException {

    public EntityNotFoundException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    public EntityNotFoundException(@Nonnull final Logger log, @Nonnull final String message,
                                   @Nonnull final Exception cause) {
        super(message, cause);
        assert log != null;
        assert message != null;
        assert cause != null;
        log.debug("EntityNotFoundException: " + message, cause);
    }
}
