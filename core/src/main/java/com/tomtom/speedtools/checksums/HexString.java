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

package com.tomtom.speedtools.checksums;

import javax.annotation.Nonnull;

/**
 * Utility class for hexadecimal representation of strings and arrays of bytes.
 */
public final class HexString {

    // Utility class only has private constructor.
    private HexString() {
        assert false;
    }

    /**
     * String to hexify.
     *
     * @param textValue Input textual string (eg. "something"). Cannot be null, cannot be empty.
     * @return Hexadecimal representation of string; the length of the string is always even, as all characters are
     * represented by 2-char hex digits. Cannot be null, cannot be empty.
     */
    @Nonnull
    public static String getHexString(@Nonnull final String textValue) {
        assert textValue != null;
        assert !textValue.isEmpty();

        return getHexString(textValue.getBytes());
    }

    /**
     * Convert an array of bytes to a hex value string.
     *
     * @param bytes Input bytes. Cannot be null, cannot be empty.
     * @return Hexadecimal representation of string (eg. "2abc"); the length of the string is always even as every byte
     * is encoded as a 2-char hex digit pair. Cannot be null, cannot be empty.
     */
    @Nonnull
    public static String getHexString(@Nonnull final byte[] bytes) {
        assert bytes != null;
        assert bytes.length > 0;

        // Create a string of 2-char hex values.
        final StringBuilder result = new StringBuilder();
        for (final byte b : bytes) {
            final String hex = Integer.toString((b & 0xff) + 0x100, 16);
            result.append(hex.substring(1));
        }
        return result.toString();
    }

    /**
     * Convert a hex value string to an array of bytes
     *
     * @param hexValue Input hex value string (eg. "2abc"). Cannot be null, cannot be empty. Number 0-9 and both upper-
     *                 and lowercase hex characters a-f are accepted. If the input value string length is not even, a
     *                 '0' is prepended to it and the string is interpreted as consisting of 2-char hex digit pairs.
     * @return Decoded bytes, or null of the input string was empty.
     * @throws IllegalArgumentException If the hexValue string contains non-hex characters.
     */
    @Nonnull
    public static byte[] getHexByteArray(@Nonnull final String hexValue) {
        assert hexValue != null;
        assert !hexValue.isEmpty();

        // Prepend '0' if odd number of characters.
        final String hex;
        if ((hexValue.length() % 2) == 0) {
            hex = hexValue;
        } else {
            hex = '0' + hexValue;
        }
        assert hex.length() >= 2;

        int i = hex.length() - 1;               // Walks over character pairs.
        int j = (hex.length() / 2) - 1;         // Index into new byte array.
        final byte[] bytes = new byte[j + 1];
        while (i > 0) {

            // Get character pair.
            final int lo = getHexDigitValue(hex.charAt(i));
            final int hi = getHexDigitValue(hex.charAt(i - 1));
            final int b = (hi * 16) + lo;
            assert ((0 <= b) && (b <= 255));

            // Store in byte array.
            //noinspection NumericCastThatLosesPrecision
            bytes[j] = (byte) b;
            i = i - 2;
            j = j - 1;
        }
        return bytes;
    }

    private static int getHexDigitValue(final char ch) throws IllegalArgumentException {
        final char lo = Character.toLowerCase(ch);
        final int result;
        if (('0' <= lo) && (lo <= '9')) {
            result = (int) lo - (int) '0';
        } else if (('a' <= lo) && (lo <= 'f')) {
            result = ((int) lo - (int) 'a') + 10;
        } else {
            throw new IllegalArgumentException("Unknown hex character '" + ch + "' in hex string");
        }
        assert ((0 <= result) && (result <= 15));
        return result;
    }
}
