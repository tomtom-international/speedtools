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

package com.tomtom.speedtools.apivalidation.errors;

import javax.annotation.Nonnull;

public final class ApiLongOutOfRangeError extends ApiValidationError {

    @Nonnull
    private final String parameter;
    private final long actual;
    private final long minValue;
    private final long maxValue;

    public ApiLongOutOfRangeError(
            @Nonnull final String parameter,
            final long actual,
            final long minValue,
            final long maxValue) {
        super(ErrorCode.LONG_OUT_OF_RANGE_ERROR);
        assert parameter != null;
        this.parameter = parameter;
        this.actual = actual;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Required by JAX-B, see {@link ApiValidationError}.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Deprecated
    public ApiLongOutOfRangeError() {
        super();
        this.parameter = null;
        this.actual = 0;
        this.minValue = 0;
        this.maxValue = 0;
    }

    @Nonnull
    public String getParameter() {
        return parameter;
    }

    public long getActualValue() {
        return actual;
    }

    public long getMinValue() {
        return minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }
}
