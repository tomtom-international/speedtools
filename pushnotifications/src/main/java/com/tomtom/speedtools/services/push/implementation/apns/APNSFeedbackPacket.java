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

package com.tomtom.speedtools.services.push.implementation.apns;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import com.tomtom.speedtools.checksums.HexString;
import com.tomtom.speedtools.json.Json;

import static com.tomtom.speedtools.utils.ByteUtils.fromBytesToInt;

/**
 * Class represent a APNS feedback packet containing a device id for which there were failed-delivery attempts.
 * <p/>
 * <p/>
 * See also http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingWIthAPS/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW3
 */
public class APNSFeedbackPacket {
    private static final Logger LOG = LoggerFactory.getLogger(APNSFeedbackPacket.class);

    public static final int PACKET_LENGTH = 38;
    public static final int TOKEN_LENGTH_INDEX = 5;
    public static final int TOKEN_START_INDEX = 6;
    private static final byte TOKEN_LENGTH = (byte) 32;

    /**
     * Timestamp indicating when the APNs determined that the application no longer exists on the device. This value,
     * which is in network order, represents the seconds since 1970, anchored to UTC.
     */
    private final int timestamp;

    /**
     * The device token in String hex encoded format.
     */
    @Nonnull
    final private String token;

    /**
     * Constructor creates an instance by taken a 38 long byte array as input. The first 4 bytes represent a timestamp
     * indicating when the APNs determined that the application no longer exists on the device. The 5th byte is zero and
     * can be omitted. The 6th byte holds the token length, which should be 32. The remaining 32 bytes of the byte array
     * make up the token.
     *
     * @param packet Array of bytes containing the feedback information.
     * @throws IllegalArgumentException in case the array size is different than 38 or when then 6th element indicating
     *                                  the token length is other than 32..
     */
    public APNSFeedbackPacket(@Nonnull final byte[] packet) {
        assert packet != null;

        // Check size.
        if (packet.length != PACKET_LENGTH) {
            LOG.warn("APNSFeedbackPacket: invalid number of bytes, actual length={}, expected length={}", packet.length,
                    PACKET_LENGTH);
            throw new IllegalArgumentException(
                    "Invalid byte array size (" + packet.length + ") expected " + PACKET_LENGTH);
        }

        // First 4 bytes are timestamp.
        timestamp = fromBytesToInt(packet[0], packet[1], packet[2], packet[3]);
        // Byte 5 is zero and can be skipped.
        // Byte 6 is token length, must be 32.
        if (packet[TOKEN_LENGTH_INDEX] != TOKEN_LENGTH) {
            LOG.warn("APNSFeedbackPacket: invalid token length, actual length={}, expected length={}", packet[5],
                    TOKEN_LENGTH);
            throw new IllegalArgumentException("Unexpected token length (" + packet[5] + ") expected " + TOKEN_LENGTH);
        }

        assert (TOKEN_LENGTH + TOKEN_START_INDEX) == PACKET_LENGTH;

        token = HexString.getHexString(Arrays.copyOfRange(packet, TOKEN_START_INDEX, TOKEN_LENGTH + TOKEN_START_INDEX));
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Nonnull
    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
