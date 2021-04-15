/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

import javax.annotation.Nonnull;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public final class SecurityContextImpl implements SecurityContext {
    @Nonnull
    private final Principal principal;
    @Nonnull
    private final String authenticationScheme;

    public SecurityContextImpl(
            @Nonnull final Principal principal,
            @Nonnull final String authenticationScheme) {
        assert principal != null;
        assert authenticationScheme != null;

        this.principal = principal;
        this.authenticationScheme = authenticationScheme;
    }

    @Override
    @Nonnull
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(@Nonnull final String role) {
        assert role != null;
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Nonnull
    @Override
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }
}
