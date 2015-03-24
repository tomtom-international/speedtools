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

package com.tomtom.speedtools.apivalidation.exceptions;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import com.tomtom.speedtools.utils.StringUtils;
import com.tomtom.speedtools.apivalidation.errors.ApiValidationError;

/**
 * This exception may contain 1 or more errors related to API parameter validation.
 */
public class ApiBadRequestException extends ApiException {
    @Nonnull
    private final List<ApiValidationError> errors;

    public ApiBadRequestException(
            @Nonnull final List<ApiValidationError> errors) {
        super();
        assert errors != null;
        this.errors = errors;
    }

    public ApiBadRequestException(
            @Nonnull final ApiValidationError error) {
        super();
        assert error != null;
        this.errors = new ArrayList<ApiValidationError>();
        errors.add(error);
    }

    @Nonnull
    public List<ApiValidationError> getErrors() {
        return errors;
    }

    public int size() {
        return errors.size();
    }

    /**
     * Returns the list of errors making up this exception as a human-readable string. <em>Note</em> that even though
     * JSON notation is used to serialise the list of {@link ApiValidationError ApiValidationErrors}, the result should
     * not be used for JSON processing, as it is not guaranteed that the serialisation will remain the same, because
     * this method is intended to optimise the result for human consumption.
     *
     * @return List of errors making up this exception.
     */
    @Override
    @Nonnull
    public String getMessage() {
        return '[' + StringUtils.mkString(", ", errors) + ']';
    }
}
