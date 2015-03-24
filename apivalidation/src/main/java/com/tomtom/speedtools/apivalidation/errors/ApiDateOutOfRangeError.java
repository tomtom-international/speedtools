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

package com.tomtom.speedtools.apivalidation.errors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;

public final class ApiDateOutOfRangeError extends ApiValidationError {

    @Nonnull
    private final String parameter;
    @Nullable
    private final DateTime actual;
    @Nonnull
    private final DateTime minDate;
    @Nonnull
    private final DateTime maxDate;

    public ApiDateOutOfRangeError(
            @Nonnull final String parameter,
            @Nullable final DateTime actual,
            @Nonnull final DateTime minDate,
            @Nonnull final DateTime maxDate) {
        super(ErrorCode.DATE_OUT_OF_RANGE_ERROR);
        assert parameter != null;
        assert minDate != null;
        assert maxDate != null;
        this.parameter = parameter;
        this.actual = actual;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    /**
     * Required by JAX-B, see {@link ApiValidationError}.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Deprecated
    public ApiDateOutOfRangeError() {
        super();
        this.parameter = null;
        this.actual = null;
        this.minDate = null;
        this.maxDate = null;
    }

    @Nonnull
    public String getParameter() {
        return parameter;
    }

    @Nullable
    public DateTime getActualDate() {
        return actual;
    }

    @Nonnull
    public DateTime getMinDate() {
        return minDate;
    }

    @Nonnull
    public DateTime getMaxDate() {
        return maxDate;
    }
}
