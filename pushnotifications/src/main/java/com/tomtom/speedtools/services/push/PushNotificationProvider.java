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

package com.tomtom.speedtools.services.push;

import com.tomtom.speedtools.services.push.domain.Notification;
import com.tomtom.speedtools.services.push.domain.NotificationChannelType;
import com.tomtom.speedtools.services.push.domain.PushToken;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Implementation of this interface can push notifications to an underlying push service.
 */
public interface PushNotificationProvider {

    /**
     * Method pushes {@link Notification} to given channel id.
     *
     * @param notification The {@link Notification} to push.
     * @param pushToken    The {@link PushToken} to push to.
     * @return The {@link PushToken} the provider is using for the registration. This could have been updated.
     * @throws PushConnectionException   Thrown in case a connection could not me made to push service.
     * @throws InvalidPushTokenException Thrown in case the provider has marked the {@link PushToken} invalid. This
     *                                   {@link PushToken} should not be used anymore.
     */
    PushToken push(@Nonnull Notification notification, @Nonnull final PushToken pushToken) throws PushConnectionException, InvalidPushTokenException;

    /**
     * Method returns supported {@link NotificationChannelType}.
     *
     * @return The {@link NotificationChannelType} that this instance supports.
     */
    @Nonnull
    NotificationChannelType getSupportedNotificationChannelType();

    /**
     * This method returns push channel IDs that have been used earlier, but which are considered to be no longer valid,
     * according to the push notification provider. This will happen when a customer removes an application from its
     * device for instance. References can safely be removed.
     *
     * @return A set of obsolete push channels or empty.
     */
    @Nonnull
    Set<PushToken> getObsoletePushTokens();

    /**
     * Some providers are not able to fetch obsolete tokens from the underlying push service. This methods returns true
     * for implementations that do support this and false otherwise.
     *
     * @return True for implementations that support fetching of obsolete tokens and false otherwise.
     */
    boolean canGetObsoletePushTokens();

    /**
     * Some providers may be disabled at startup. this method returns true in case the provider instance is enabled,
     * false otherwise.
     *
     * @return True in case the provider instance is enabled, false otherwise.
     */
    boolean isEnabled();
}
