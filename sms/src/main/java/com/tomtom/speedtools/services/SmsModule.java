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

package com.tomtom.speedtools.services;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListenerRegistry;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportResource;
import com.tomtom.speedtools.services.sms.SMSProviderConnector;
import com.tomtom.speedtools.services.sms.implementation.SMSDeliveryReportResourceImpl;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBird;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBirdProperties;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBirdResource;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBirdResourceMock;
import com.tomtom.speedtools.services.sms.implementation.nexmo.Nexmo;
import com.tomtom.speedtools.services.sms.implementation.nexmo.NexmoProperties;
import com.tomtom.speedtools.services.sms.implementation.nexmo.NexmoResource;
import com.tomtom.speedtools.services.sms.implementation.nexmo.NexmoResourceMock;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import javax.annotation.Nonnull;
import javax.inject.Singleton;


/**
 * This is a Guice module to configure the modules for {@link com.tomtom.speedtools.services}.
 */
public class SmsModule implements Module {

    @Override
    public void configure(@Nonnull final Binder binder) {
        assert binder != null;

        // Bind SMS providers.
        final Multibinder<SMSProviderConnector> smsProviderBinder =
                Multibinder.newSetBinder(binder, SMSProviderConnector.class);
        smsProviderBinder.addBinding().to(Nexmo.class).in(Singleton.class);
        smsProviderBinder.addBinding().to(MessageBird.class).in(Singleton.class);
        binder.bind(NexmoProperties.class).in(Singleton.class);
        binder.bind(MessageBirdProperties.class).in(Singleton.class);
        binder.bind(SMSDeliveryReportListenerRegistry.class).in(Singleton.class);
        binder.bind(SMSDeliveryReportResource.class).to(SMSDeliveryReportResourceImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Nonnull
    public NexmoResource provideNexmoResource(
            @Nonnull final NexmoProperties nexmoProperties,
            @Nonnull final SMSDeliveryReportListenerRegistry smsDeliveryReportListenerRegistry) {
        assert nexmoProperties != null;
        assert smsDeliveryReportListenerRegistry != null;

        if (nexmoProperties.isMockEnabled()) {
            return new NexmoResourceMock(smsDeliveryReportListenerRegistry);
        }

        // Create an Apache HTTP client executor that has an HTTP client that uses a thread safe connection manager.
        final ClientExecutor clientExecutor =
                new ApacheHttpClient4Executor(new DefaultHttpClient(new ThreadSafeClientConnManager()));

        // Return a new Nexmo proxy instance.
        return ProxyFactory.create(NexmoResource.class, nexmoProperties.getBaseUrl(), clientExecutor);
    }

    @Provides
    @Singleton
    @Nonnull
    public MessageBirdResource provideMessageBirdResource(
            @Nonnull final MessageBirdProperties messageBirdProperties,
            @Nonnull final SMSDeliveryReportListenerRegistry smsDeliveryReportListenerRegistry) {
        assert messageBirdProperties != null;
        assert smsDeliveryReportListenerRegistry != null;

        if (messageBirdProperties.isMockEnabled()) {
            return new MessageBirdResourceMock(smsDeliveryReportListenerRegistry);
        }

        // Create an Apache HTTP client executor that has an HTTP client that uses a thread safe connection manager.
        final ClientExecutor clientExecutor =
                new ApacheHttpClient4Executor(new DefaultHttpClient(new ThreadSafeClientConnManager()));

        // Return a new MessageBird proxy instance.
        return ProxyFactory.create(MessageBirdResource.class, messageBirdProperties.getBaseUrl(), clientExecutor);
    }

}
