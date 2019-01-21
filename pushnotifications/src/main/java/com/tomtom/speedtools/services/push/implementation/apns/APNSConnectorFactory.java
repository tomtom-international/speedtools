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

package com.tomtom.speedtools.services.push.implementation.apns;

import com.tomtom.speedtools.services.push.PushNotificationProviderFactory;
import com.tomtom.speedtools.services.push.domain.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Factory that can create {@link APNSConnector}s.
 */
public class APNSConnectorFactory implements PushNotificationProviderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(APNSConnectorFactory.class);

    @Nonnull
    private final APNSProperties apnsProperties;

    @Inject
    public APNSConnectorFactory(@Nonnull final APNSProperties apnsProperties) {
        assert apnsProperties != null;
        this.apnsProperties = apnsProperties;
        LOG.info("APNSConnectorFactory: created, gatewayHost={}, gatewayPort={}",
                apnsProperties.getGatewayHost(), apnsProperties.getGatewayPort());
    }

    @Nonnull
    @Override
    public NotificationChannelType getSupportedNotificationChannelType() {
        return NotificationChannelType.IOS_ASPN;
    }

    @Override
    public int getMaxInstances() {
        return apnsProperties.getMaxInstances();
    }

    @Override
    @Nonnull
    public APNSConnector create() {
        return new APNSConnector(apnsProperties);
    }
}
