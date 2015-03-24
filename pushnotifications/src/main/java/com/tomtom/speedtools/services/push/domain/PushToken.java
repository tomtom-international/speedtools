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

package com.tomtom.speedtools.services.push.domain;

import javax.annotation.Nonnull;

/**
 * This class holds the push token information for a particular client. It acts as an ID that identifies which mobile
 * client to push a message to.
 */
public final class PushToken {

    @Nonnull
    private final String token;

    public PushToken(@Nonnull final String token) {
        assert token != null;
        this.token = token;
    }

    @Nonnull
    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(final Object obj) {
        assert obj != null;
        if (obj instanceof PushToken) {
            final PushToken that = (PushToken) obj;
            return token.equals(that.token);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        return token;
    }
}
