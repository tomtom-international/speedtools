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
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.tomtom.speedtools.utils.StringUtils.nullToEmpty;
import static com.tomtom.speedtools.utils.StringUtils.trim;

/**
 * Representation of identities.
 */
public final class Identity implements JsonRenderable {
    @Nonnull
    private final Uid<Identity> id;
    @Nonnull
    private final String userName;

    /**
     * Create an identity.
     *
     * @param id       Uid of identity.
     * @param userName User name of identity.
     */
    public Identity(
            @Nonnull final Uid<Identity> id,
            @Nullable final String userName) {
        super();
        assert id != null;
        this.id = id;
        this.userName = nullToEmpty(trim(userName));
    }

    // Convenience constructor.
    public Identity(
            @Nonnull final Uid<Identity> id) {
        this(id, null);
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    Identity() {
        super();
        id = null;
        userName = null;
    }

    /**
     * Id of the identity.
     *
     * @return Id of the identity.
     */
    @Nonnull
    public Uid<Identity> getId() {
        return id;
    }

    /**
     * User name.
     *
     * @return User name.
     */
    @Nonnull
    public String getUserName() {
        return userName;
    }

    /**
     * Setter for {@link #getId()}.
     *
     * @param id Id of the app.
     * @return New immutable object.
     */
    @Nonnull
    public Identity withId(@Nonnull final Uid<Identity> id) {
        return new Identity(id, userName);
    }

    /**
     * Setter for {@link #getUserName()}.
     *
     * @param userName User name.
     * @return New immutable object.
     */
    @Nonnull
    public Identity withUserName(@Nullable final String userName) {
        return new Identity(id, userName);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof Identity;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Identity)) {
            final Identity that = (Identity) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && id.equals(that.id);
            eq = eq && userName.equals(that.userName);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, userName);
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
