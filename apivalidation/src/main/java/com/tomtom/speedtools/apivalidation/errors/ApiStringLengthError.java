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

package com.tomtom.speedtools.apivalidation.errors;

import javax.annotation.Nonnull;

public final class ApiStringLengthError extends ApiValidationError {

    @Nonnull
    private final String parameter;
    private final int actual;
    private final int minLength;
    private final int maxLength;

    public ApiStringLengthError(
            @Nonnull final String parameter,
            final int actual,
            final int minLength,
            final int maxLength) {
        super(ErrorCode.STRING_LENGTH_ERROR);
        assert parameter != null;
        this.parameter = parameter;
        this.actual = actual;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    /**
     * Required by JAX-B, see {@link ApiValidationError}.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Deprecated
    public ApiStringLengthError() {
        super();
        this.parameter = null;
        this.actual = 0;
        this.minLength = 0;
        this.maxLength = 0;
    }

    @Nonnull
    public String getParameter() {
        return parameter;
    }

    public int getActualLength() {
        return actual;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
