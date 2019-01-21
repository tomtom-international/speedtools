/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.utils;

import javax.annotation.Nonnull;

/**
 * Util class for byte relate operations and conversions.
 */
public final class ByteUtils {

    private ByteUtils() {
        // Prevent instantiation.
    }

    /**
     * Method converts given byte array of 4 bytes into a signed integer.
     *
     * @param bytes Byte array of 4 that needs to be converted to a signed integer.
     * @return Integer value that is represented by the 4 given bytes.
     * @throws IllegalArgumentException In case the byte length is not equal to 4.
     */
    public static int fromBytesToInt(@Nonnull final byte[] bytes) {
        assert bytes != null;

        if (bytes.length != 4) {
            throw new IllegalArgumentException("Can only convert 4 bytes to an integer");
        }

        return fromBytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
    }


    /**
     * Method converts given 4 bytes into a signed integer.
     *
     * @param byte1 First byte.
     * @param byte2 Second byte.
     * @param byte3 Third byte.
     * @param byte4 Fourth byte.
     * @return Integer value that is represented by the 4 given bytes.
     */
    public static int fromBytesToInt(final byte byte1, final byte byte2, final byte byte3, final byte byte4) {
        int i = 0;

        i += unsignedByteToInt(byte1) << 24;
        i += unsignedByteToInt(byte2) << 16;
        i += unsignedByteToInt(byte3) << 8;
        i += unsignedByteToInt(byte4);

        return i;
    }

    /**
     * Converts a byte to an unsigned integer.
     *
     * @param b The byte to convert.
     * @return an unsigned integer.
     */
    public static int unsignedByteToInt(final byte b) {
        return (int) b & 0xFF;
    }

    /**
     * Method converts given integer into a byte array.
     *
     * @param value The integer value to convert to 4 bytes
     * @return An array of 4 bytes representing the given signed integer.
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Nonnull
    public static byte[] intToByteArray(final int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }
}
