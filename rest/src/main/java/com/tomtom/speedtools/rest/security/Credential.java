/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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


import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Objects;
import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Placeholder for credential information. (Immutable)  Actual credentials will be stored in sub-classes.
 * Constructor: {@link #Credential}
 */
@Immutable
public abstract class Credential implements JsonRenderable {
    @Nonnull
    protected final DateTime credentialUpdateTime;
    @Nonnull
    protected final CredentialStatus credentialStatus;
    @Nonnull
    protected final DateTime credentialStatusUpdateTime;

    /**
     * Create a credential.
     *
     * @param credentialUpdateTime       Credential update time.
     * @param credentialStatus           Credential status.
     * @param credentialStatusUpdateTime Credential status update time.
     */
    public Credential(
            @Nonnull final DateTime credentialUpdateTime,
            @Nonnull final CredentialStatus credentialStatus,
            @Nonnull final DateTime credentialStatusUpdateTime) {
        super();
        assert credentialUpdateTime != null;
        assert credentialStatus != null;
        assert credentialStatusUpdateTime != null;
        this.credentialUpdateTime = credentialUpdateTime;
        this.credentialStatus = credentialStatus;
        this.credentialStatusUpdateTime = credentialStatusUpdateTime;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    Credential() {
        super();
        credentialUpdateTime = null;
        credentialStatus = null;
        credentialStatusUpdateTime = null;
    }

    /**
     * Credential update time of the credential.
     *
     * @return Credential update time of the credential.
     */
    @Nonnull
    public DateTime getCredentialUpdateTime() {
        return credentialUpdateTime;
    }

    /**
     * Credential status of the credential.
     *
     * @return Credential status of the credential.
     */
    @Nonnull
    public CredentialStatus getCredentialStatus() {
        return credentialStatus;
    }

    /**
     * Credential status update time.
     *
     * @return Credential status update time.
     */
    @Nonnull
    public DateTime getCredentialStatusUpdateTime() {
        return credentialStatusUpdateTime;
    }

    /**
     * Setter for {@link #getCredentialUpdateTime()}.
     *
     * @param credentialUpdateTime Update time of the credential.
     * @return New immutable object.
     */
    @Nonnull
    public abstract Credential withCredentialUpdateTime(@Nonnull DateTime credentialUpdateTime);

    /**
     * Setter for {@link #getCredentialUpdateTime()}.
     *
     * @param credentialStatus Update time of the credential.
     * @return New immutable object.
     */
    @Nonnull
    public abstract Credential withCredentialStatus(@Nonnull CredentialStatus credentialStatus);

    /**
     * Setter for {@link #getCredentialStatusUpdateTime()}.
     *
     * @param credentialStatusUpdateTime Status update time of the credential.
     * @return New immutable object.
     */
    @Nonnull
    public abstract Credential withCredentialStatusUpdateTime(@Nonnull DateTime credentialStatusUpdateTime);

    protected final int hashCodeSuper(final Object... objects) {
        return Objects.hashCode(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, objects);
    }

    public abstract boolean canEqual(@Nonnull final Object obj);

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Credential)) {
            final Credential that = (Credential) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && credentialUpdateTime.equals(that.credentialUpdateTime);
            eq = eq && credentialStatus.equals(that.credentialStatus);
            eq = eq && credentialStatusUpdateTime.equals(that.credentialStatusUpdateTime);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper();
    }

    @Override
    @Nonnull
    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }
}
