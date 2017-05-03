/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.services.push;

import com.tomtom.speedtools.services.push.domain.PushToken;

import javax.annotation.Nonnull;

/**
 * In case a PushToken is considered invalid. Typically thrown by a PushNotificationProvider while pushing.
 */
public class InvalidPushTokenException extends Exception {

    @Nonnull
    private final PushToken pushToken;

    public InvalidPushTokenException(@Nonnull final PushToken pushToken, @Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);

        assert message != null;
        assert cause != null;
        assert pushToken != null;
        this.pushToken = pushToken;
    }

    public InvalidPushTokenException(@Nonnull final PushToken pushToken, @Nonnull final String message) {
        super(message);

        assert message != null;
        assert pushToken != null;
        this.pushToken = pushToken;
    }

    public InvalidPushTokenException(@Nonnull final PushToken pushToken, @Nonnull final Throwable cause) {
        super(cause);

        assert cause != null;
        assert pushToken != null;
        this.pushToken = pushToken;
    }

    public InvalidPushTokenException(@Nonnull final PushToken pushToken) {
        super();

        assert pushToken != null;
        this.pushToken = pushToken;
    }

    /**
     * Returns the {@link PushToken} that is marked invalid.
     *
     * @return The {@link PushToken} that is marked invalid.
     */
    @Nonnull
    public PushToken getPushToken() {
        return pushToken;
    }
}
