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

package com.tomtom.speedtools.services.push;


import javax.annotation.Nonnull;

/**
 * Thrown in case a connection to push service could not be established or in case the message
 * could not be delivered successfully. Most often this exception will be the result of a runtime IOException.
 */
public class PushConnectionException extends Exception {

    public PushConnectionException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    public PushConnectionException(@Nonnull final Exception exception) {
        super(exception);
        assert exception != null;
    }

    public PushConnectionException(final String message, final Exception exception) {
        super(message, exception);
        assert message != null;
        assert exception != null;
    }
}
