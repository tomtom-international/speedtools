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

public final class ApiResourceConflictError extends ApiValidationError {
    @Nonnull
    private final String message;

    public ApiResourceConflictError(@Nonnull final String message) {
        super(ErrorCode.RESOURCE_CONFLICT);
        assert message != null;
        this.message = message;
    }

    /**
     * Required by JAX-B, see {@link ApiValidationError}.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Deprecated
    public ApiResourceConflictError() {
        super();
        this.message = null;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }
}
