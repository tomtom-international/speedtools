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

package com.tomtom.speedtools.services.push.implementation.gcm;

import com.tomtom.speedtools.services.push.PushNotificationProviderFactory;
import com.tomtom.speedtools.services.push.domain.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Factory that can create {@link GCMConnector}s.
 */
public class GCMConnectorFactory implements PushNotificationProviderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GCMConnectorFactory.class);

    @Nonnull
    private final GCMProperties gcmProperties;

    @Inject
    public GCMConnectorFactory(@Nonnull final GCMProperties gcmProperties) {
        assert gcmProperties != null;
        this.gcmProperties = gcmProperties;
        LOG.debug("GCMConnectorFactory: created, maxInstances={}",
                gcmProperties.getMaxInstances());
    }

    @Nonnull
    @Override
    public NotificationChannelType getSupportedNotificationChannelType() {
        return NotificationChannelType.ANDROID_GCM;
    }

    @Override
    public int getMaxInstances() {
        return gcmProperties.getMaxInstances();
    }

    @Override
    @Nonnull
    public GCMConnector create() {
        return new GCMConnector(gcmProperties);
    }
}
