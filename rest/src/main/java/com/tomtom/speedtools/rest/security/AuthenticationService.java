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


import com.tomtom.speedtools.domain.Uid;
import scala.App;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Authenticates users with given {@link Credential}s.
 *
 * This class needs to be implemented by the user of the authentication service.
 * It needs to be bound to a singleton like this, in a Guice module:
 * <pre>
 *     binder.bind(AuthenticationService.class).to(AuthenticationServiceImpl.class).in(Singleton.class);
 * </pre>
 */
public interface AuthenticationService {

    /**
     * Tries to authenticate a userName using the provided {@link Password}. Looks up the {@link Identity} in
     * the system.
     *
     * @param userName The (unique) username of the {@link Identity} that needs to be authenticated. Username is
     *                 case-insensitive.
     * @param password The password to use for the authentication.
     * @return The authenticated {@link Identity} with username if credentials match, or null if they do not.
     */
    @Nullable
    Identity authenticateByUserName(@Nonnull String userName, @Nonnull Password password);

    /**
     * Tries to authenticate a {@link Identity} using the provided {@code plaintextToken} <i>d</i> based on the given
     * {@code appId}. Looks up the {@link Identity} in the system via the {@code appId} and compares its associated {@link
     * Credential} <i>c</i> with <i>d</i>. If <i>c</i> and <i>d</i> match (considering {@link Credential} values {@link
     * Credential#credentialStatus} and actual {@link Credential} implementation the {@link Identity} is considered
     * authenticated. <i>c</i>'s {@link Credential#credentialStatus} will be updated as described in {@link
     * CredentialStatus}, both if <i>c</i> and <i>d</i> match and do not match.
     *
     * @param appId          The (unique) app ID associated with the {@link Identity} that needs to be authenticated. App
     *                       ID is case-insensitive.
     * @param plaintextToken A dummy plaintext credential <i>d</i> that needs to be compared to one of {@link Identity}'s
     *                       {@link Credential}'s <i>c</i>.
     * @return The authenticated {@link Identity} with username as derived from the app ID association if credentials
     * match, or null if they do not.
     */
    @Nullable
    Identity authenticateByAppId(@Nonnull Uid<App> appId, @Nonnull String plaintextToken);
}
