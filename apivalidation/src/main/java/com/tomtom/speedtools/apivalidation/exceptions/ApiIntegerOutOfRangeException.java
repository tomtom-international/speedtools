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

import com.tomtom.speedtools.apivalidation.errors.ApiIntegerOutOfRangeError;

import javax.annotation.Nonnull;

public final class ApiIntegerOutOfRangeException extends ApiBadRequestException {

    public ApiIntegerOutOfRangeException(
            @Nonnull final String parameter,
            final int actual,
            final int minValue,
            final int maxValue) {
        super(new ApiIntegerOutOfRangeError(parameter, actual, minValue, maxValue));
    }
}
