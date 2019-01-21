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
import javax.annotation.Nullable;

public final class ApiBICFormatError extends ApiValidationError {

    @Nonnull
    private final String parameter;
    @Nullable
    private final String actual;

    public ApiBICFormatError(
            @Nonnull final String parameter,
            @Nullable final String actual) {
        super(ErrorCode.BIC_FORMAT_ERROR);
        assert parameter != null;
        this.parameter = parameter;
        this.actual = actual;
    }

    /**
     * Required by JAX-B, see {@link ApiValidationError}.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Deprecated
    public ApiBICFormatError() {
        super();
        this.parameter = null;
        this.actual = null;
    }

    @Nonnull
    public String getParameter() {
        return parameter;
    }

    @Nullable
    public String getActual() {
        return actual;
    }
}
