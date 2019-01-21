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

package com.tomtom.speedtools.checksums;

import com.tomtom.speedtools.objects.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * Class for SHA1 hashes.
 */
public final class SHA1Hash {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(SHA1Hash.class);

    @Nonnull
    private static final String SHA1_ALGORITHM = "SHA-1";
    @Nonnull
    private static final String SHA1PRNG_ALGORITHM = "SHA1PRNG";
    @Nonnull
    private static final String UTF8_ENCODING = "UTF-8";

    @Nonnull
    private final String hash;
    @Nonnull
    private final String salt;

    /**
     * Used internally only.
     *
     * @param hash Hash, as a hex-encoded string.
     * @param salt Salt, as a hex-encoded string.
     */
    private SHA1Hash(@Nonnull final String hash, @Nonnull final String salt) {
        assert hash != null;
        assert salt != null;
        this.hash = hash;
        this.salt = salt;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private SHA1Hash() {
        super();
        hash = null;
        salt = null;
    }

    /**
     * Hashes the given {@code value} without salt.
     *
     * <em>Note</em> that in case the hashing process fails, an object will be returned that contains an empty string
     * for its hash.
     *
     * @param value The string to hash.
     * @return The hashed {@code value}.
     */
    @Nonnull
    public static SHA1Hash hash(@Nonnull final String value) {
        assert value != null;

        return doHash(value, null);
    }

    /**
     * Hashes the given {@code value} using a salt generated during the hash operation. Each invocation of this method,
     * even for the same {@code value}, will use a different salt.
     *
     * <em>Note</em> that is the salt generation fails, hashing will proceed using no salt, and the returned object will
     * contain an empty string for its salt value.
     *
     * <em>Note</em> that in case the hashing process fails, an object will be returned that contains an empty string
     * for its hash.
     *
     * @param value The string to hash using salt.
     * @return The hashed {@code value} using salt.
     */
    @Nonnull
    public static SHA1Hash saltedHash(@Nonnull final String value) {
        assert value != null;

        // See https://www.owasp.org/index.php/Hashing_Java for implementing secure password hashing in Java.
        byte[] salt = null;
        try {
            // Generate 64 random bits using a cryptographically strong random number generator.
            final SecureRandom random = SecureRandom.getInstance(SHA1PRNG_ALGORITHM);
            salt = new byte[8];
            random.nextBytes(salt);
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("saltedHash: Serious failure: No algorithm found for salt generation use {}. Proceeding " +
                    "without salting the input. {}", SHA1PRNG_ALGORITHM, e.getMessage());
        }
        return doHash(value, salt);
    }

    /**
     * Hashes the given {@code value} using the provided {@code salt}.
     *
     * <em>Note</em> that in case the hashing process fails, an object will be returned that contains an empty string
     * for its hash.
     *
     * @param value The string to hash using salt using the given {@code salt}.
     * @param salt  The salt to use to hash the given {@code value} with. Can be an empty string, in which case no salt
     *              will be used. If not empty, must be a hex-encoded string (see {@link
     *              HexString#getHexByteArray(String)} for further requirements on non-empty salt values).
     * @return The hashed {@code value} using the given {@code salt}.
     * @throws IllegalArgumentException If the {@code salt} string contains non-hex characters.
     */
    @Nonnull
    public static SHA1Hash saltedHash(@Nonnull final String value, @Nonnull final String salt) {
        assert value != null;
        assert salt != null;

        final byte[] saltBytes = salt.isEmpty() ? null : HexString.getHexByteArray(salt);
        return doHash(value, saltBytes);
    }

    /**
     * Returns the salt used during hashing as a hex-encoded string. Can be an empty string if no salt was used, or salt
     * generation failed.
     *
     * @return Salt for this hash.
     */
    @Nonnull
    public String getSalt() {
        return salt;
    }

    /**
     * Hashes the given {@code value}. If {@code saltBytes} are provided, then they will be used during the hashing
     * process.
     *
     * @param value     The string to hash, using {@code saltBytes} if not {@code null}.
     * @param saltBytes The salt to use during hashing. Can be {@code null} if no salt should be used.
     * @return An object providing access to the hash and the salt.
     */
    @Nonnull
    private static SHA1Hash doHash(@Nonnull final String value, @Nullable final byte[] saltBytes) {
        assert value != null;

        String hash = "";
        String salt = "";

        try {
            final MessageDigest digest = MessageDigest.getInstance(SHA1_ALGORITHM);
            if (saltBytes != null) {
                digest.update(saltBytes);
                salt = HexString.getHexString(saltBytes);
            }
            digest.update(value.getBytes(UTF8_ENCODING), 0, value.length());
            hash = HexString.getHexString(digest.digest());
        } catch (final UnsupportedEncodingException e) {
            LOG.error("doHash: Serious failure: Unsupported encoding for {}. Resulting hash will be the empty " +
                    "string. {}", UTF8_ENCODING, e.getMessage());
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("doHash: Serious failure: No algorithm found for hashing use {}. Resulting hash will be the " +
                    "empty string. {}", SHA1_ALGORITHM, e.getMessage());
        }

        return new SHA1Hash(hash, salt);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof SHA1Hash;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof SHA1Hash)) {
            final SHA1Hash that = (SHA1Hash) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && hash.equals(that.hash);
            eq = eq && salt.equals(that.salt);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hash, salt);
    }

    /**
     * Returns the hash, as a hex-encoded string.
     *
     * @return Hash, as a hex-encoded string.
     */
    @Nonnull
    @Override
    public String toString() {
        return hash;
    }
}
