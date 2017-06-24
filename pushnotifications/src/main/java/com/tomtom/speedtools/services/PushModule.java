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

package com.tomtom.speedtools.services;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.tomtom.speedtools.services.push.PushNotificationProviderFactory;
import com.tomtom.speedtools.services.push.implementation.apns.APNSConnectorFactory;
import com.tomtom.speedtools.services.push.implementation.apns.APNSProperties;
import com.tomtom.speedtools.services.push.implementation.gcm.GCMConnectorFactory;
import com.tomtom.speedtools.services.push.implementation.gcm.GCMProperties;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

/**
 * This is a Guice module to configure the modules for {@link com.tomtom.speedtools.services}.
 */
public class PushModule implements Module {

    @Override
    public void configure(@Nonnull final Binder binder) {
        assert binder != null;

        // Binder services.
        binder.bind(APNSProperties.class).in(Singleton.class);
        binder.bind(GCMProperties.class).in(Singleton.class);

        // Push notification provider factories.
        final Multibinder<PushNotificationProviderFactory> pushNotificationProviderBinder =
                Multibinder.newSetBinder(binder, PushNotificationProviderFactory.class);
        pushNotificationProviderBinder.addBinding().to(APNSConnectorFactory.class).in(Singleton.class);
        pushNotificationProviderBinder.addBinding().to(GCMConnectorFactory.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Nonnull
    public APNSConnectorFactory provideAPNSConnectorFactory(@Nonnull final APNSProperties apnsProperties) {
        assert apnsProperties != null;
        return new APNSConnectorFactory(apnsProperties);
    }

    @Provides
    @Singleton
    @Nonnull
    public GCMConnectorFactory provideGCMConnectorFactory(@Nonnull final GCMProperties gcmProperties) {
        assert gcmProperties != null;
        return new GCMConnectorFactory(gcmProperties);
    }
}
