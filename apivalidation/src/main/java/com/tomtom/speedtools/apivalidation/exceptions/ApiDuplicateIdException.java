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

package com.tomtom.speedtools.apivalidation.exceptions;

import com.tomtom.speedtools.apivalidation.errors.ApiDuplicateIdError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ApiDuplicateIdException extends ApiBadRequestException {

    @Nonnull
    public static final String DEFAULT_PARAMETER_NAME = "id";

    public ApiDuplicateIdException(
            @Nullable final String actual) {
        this(DEFAULT_PARAMETER_NAME, actual);
    }

    public ApiDuplicateIdException(
            @Nonnull final String parameter,
            @Nullable final String actual) {
        super(new ApiDuplicateIdError(parameter, actual));
    }
}