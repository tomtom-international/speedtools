/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.services.push.InvalidPushTokenException;
import com.tomtom.speedtools.services.push.domain.Notification;
import com.tomtom.speedtools.services.push.domain.NotificationChannelType;
import com.tomtom.speedtools.services.push.domain.PushToken;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Tests Apple Push Notification Service Connector. It uses the sandbox environment.
 */
public class APNSConnectorTest {
    private static final Logger LOG = LoggerFactory.getLogger(APNSConnectorTest.class);

    private static final String GATEWAY_PUSH_HOST = "gateway.sandbox.push.apple.com";
    private static final String FEEDBACK_PUSH_HOST = "feedback.sandbox.push.apple.com";
    private static final String PATH_TO_SANDBOX_CERTIFICATE =
            "/com/tomtom/speedtools/push/implementation/apns/tomtomspeedtools-apns-sandbox.p12";
    public static final String SANDBOX_CERTIFICATE_PASSWORD = "Bertha 92";

    private enum TestToken {

        PUSH_CHANNEL_ID("<place push channel ID here>"),

        PUSH_CHANNEL_ID_INVALID("beebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeeb");

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

    private APNSConnector apnsConnector;

    @Before
    public void setUp() {
        final APNSProperties apnsProperties = new APNSProperties(
                GATEWAY_PUSH_HOST, 2195,
                FEEDBACK_PUSH_HOST, 2196,
                PATH_TO_SANDBOX_CERTIFICATE, SANDBOX_CERTIFICATE_PASSWORD, 2, true);

        apnsConnector = new APNSConnector(apnsProperties);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Ignore // Do not want this in automatic builds.
    @Test
    public void testPush() throws APNSGatewayConnectionException, InvalidPushTokenException {
        LOG.info("testPush");

        final Notification notification =
                new Notification(
                        new Uid<Notification>(),
                        "MESSAGE",
                        Duration.standardHours(1),
                        "Test message - ignore",
                        new TestPayLoad("123-122", "test message", 4),
                        1);

        apnsConnector.push(notification, TestToken.PUSH_CHANNEL_ID.getToken());
        try {
            Thread.sleep(5000);
        } catch (final InterruptedException ignored) {
            LOG.error("testPush: sleeping problems");
        }
        // Send it again.
        apnsConnector.push(notification, TestToken.PUSH_CHANNEL_ID.getToken());
    }

    @Test(expected = APNSGatewayConnectionException.class)
    public void testPushBadConnection() throws APNSGatewayConnectionException, InvalidPushTokenException {
        LOG.info("testPushBadConnection");

        final APNSProperties apnsProperties = new APNSProperties(
                "gateway.sandbox.push.apple.com", 2195,
                "feedback.sandbox.push.apple.com", 2196,
                "/com/tomtom/speedtools/push/implementation/apns/tomtomspeedtools-apns-sandbox.p12",
                "wronggggg", 2, true);

        final APNSConnector faultyAPNSConnector = new APNSConnector(apnsProperties);

        final Notification notification = new Notification(
                new Uid<Notification>(),
                "TRIP_STATUS_CHANGE",
                Duration.standardHours(6),
                "Happy New Year!",
                null,
                0);

        faultyAPNSConnector.push(notification, TestToken.PUSH_CHANNEL_ID.getToken());
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Ignore // Do not want this in automatic builds.
    @Test
    public void testFeedback() throws APNSGatewayConnectionException, InvalidPushTokenException {
        LOG.info("testFeedback");

        final PushToken obsoltePushToken = TestToken.PUSH_CHANNEL_ID.getToken();

        // Send a message to a device that does not have the app installed anymore.
        final Notification notification =
                new Notification(new Uid<Notification>(),
                        "TRIP_STATUS_CHANGE",
                        Duration.standardHours(6),
                        "TEST!",
                        new TestPayLoad("123-122", "test message", 4),
                        1);

        apnsConnector.push(notification, obsoltePushToken);

        // Wait for the feedback service to be notified of previous request.
        LOG.info("testFeedback: waiting for feedback service to be notified of previous request.");
        try {
            Thread.sleep(5000);
        } catch (final InterruptedException ignored) {
            LOG.error("testPush: sleeping problems");
        }

        LOG.info("testFeedback: calling feedback service.");
        final Set<PushToken> pushTokens = apnsConnector.getObsoletePushTokens();

        LOG.info("testFeedback: pushTokens.size={}", pushTokens.size());
        for (final PushToken pushToken : pushTokens) {
            LOG.info("testFeedback: pushToken={}", pushToken);
        }

        Assert.assertEquals(1, pushTokens.size());
        Assert.assertEquals(obsoltePushToken, pushTokens.toArray()[0]);
    }

    @Test
    public void testGetSupportedNotificationChannelType() {
        LOG.info("testGetSupportedNotificationChannelType");
        Assert.assertEquals(NotificationChannelType.IOS_ASPN, apnsConnector.getSupportedNotificationChannelType());
    }

    private static class TestPayLoad implements JsonRenderable {
        @Nonnull
        private final String id;
        @Nonnull
        private final String message;
        @Nonnull
        private final Integer number;

        TestPayLoad(@Nonnull final String id,
                    @Nonnull final String message,
                    @Nonnull final Integer number) {
            assert id != null;
            assert message != null;
            assert number != null;

            this.id = id;
            this.message = message;
            this.number = number;
        }

        @Nonnull
        public String getId() {
            return id;
        }

        @Nonnull
        public String getMessage() {
            return message;
        }

        @Nonnull
        public Integer getNumber() {
            return number;
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
