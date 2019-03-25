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

package com.tomtom.speedtools.apivalidation.exceptions;

import javax.annotation.Nonnull;

/**
 * Exception is used when the server either does not recognise the request method, or it lacks the ability to fulfill
 * the request.
 */
public final class ApiNotImplementedException extends ApiException {

    public ApiNotImplementedException() {
        super();
    }

    public ApiNotImplementedException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    public ApiNotImplementedException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
        assert message != null;
        assert cause != null;
    }

    public ApiNotImplementedException(@Nonnull final Throwable cause) {
        super(cause);
        assert cause != null;
    }
}
