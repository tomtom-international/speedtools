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

package com.tomtom.speedtools.services.push.implementation.gcm;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.services.push.InvalidPushTokenException;
import com.tomtom.speedtools.services.push.PushConnectionException;
import com.tomtom.speedtools.services.push.PushNotificationProvider;
import com.tomtom.speedtools.services.push.domain.Notification;
import com.tomtom.speedtools.services.push.domain.NotificationChannelType;
import com.tomtom.speedtools.services.push.domain.PushToken;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

import static com.tomtom.speedtools.loghelper.LogHelper.logId;

/**
 * Connector that is able to push messages for Google Cloud Messaging. This should be a singleton in the context of a
 * single thread because it maintains state.
 */
public class GCMConnector implements PushNotificationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GCMConnector.class);

    private static final String KEY_TYPE = "type";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_PAYLOAD = "payload";

    private static final int MIN_SECONDS_TO_LIVE = 0;
    private static final int MAX_SECONDS_TO_LIVE = 2419200;

    private final GCMProperties gcmProperties;

    public GCMConnector(@Nonnull final GCMProperties gcmProperties) {
        assert gcmProperties != null;
        this.gcmProperties = gcmProperties;
    }


    @Override
    public PushToken push(@Nonnull final Notification notification, @Nonnull final PushToken pushToken) throws PushConnectionException, InvalidPushTokenException {
        assert notification != null;
        assert pushToken != null;

        LOG.debug("push: pushToken={}", pushToken);

        if (!gcmProperties.isEnabled()) {
            LOG.debug("sendPush: notification not sent because connector is disabled, pushToken={}",
                    logId(pushToken.getToken()));
            return pushToken;
        }

        // Create a Sender.
        final Sender sender = new GCMSender(gcmProperties.getEndpoint(), gcmProperties.getApiKey());

        // Create the Message.
        final String pushMessage = notification.getMessage();
        final JsonRenderable jsonPayload = notification.getPayload();
        final Duration timeToLive = notification.getTimeToLive();

        Builder builder = new Builder().addData(KEY_TYPE, notification.getType());
        if (pushMessage != null) {
            builder = builder.addData(KEY_MESSAGE, pushMessage);
        }

        if (jsonPayload != null) {
            builder = builder.addData(KEY_PAYLOAD, jsonPayload.toJson());
        }

        if (timeToLive != null) {
            // The value of this parameter must be a duration from 0 to 2,419,200 seconds.
            final long seconds = timeToLive.getStandardSeconds();
            if ((seconds >= MIN_SECONDS_TO_LIVE) && (seconds <= MAX_SECONDS_TO_LIVE)) {
                //noinspection NumericCastThatLosesPrecision
                builder.timeToLive((int) seconds); // Safe cast.
            }
        }

        final Message message = builder.build();

        // Send the Message.
        final Result result;
        try {
            result = sender.send(message, pushToken.getToken(), gcmProperties.getRetries());
            LOG.debug("push: notification sent, message={}", message);
        } catch (final IOException e) {
            LOG.error("push: unable to send message, pushToken={}", logId(pushToken.getToken()), e);
            throw new PushConnectionException(e);
        }

        if (result.getMessageId() != null) {
            LOG.debug("push: message delivered to GCM, pushToken={}", logId(pushToken.getToken()));
            final String canonicalRegId = result.getCanonicalRegistrationId();
            if (canonicalRegId != null) {

                /**
                 * Same device has more than on registration ID: update database with new (canonical) registration id.
                 * Make sure to replace the registration ID stored the system with this canonical ID, as eventually the ID you're using will stop working.
                 */
                return new PushToken(canonicalRegId);
            } else {
                return pushToken;
            }
        } else {
            final String error = result.getErrorCodeName();
            LOG.error("push: message could not be delivered, pushToken={}, error={}", logId(pushToken.getToken()),
                    error);
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {

                // Application has been removed from device. Invalidate push token.
                throw new InvalidPushTokenException(pushToken,
                        "Push token is invalid, application has been removed from device");
            }

            if (error.equals(Constants.ERROR_INVALID_REGISTRATION)) {

                // Application registered an invalid registration id. Invalidate push token.
                throw new InvalidPushTokenException(pushToken, "Push token is invalid");
            }

            throw new PushConnectionException("Notification could not be sent: " + error);
        }
    }

    @Nonnull
    @Override
    public NotificationChannelType getSupportedNotificationChannelType() {
        return NotificationChannelType.ANDROID_GCM;
    }

    @Nonnull
    @Override
    public Set<PushToken> getObsoletePushTokens() {
        return Immutables.emptySet();
    }

    @Override
    public boolean canGetObsoletePushTokens() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return gcmProperties.isEnabled();
    }
}
