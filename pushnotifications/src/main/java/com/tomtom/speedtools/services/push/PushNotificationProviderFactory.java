/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

import com.tomtom.speedtools.services.push.domain.NotificationChannelType;

import javax.annotation.Nonnull;


/**
 * Factory for {@link PushNotificationProvider}s.
 */
public interface PushNotificationProviderFactory {

    /**
     * Method returns supported {@link NotificationChannelType}.
     *
     * @return The {@link NotificationChannelType} that this instance supports.
     */
    @Nonnull
    NotificationChannelType getSupportedNotificationChannelType();

    /**
     * Method returns the maximum number of instances of this provider that may be used simultaneously be used by client.
     *
     * @return The maximum number of instances of this provider. Always greater than or equal to zero.
     */
    int getMaxInstances();

    /**
     * Method creates and returns a new instance of {@link PushNotificationProvider}. Implementations can generate more
     * than {@link #getMaxInstances()}. Client code is responsible to manage the maximum number of instances.
     *
     * @return A new {@link PushNotificationProvider}.
     */
    @Nonnull
    PushNotificationProvider create();
}
