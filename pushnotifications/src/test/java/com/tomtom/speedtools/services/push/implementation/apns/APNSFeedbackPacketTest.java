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
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.utils.ByteUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class APNSFeedbackPacketTest {
    private static final Logger LOG = LoggerFactory.getLogger(APNSFeedbackPacketTest.class);

    @Test
    public void testValidBytes() {
        LOG.info("testValidBytes");

        final int timestamp = (int) (UTCTime.now().getMillis() / 1000); // Save cast.
        final String token = "beebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeeb";

        final ByteBuffer byteBuffer = ByteBuffer.allocate(APNSFeedbackPacket.PACKET_LENGTH);
        byteBuffer.put(ByteUtils.intToByteArray(timestamp));
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 32);
        byteBuffer.put(HexString.getHexByteArray(token));

        final APNSFeedbackPacket packet = new APNSFeedbackPacket(byteBuffer.array());
        Assert.assertNotNull(packet);

        Assert.assertEquals(timestamp, packet.getTimestamp());
        Assert.assertEquals(token, packet.getToken());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenLength() {
        LOG.info("testInvalidTokenLength");

        final int timestamp = (int) (UTCTime.now().getMillis() / 1000); // Save cast.
        final String token = "beebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeebeeb";

        final ByteBuffer byteBuffer = ByteBuffer.allocate(APNSFeedbackPacket.PACKET_LENGTH);
        byteBuffer.put(ByteUtils.intToByteArray(timestamp));
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 99);
        byteBuffer.put(HexString.getHexByteArray(token));

        //noinspection ResultOfObjectAllocationIgnored
        new APNSFeedbackPacket(byteBuffer.array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidToken() {
        LOG.info("testInvalidToken");

        final int timestamp = (int) (UTCTime.now().getMillis() / 1000); // Save cast.
        final String token = "beebeebeebeebeebeebeebeebeebeebe"; // Too small.

        final ByteBuffer byteBuffer = ByteBuffer.allocate((token.getBytes().length / 2) + 6);
        byteBuffer.put(ByteUtils.intToByteArray(timestamp));
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 32);
        byteBuffer.put(HexString.getHexByteArray(token));

        //noinspection ResultOfObjectAllocationIgnored
        new APNSFeedbackPacket(byteBuffer.array());
    }
}
