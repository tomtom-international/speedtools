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

package com.tomtom.speedtools.services;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListenerRegistry;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportResource;
import com.tomtom.speedtools.services.sms.SMSProviderConnector;
import com.tomtom.speedtools.services.sms.implementation.SMSDeliveryReportResourceImpl;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBird;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBirdProperties;
import com.tomtom.speedtools.services.sms.implementation.nexmo.Nexmo;
import com.tomtom.speedtools.services.sms.implementation.nexmo.NexmoProperties;

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
}
