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

import javax.annotation.Nonnull;

/**
 * This exception is a base class for exceptions that get thrown whenever a DAO encounters a problem.
 */
public abstract class DaoException extends Exception {
    private static final long serialVersionUID = 1L;

    protected DaoException() {
        super();
    }

    protected DaoException(@Nonnull final Exception cause) {
        super(cause);
        assert cause != null;
    }

    protected DaoException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    protected DaoException(@Nonnull final String message, @Nonnull final Exception cause) {
        super(message, cause);
        assert message != null;
        assert cause != null;
    }
}
