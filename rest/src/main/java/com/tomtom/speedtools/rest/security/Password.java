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

import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Password credentials. (Immutable)
 *
 * Constructor: {@link #Password}
 */
public final class Password extends Credential {
    @Nonnull
    private final String hash;

    /**
     * Create password.
     *
     * @param credentialUpdateTime       Last update of credential.
     * @param credentialStatus           Status.
     * @param credentialStatusUpdateTime Last update of status.
     * @param hash                       Hash of password. Not empty.
     */
    public Password(
            @Nonnull final DateTime credentialUpdateTime,
            @Nonnull final CredentialStatus credentialStatus,
            @Nonnull final DateTime credentialStatusUpdateTime,
            @Nonnull final String hash) {
        super(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime);
        assert hash != null;
        assert !hash.isEmpty();
        this.hash = hash;
    }

    public Password(
            @Nonnull final DateTime credentialUpdateTime,
            @Nonnull final String hash) {
        this(credentialUpdateTime, CredentialStatus.TRY_1, credentialUpdateTime, hash);
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Deprecated
    private Password() {
        super();
        hash = null;
    }

    /**
     * Get password hash.
     *
     * @return Hash.
     */
    @Nonnull
    public String getHash() {
        return hash;
    }

    /**
     * Setter for {@link #getHash()}.
     *
     * @param hash New hash.
     * @return New immutable object.
     */
    @Nonnull
    public Password withHash(@Nonnull final String hash) {
        return new Password(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, hash);
    }

    @Nonnull
    @Override
    public Credential withCredentialUpdateTime(@Nonnull final DateTime credentialUpdateTime) {
        return new Password(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, hash);
    }

    @Nonnull
    @Override
    public Credential withCredentialStatus(@Nonnull final CredentialStatus credentialStatus) {
        return new Password(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, hash);
    }

    @Nonnull
    @Override
    public Credential withCredentialStatusUpdateTime(@Nonnull final DateTime credentialStatusUpdateTime) {
        return new Password(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, hash);
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof Password;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Password)) {
            final Password that = (Password) obj;
            eq = that.canEqual(this);
            eq = eq && super.equals(that);
            eq = eq && hash.equals(that.hash);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(hash);
    }
}
