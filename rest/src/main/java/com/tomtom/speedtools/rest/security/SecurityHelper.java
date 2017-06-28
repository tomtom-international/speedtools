/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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
package com.tomtom.speedtools.rest.security;

import com.tomtom.speedtools.apivalidation.exceptions.ApiUnauthorizedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * This class is a utility class, which provides simple functions to get the principal name from a security, or throw an
 * "ApiUnauthorizedException" otherwise.
 *
 * The "principal name" is the same the ID of the logged in user.
 */
public class SecurityHelper {

    // Prevent instantiation of helper class.
    private SecurityHelper() {
        assert false;
    }

    /**
     * Return the principal name, or null if the user was not logged in.
     *
     * @param securityContext Security context.
     * @return Principal name.
     */
    @Nullable
    public static String getPrincipalNameOrNull(@Nonnull final SecurityContext securityContext) {
        assert securityContext != null;

        // If the user was not authenticated, the principal name will be null.
        final String callerId;
        if (securityContext.getUserPrincipal() == null) {
            callerId = null;
        } else {
            callerId = securityContext.getUserPrincipal().getName();
        }
        return callerId;
    }

    /**
     * Return the principal name, or throw an ApiUnauthorizedException if the user was not logged in.
     *
     * @param securityContext Security context.
     * @return Principal name.
     */
    @Nonnull
    public static String getPrincipalNameOrThrow(@Nonnull final SecurityContext securityContext) {
        assert securityContext != null;

        // If the user was not authenticated, the ID will be null.
        if (securityContext.getUserPrincipal() == null) {
            throw new ApiUnauthorizedException("User must be logged in.");
        }
        final Principal userPrincipal = securityContext.getUserPrincipal();
        final String principalName = userPrincipal.getName();
        if (principalName == null) {
            throw new ApiUnauthorizedException("User must be logged in (not a valid user ID).");
        }
        return principalName;
    }
}
