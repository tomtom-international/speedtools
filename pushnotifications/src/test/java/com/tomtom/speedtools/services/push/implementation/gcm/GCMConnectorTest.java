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

package com.tomtom.speedtools.services.push.implementation.gcm;

import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.services.push.InvalidPushTokenException;
import com.tomtom.speedtools.services.push.PushConnectionException;
import com.tomtom.speedtools.services.push.domain.Notification;
import com.tomtom.speedtools.services.push.domain.PushToken;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class GCMConnectorTest {
    private static final Logger LOG = LoggerFactory.getLogger(GCMConnectorTest.class);

    private static final String DEV_API_KEY = "<place developer API key here>";

    private enum TestToken {

        PUSH_CHANNEL_ID("<place test channel ID here>");

        @Nonnull
        private final PushToken token;

        TestToken(@Nonnull final String token) {
            assert token != null;
            this.token = new PushToken(token);
        }

        @Nonnull
        public PushToken getToken() {
            return token;
        }
    }

    private GCMConnector gcmConnector;

    @Before
    public void setUp() {
        final GCMProperties gcmProperties = new GCMProperties(null, DEV_API_KEY, 1, 2, true);

        gcmConnector = new GCMConnector(gcmProperties);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Ignore
    @Test
    public void testPush() throws PushConnectionException, InvalidPushTokenException {
        LOG.info("testPush");

        final Notification notification =
                new Notification(
                        new Uid<Notification>(),
                        "MESSAGE",
                        Duration.standardHours(1),
                        "Test message - ignore",
                        new TestPayLoad("123-122", "test message", 4),
                        1);

        gcmConnector.push(notification, TestToken.PUSH_CHANNEL_ID.getToken());
    }

    private class TestPayLoad implements JsonRenderable {
        @Nonnull
        final String id;
        @Nonnull
        final String someKey;
        @Nonnull
        final Integer number;

        TestPayLoad(@Nonnull final String id,
                    @Nonnull final String someKey,
                    @Nonnull final Integer number) {
            assert id != null;
            assert someKey != null;
            assert number != null;

            this.id = id;
            this.someKey = someKey;
            this.number = number;
        }

        @Override
        @Nonnull
        public String toJson() {
            return Json.toJson(this);
        }

        @Override
        @Nonnull
        public String toString() {
            return Json.toStringJson(this);
        }
    }
}
