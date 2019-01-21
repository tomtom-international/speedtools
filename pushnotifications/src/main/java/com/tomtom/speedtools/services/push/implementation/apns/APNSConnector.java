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

import com.tomtom.speedtools.checksums.HexString;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.services.push.InvalidPushTokenException;
import com.tomtom.speedtools.services.push.PushNotificationProvider;
import com.tomtom.speedtools.services.push.domain.Notification;
import com.tomtom.speedtools.services.push.domain.NotificationChannelType;
import com.tomtom.speedtools.services.push.domain.PushToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static com.tomtom.speedtools.loghelper.LogHelper.logId;
import static com.tomtom.speedtools.services.push.implementation.apns.APNSFeedbackPacket.PACKET_LENGTH;
import static java.util.Arrays.copyOfRange;

/**
 * Connector that is able to push messages for IOS devices Apple Push Notification Service. This should be a singleton
 * in the context of a single thread because it maintains state.
 * <p>
 * See also
 * http://developer.apple.com/library/mac/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html
 */
public class APNSConnector implements PushNotificationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(APNSConnector.class);

    private static final int MSECS_TO_WAIT_FOR_RESPONSE = 5000;     // APNS directly returns, this should be enough.
    private static final int MSECS_TO_WAIT_FOR_FEEDBACK = 5000;
    private static final int RETURN_PACKET_BYTE_LENGTH = 6;         // Should be >= 0.
    private static final int RETURN_COMMAND_BYTE_INDEX = 0;         // Should be >= 0 and < RETURN_PACKET_BYTE_LENGTH.
    private static final byte RETURN_COMMAND_BYTE = (byte) 8;
    private static final int RETURN_ERROR_CODE_BYTE_INDEX = 1;      // Should be >= 0 and < RETURN_PACKET_BYTE_LENGTH.
    private static final byte RETURN_INVALID_TOKEN_BYTE = (byte) 8;

    @Nonnull
    private final APNSConnectionFactory apnsConnectionFactory;
    @Nonnull
    private final APNSProperties apnsProperties;

    @Nullable
    private APNSGatewayConnection pushConnection = null; // Lazy initialize.

    public APNSConnector(@Nonnull final APNSProperties apnsProperties) {
        assert apnsProperties != null;

        apnsConnectionFactory = new APNSConnectionFactory(
                apnsProperties.getGatewayHost(),
                apnsProperties.getGatewayPort(),
                apnsProperties.getFeedbackHost(),
                apnsProperties.getFeedbackPort(),
                apnsProperties.getP12KeyStorePath(),
                apnsProperties.getP12KeyStorePassword());

        this.apnsProperties = apnsProperties;
    }

    @Override
    public PushToken push(@Nonnull final Notification notification, @Nonnull final PushToken pushToken) throws APNSGatewayConnectionException, InvalidPushTokenException {
        assert notification != null;
        assert pushToken != null;

        LOG.debug("push: pushToken={}", logId(pushToken.getToken()));

        if (!apnsProperties.isEnabled()) {
            LOG.debug("push: notification not sent because connector is disabled, pushToken={}",
                    logId(pushToken.getToken()));
            return pushToken;
        }

        // The first time or in case of faulty connections the APNSGatewayConnection needs to be initialized.
        if (pushConnection == null) {
            LOG.debug("push: fetching new connection, pushToken={}", logId(pushToken.getToken()));
            pushConnection = apnsConnectionFactory.createPushConnection();
            try {
                sendPush(notification, pushToken);
            } catch (final APNSGatewayConnectionException e) {
                // A newly created connection should not fail.
                LOG.warn("push: exception sending notification, pushToken={}", logId(pushToken.getToken()));
                assert pushConnection != null;
                pushConnection.close();
                pushConnection = null;
                // Rethrow exception.
                throw e;
            }
        } else {
            LOG.debug("push: using existing connection, pushToken={}", logId(pushToken.getToken()));
            try {
                sendPush(notification, pushToken);
            } catch (final APNSGatewayConnectionException ignored) {
                LOG.debug("push: existing connection possibly faulty, creating new one and retrying, pushToken={}",
                        logId(pushToken.getToken()));
                // It could be possible that the connection became flaky. Retry once with a new connection!
                assert pushConnection != null;
                pushConnection.close();
                pushConnection = apnsConnectionFactory.createPushConnection();
                try {
                    sendPush(notification, pushToken);
                } catch (final APNSGatewayConnectionException e) {
                    // A newly created connection should not fail.
                    LOG.warn("push: exception sending notification, pushToken={}", logId(pushToken.getToken()));
                    assert pushConnection != null;
                    pushConnection.close();
                    pushConnection = null;
                    // Rethrow exception.
                    throw e;
                }
            }
        }
        // APNS never updates the token. Return original one.
        return pushToken;
    }

    private void sendPush(@Nonnull final Notification notification, @Nonnull final PushToken pushToken) throws APNSGatewayConnectionException, InvalidPushTokenException {
        assert notification != null;
        assert pushToken != null;
        assert pushConnection != null; // Calling methods should have initialized a PushConnection.

        LOG.debug("sendPush: pushToken={}", logId(pushToken.getToken()));

        try {

            // Create aps part of Apple push message.
            final StringBuilder aps = new StringBuilder("\"aps\":{");
            boolean apsNeedsSeparator = false;
            final Integer badge = notification.getBadge();
            if (badge != null) {
                aps.append("\"badge\":").append(badge);
                apsNeedsSeparator = true;
            }
            final String message = notification.getMessage();
            if (message != null) {
                if (apsNeedsSeparator) {
                    aps.append(',');
                }
                aps.append("\"alert\":\"").append(message).append('"');
                aps.append(",\"sound\":\"default\"");
            }
            aps.append('}');

            // Overall payload string builder.
            final StringBuilder pushPayload = new StringBuilder();
            pushPayload.append('{');

            // Add aps part.
            pushPayload.append(aps);

            // Add type field.
            final String type = notification.getType();
            pushPayload.append(",\"type\":\"").append(type).append('"');

            // Add payload field if provided.
            final JsonRenderable notificationPayload = notification.getPayload();
            if (notificationPayload != null) {
                pushPayload.append(",\"payload\":").append(notificationPayload.toJson());
            }
            pushPayload.append('}');
            LOG.debug("sendPush: payload={}", pushPayload);

            // TODO ST-4: Send enhanced packet with expire date.
            // Create Simple Notification.
            final APNSNotificationPacket packet = new APNSNotificationPacket(pushToken, pushPayload.toString());
            final OutputStream outputStream = pushConnection.getOutputStream();
            final byte[] packetBytes = packet.toBytes();
            outputStream.write(packetBytes);
            outputStream.flush();
            LOG.debug("sendPush: notification sent, packet={}", HexString.getHexString(packet.toBytes()));

            // Validate response if it could be expected.
            if (packet.couldResponseBeExpected()) {

                final InputStream inputStream = pushConnection.getInputStream();
                final InputStream in = inputStream;

                final ExecutorService executor = Executors.newSingleThreadExecutor();
                final ByteBuffer byteBuffer = ByteBuffer.allocate(RETURN_PACKET_BYTE_LENGTH);
                int readByte = 0;
                //noinspection NestedTryStatement
                try {
                    while (byteBuffer.hasRemaining() && (readByte >= 0)) {
                        final Future<Integer> future = executor.submit(() -> in.read());
                        readByte = future.get(MSECS_TO_WAIT_FOR_RESPONSE, TimeUnit.MILLISECONDS);
                        if (readByte >= 0) {
                            //noinspection NumericCastThatLosesPrecision
                            byteBuffer.put(((byte) readByte));
                        }
                    }
                } catch (final TimeoutException ignored) {
                    // No response means notification sent successful.
                }

                if (!byteBuffer.hasRemaining()) {

                    /**
                     * Allocated ByteBuffer has the full expected number of bytes. Safe to get elements within range.
                     * All element are guaranteed to be initialized to
                     */
                    if (byteBuffer.get(RETURN_COMMAND_BYTE_INDEX) == RETURN_COMMAND_BYTE) {
                        // Check return code.
                        final byte error = byteBuffer.get(RETURN_ERROR_CODE_BYTE_INDEX);
                        LOG.warn("sendPush: could not push notification, pushToken={}, error={}",
                                logId(pushToken.getToken()), error);

                        if (error == RETURN_INVALID_TOKEN_BYTE) {
                            // This token must be cleared.
                            throw new InvalidPushTokenException(pushToken, "Push token is invalid");
                        }
                    } else {
                        LOG.warn("sendPush: unexpected response, pushToken={}, hexstring.response={}",
                                logId(pushToken.getToken()), HexString.getHexString(byteBuffer.array()));
                    }
                } else {
                    if (byteBuffer.remaining() != RETURN_PACKET_BYTE_LENGTH) {
                        // Something had been written, but not the expected amount of bytes.
                        LOG.warn("sendPush: unexpected response length, pushToken={}, hexstring.response={}",
                                logId(pushToken.getToken()), HexString.getHexString(byteBuffer.array()));
                    }
                }
            }
        } catch (final IOException e) {
            LOG.warn("sendPush: exception sending notification: pushToken={}", logId(pushToken.getToken()), e);
            throw new APNSGatewayConnectionException(e);
        } catch (final InterruptedException e) {
            LOG.warn("sendPush: exception reading response: pushToken={}", logId(pushToken.getToken()), e);
        } catch (final ExecutionException e) {
            LOG.warn("sendPush: could not read response: pushToken={}", logId(pushToken.getToken()), e);
        }
    }

    @Nonnull
    @Override
    public NotificationChannelType getSupportedNotificationChannelType() {
        return NotificationChannelType.IOS_ASPN;
    }

    @Nonnull
    @Override
    public Set<PushToken> getObsoletePushTokens() {
        LOG.debug("getObsoletePushTokens");

        final Set<PushToken> obsoleteTokens;
        obsoleteTokens = new HashSet<>();

        if (!apnsProperties.isEnabled()) {
            LOG.debug("getObsoletePushTokens: not fetching obsolete tokens because connector is disabled");
            return obsoleteTokens;
        }

        APNSGatewayConnection connection = null;

        try {
            connection = apnsConnectionFactory.createFeedbackConnection();

            final InputStream inputStream = connection.getInputStream();
            final InputStream in = inputStream;
            int readByte = 1;
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //noinspection NestedTryStatement
            try {
                while (readByte >= 0) {
                    final Future<Integer> future = executor.submit(() -> in.read());
                    readByte = future.get(MSECS_TO_WAIT_FOR_FEEDBACK, TimeUnit.MILLISECONDS);
                    if (readByte >= 0) {
                        //noinspection NumericCastThatLosesPrecision
                        byteArrayOutputStream.write(((byte) readByte));
                    }
                }
            } catch (final TimeoutException ignored) {
                // No response means not obsolete tokens received.
            }

            final byte[] bytes = byteArrayOutputStream.toByteArray();
            LOG.debug("getObsoletePushTokens: byte.size={}", bytes.length);

            // The total of bytes should be a multiplicity of {@link APNSFeedbackPacket#PACKET_LENGTH}.
            if ((bytes.length % PACKET_LENGTH) != 0) {
                LOG.warn("getObsoletePushTokens: amount of bytes not a multiplicity of {}, length={}, remainder={}",
                        PACKET_LENGTH, bytes.length, (bytes.length % PACKET_LENGTH));
            }

            for (int i = 0; i < bytes.length; i += PACKET_LENGTH) {
                if ((i + PACKET_LENGTH) <= bytes.length) {
                    final APNSFeedbackPacket feedbackPacket =
                            new APNSFeedbackPacket(copyOfRange(bytes, i, i + PACKET_LENGTH));
                    obsoleteTokens.add(new PushToken(feedbackPacket.getToken()));
                } else {
                    // Ignoring remainder bytes and return what is there.
                    LOG.debug("getObsoletePushTokens: ignoring remaining bytes, size={}", (bytes.length - i));
                    assert (i + PACKET_LENGTH) > bytes.length;
                }
            }
        } catch (final IOException e) {
            LOG.warn("getObsoletePushTokens: exception sending notification", e);
        } catch (final InterruptedException e) {
            LOG.warn("getObsoletePushTokens: exception reading response", e);
        } catch (final ExecutionException e) {
            LOG.warn("getObsoletePushTokens: could not read response", e);
        } catch (final APNSGatewayConnectionException e) {
            LOG.warn("getObsoletePushTokens: could not connect to service", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return obsoleteTokens;
    }

    @Override
    public boolean canGetObsoletePushTokens() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return apnsProperties.isEnabled();
    }
}
