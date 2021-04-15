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

package com.tomtom.speedtools.services.sms;

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.rest.RestEasyJacksonContextResolver;
import com.tomtom.speedtools.services.sms.SMSProviderConnector.Status;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBird;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBirdProperties;
import com.tomtom.speedtools.services.sms.implementation.nexmo.Nexmo;
import com.tomtom.speedtools.services.sms.implementation.nexmo.NexmoProperties;
import com.tomtom.speedtools.time.UTCTime;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

// Do not run this test as part of standard test suite.
@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class SMSConnectorLiveTest {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(SMSConnectorLiveTest.class);

    /**
     * This test is explicitly DISABLED to avoid sending test message (which cost money!) every time a unit test is
     * run.
     */
    @Ignore
    @Test
    public void testMessageBird() {
        LOG.info("testMessageBird");

        final ResteasyProviderFactory factory = new ResteasyProviderFactory() {
            {
                this.addContextResolver(new RestEasyJacksonContextResolver());
            }
        };
        ResteasyProviderFactory.setInstance(factory);

        /**
         * Do not use these credentials for real applications purposes. They are here for demonstration
         * purposes only!
         */
        final MessageBirdProperties messageBirdProperties =
                new MessageBirdProperties("http://api.messagebird.com", "username", "password", "SpeedTools", false);
        final SMSProviderConnector connector = new MessageBird(messageBirdProperties);

        /**
         * The telephone number has been crossed out on purpose. Fill it with your test phone number
         * if needed.
         */
        final Status status =
                connector.sendTextMessage("+31650431247", "This is a test message, using MessageBird, please ignore.", 33333L);
        // connector.sendTextMessage("<telephoneNumber>", "Please ignore. This is a test message using MessageBird. Sent at " + Json.toJson(UTCTime.now()), 33333L);

        LOG.info("testMessageBird: status={}", status);
    }

    /**
     * This test is explicitly DISABLED to avoid sending test message (which cost money!) every time a unit test is
     * run.
     */
    @Ignore
    @Test
    public void testNexmo() {
        LOG.info("testNexmo");

        final ResteasyProviderFactory factory = new ResteasyProviderFactory() {
            {
                this.addContextResolver(new RestEasyJacksonContextResolver());
            }
        };
        ResteasyProviderFactory.setInstance(factory);

        /**
         * Do not use these credentials for real applications purposes. They are here for demonstration
         * purposes only!
         */
        final NexmoProperties nexmoProperties =
                new NexmoProperties("http://rest.nexmo.com", "<username>", "<password>", "SpeedTools", false);
        final SMSProviderConnector connector = new Nexmo(nexmoProperties);

        /**
         * The telephone number has been crossed out on purpose. Fill it with your test phone number
         * if needed.
         */
        final Status status =
                connector.sendTextMessage("+31650431247", "Please ignore. This is a test message using Nexmo. Sent at " + Json.toJson(UTCTime.now()), 33333L);

        LOG.info("testNexmo: status={}", status);
    }
}
