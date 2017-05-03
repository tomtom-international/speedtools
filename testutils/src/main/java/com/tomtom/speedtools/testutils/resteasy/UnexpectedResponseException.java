/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.testutils.resteasy;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

/**
 * Throw to communicate that the validation failed.
 */
public class UnexpectedResponseException extends Exception {

    @Nonnull
    private final Response response;

    public UnexpectedResponseException(@Nonnull final Response response, @Nonnull final String message) {
        super(message);
        this.response = response;
    }

    @Nonnull
    public Response getResponse() {
        return response;
    }
}
