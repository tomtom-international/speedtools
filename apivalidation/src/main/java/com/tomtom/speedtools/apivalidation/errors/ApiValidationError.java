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

import com.tomtom.speedtools.json.JsonBased;

/**
 * Common base class for API errors.
 */
public abstract class ApiValidationError extends JsonBased {
    @Nonnull
    private final ErrorCode errorCode;

    public enum ErrorCode {
        DATE_OUT_OF_RANGE_ERROR,
        LOCAL_DATE_FORMAT_ERROR,
        DOUBLE_OUT_OF_RANGE_ERROR,
        INTEGER_OUT_OF_RANGE_ERROR,
        LONG_OUT_OF_RANGE_ERROR,
        PARAMETER_MISSING_ERROR,
        STRING_FORMAT_ERROR,
        STRING_LENGTH_ERROR,
        COLOR_FORMAT_ERROR,
        IBAN_FORMAT_ERROR,
        BIC_FORMAT_ERROR,
        INVALID_UID,
        RESOURCE_CONFLICT,
        DUPLICATE_USER_NAME,
        INVALID_PARAMETER_COMBINATION_ERROR,
        INVALID_FORMAT
    }

    /**
     * This "no-arg" constructor is required for serialization by JAX-B in case a list of ApiValidationErrors needs to
     * be returned to the caller of a service.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    protected ApiValidationError() {
        super();
        this.errorCode = null;
    }

    protected ApiValidationError(
            @Nonnull final ErrorCode errorCode) {
        super();
        assert errorCode != null;
        this.errorCode = errorCode;
    }

    @Nonnull
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
