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

package com.tomtom.speedtools.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple utility class to deal with hash and cryptographic functions. It provides a thin
 * convenience layer on top of Java Security.
 */
public final class Crypto {
    private static final Logger LOG = LoggerFactory.getLogger(Crypto.class);

    /**
     * Initialization code to see if we can get a hasher.
     */
    static {
        try {
            MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("Crypto: Cannot get SHA-256 message digest.", e);
        }
    }

    private Crypto() {
        // Prevent instantiation.
    }

    /**
     * Convert a string into a password hash. A password hash is defined as a SHA-256 hash.
     *
     * @param value Non-empty string.
     * @return Hash of string.
     */
    @Nonnull
    public static String hash(@Nonnull final String value) {
        assert value != null;
        assert !value.isEmpty();

        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(value.getBytes());
            final byte[] bytes = messageDigest.digest();

            // Convert the byte to hex format method 1
            final StringBuilder stringBuffer = new StringBuilder();
            for (final byte b : bytes) {
                stringBuffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuffer.toString();
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("Crypto: Cannot get SHA-256 message digest.", e);

            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }
}
