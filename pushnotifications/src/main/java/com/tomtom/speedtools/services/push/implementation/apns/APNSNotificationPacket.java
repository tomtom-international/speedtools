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

package com.tomtom.speedtools.services.push.implementation.apns;

import com.tomtom.speedtools.checksums.HexString;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.services.push.domain.PushToken;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.tomtom.speedtools.utils.ByteUtils.intToByteArray;

/**
 * Apple Push Notification Service Notification Packet.
 *
 * In case the enhanced notification format is used the following return code could be expected:
 *
 * 0    No errors encountered (never actually returned)
 *
 * 1    Processing error
 *
 * 2    Missing device token
 *
 * 3    Missing topic
 *
 * 4    Missing payload
 *
 * 5    Invalid token size
 *
 * 6    Invalid topic size
 *
 * 7    Invalid payload size
 *
 * 8    Invalid token
 *
 * 255  None (unknown)
 *
 * See also http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingWIthAPS/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW4
 */
public class APNSNotificationPacket {
    private static final Logger LOG = LoggerFactory.getLogger(APNSNotificationPacket.class);

    private static final byte SIMPLE_NOTIFICATION_FORMAT_COMMAND = (byte) 0;
    private static final byte ENHANCED_NOTIFICATION_FORMAT_COMMAND = (byte) 1;

    private final byte command;

    /**
     * An arbitrary value that identifies this notification. This same identifier is returned in a error-response packet
     * if APNs cannot interpret a notification.
     */
    private final int identifier;

    /**
     * A fixed UNIX epoch date expressed in seconds (UTC) that identifies when the notification is no longer valid and
     * can be discarded. The expiry value should be in network order (big endian). If the expiry value is positive, APNs
     * tries to deliver the notification at least once. You can specify zero or a value less than zero to request that
     * APNs not store the notification at all.
     */
    private final int expiry;

    @Nonnull
    final private PushToken pushToken;
    @Nonnull
    final private String payload;

    /**
     * * Constructs an Enhanced notification format.
     *
     * @param identifier An arbitrary value that identifies this notification.
     * @param expiry     The expiry date
     * @param pushToken  The device push token.
     * @param payload    The payload to send.
     */
    public APNSNotificationPacket(
            final int identifier,
            @Nonnull final DateTime expiry,
            @Nonnull final PushToken pushToken,
            @Nonnull final String payload) {

        assert expiry != null;
        assert pushToken != null;
        assert payload != null;

        this.command = ENHANCED_NOTIFICATION_FORMAT_COMMAND;
        this.identifier = identifier;
        this.expiry = UTCTime.dateTimeToUTCTimestamp(expiry);
        this.pushToken = pushToken;
        this.payload = payload;
    }

    /**
     * Constructs a Simple notification format.
     *
     * @param pushToken The device push token.
     * @param payload   The payload to send.
     */
    public APNSNotificationPacket(
            @Nonnull final PushToken pushToken,
            @Nonnull final String payload) {

        assert pushToken != null;
        assert payload != null;

        this.command = SIMPLE_NOTIFICATION_FORMAT_COMMAND;
        this.identifier = 0;
        this.expiry = 0;
        this.pushToken = pushToken;
        this.payload = payload;
    }

    /**
     * Only the Enhanced notification format could return a response in case it could not handle the notification.
     *
     * @return True in case a response can be expected, false otherwise.
     */
    public boolean couldResponseBeExpected() {
        return command == ENHANCED_NOTIFICATION_FORMAT_COMMAND;
    }

    /**
     * Method returns a byte array representation of the notification packet.
     *
     * @return A byte array representation of the notification packet.
     * @throws IOException In case bytes could not be written to ByteArrayOutputStream this exception is thrown.
     */
    @Nonnull
    public byte[] toBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final byte[] tokenBytes = HexString.getHexByteArray(pushToken.getToken());

        try {
            baos.write(command); // First byte is the command.
            if (command == ENHANCED_NOTIFICATION_FORMAT_COMMAND) {

                /**
                 * Enhanced notification format. With this format, if APNs encounters an unintelligible command,
                 * it returns an error response before disconnecting.
                 */
                baos.write(intToByteArray(identifier)); // Identifier of 4 bytes.
                baos.write(intToByteArray(expiry)); // Expiry of 4 bytes UNIX epoch date expressed in seconds (UTC).
            }
            baos.write((byte) 0);
            baos.write(tokenBytes.length); // Token length.
            baos.write(tokenBytes);
            baos.write((byte) 0);
            baos.write(payload.length()); // Payload length.
            baos.write(payload.getBytes());
            return baos.toByteArray(); // Finally is called before return.
        } finally {
            try {
                baos.close();
            } catch (final IOException e) {
                LOG.error("toBytes: expection encountered closing ByteArrayOutputStream", e);
            }
        }
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
