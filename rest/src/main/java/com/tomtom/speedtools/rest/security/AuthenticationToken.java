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
package com.tomtom.speedtools.rest.security;

import com.tomtom.speedtools.checksums.HexString;
import com.tomtom.speedtools.checksums.SHA1Hash;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Authentication token. (Immutable)
 *
 * Authentication tokens are used to authenticate a device which needs to use an API, if you do not wish to send
 * username and password credentials.
 *
 * Constructor: {@link #AuthenticationToken}
 */
public final class AuthenticationToken extends Credential {

    /**
     * The life time of the generated token. Tokens are valid from the last update time until
     * (update time + token life time).
     */
    private static final Duration TOKEN_LIFETIME = Duration.standardDays(365 * 100);

    @Nonnull
    private final DateTime expiry;
    @Nonnull
    private final String tokenHash;
    @Nonnull
    private final String salt;

    /**
     * Create an authentication token.
     *
     * @param credentialUpdateTime       Credential update time.
     * @param credentialStatus           Credential status.
     * @param credentialStatusUpdateTime Credential status update time.
     * @param expiry                     Expiry time (token is valid until and including this time).
     * @param tokenHash                  Hashed authentication token. Cannot be empty. Must be a hex-encoded string (see
     *                                   {@link HexString#getHexByteArray(String)} for further requirements on
     *                                   hex-encoded values). A good value would be, for example, new
     *                                   Uid().toHexString().
     * @param salt                       The salt used to hash this authentication token. Cannot be {@code null}, but
     *                                   can be the empty string. If not empty, must be a hex-encoded string (see {@link
     *                                   HexString#getHexByteArray(String)} for further requirements on non-empty salt
     *                                   values).
     */
    public AuthenticationToken(
            @Nonnull final DateTime credentialUpdateTime,
            @Nonnull final CredentialStatus credentialStatus,
            @Nonnull final DateTime credentialStatusUpdateTime,
            @Nonnull final DateTime expiry,
            @Nonnull final String tokenHash,
            @Nonnull final String salt) {
        super(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime);
        assert expiry != null;
        assert tokenHash != null;
        assert !tokenHash.isEmpty();
        assert HexString.getHexByteArray(tokenHash) != null; // Ensure tokenHash is a valid hex string.
        assert salt != null;
        //noinspection ConstantConditions
        assert salt.isEmpty() || (HexString.getHexByteArray(salt) != null); // Ensure tokenHash is a valid hex string.

        this.expiry = expiry;
        this.tokenHash = tokenHash;
        this.salt = salt;
    }

    /**
     * Create an authentication token.
     *
     * @param credentialUpdateTime Credential update time.
     * @param expiry               Expiry time (token is valid until and including this time).
     * @param tokenHash            Hashed authentication token. Cannot be empty. Must be a hex-encoded string (see
     *                             {@link HexString#getHexByteArray(String)} for further requirements on hex-encoded
     *                             values). A good value would be, for example, new Uid().toHexString().
     * @param salt                 The salt used to hash this authentication token. Cannot be {@code null}, but can be
     *                             the empty string. If not empty, must be a hex-encoded string (see {@link
     *                             HexString#getHexByteArray(String)} for further requirements on non-empty salt
     *                             values).
     */
    public AuthenticationToken(
            @Nonnull final DateTime credentialUpdateTime,
            @Nonnull final DateTime expiry,
            @Nonnull final String tokenHash,
            @Nonnull final String salt) {
        this(credentialUpdateTime, CredentialStatus.TRY_1, credentialUpdateTime, expiry, tokenHash, salt);
    }

    /**
     * Convenience constructor to create a token with an expiry time specified as a duration relative to creation time.
     *
     * @param credentialUpdateTime Credential update time.
     * @param lifetime             Lifetime, counted from 'created'.
     * @param tokenHash            Hashed authentication token. Cannot be empty. Must be a hex-encoded string (see
     *                             {@link HexString#getHexByteArray(String)} for further requirements on hex-encoded
     *                             values). A good value would be, for example, new Uid().toHexString().
     * @param salt                 The salt used to hash this authentication token. Cannot be {@code null}, but can be
     *                             the empty string. If not empty, must be a hex-encoded string (see {@link
     *                             HexString#getHexByteArray(String)} for further requirements on non-empty salt
     *                             values).
     */
    public AuthenticationToken(
            @Nonnull final DateTime credentialUpdateTime,
            @Nonnull final Duration lifetime,
            @Nonnull final String tokenHash,
            @Nonnull final String salt) {
        this(credentialUpdateTime, CredentialStatus.TRY_1, credentialUpdateTime, credentialUpdateTime.plus(lifetime),
                tokenHash, salt);
        assert lifetime != null;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation", "UnusedDeclaration"})
    @Deprecated
    private AuthenticationToken() {
        super();
        tokenHash = null;
        expiry = null;
        salt = null;
    }

    /**
     * Generates a new authentication token. A plaintext token will be generated, as well as salt to store the hash of
     * the token with. Returns a {@link AuthenticationTokenHolder holder object} that contains both the newly created
     * {@code AuthenticationToken} as well as the token in plaintext that was used in this {@code AuthenticationToken}.
     *
     * The expiration date of this token will be set approximately 100 years into the future.
     *
     * @return Holder object containing both the newly created {@code AuthenticationToken} as well as the plaintext
     * token.
     */
    @Nonnull
    public static AuthenticationTokenHolder generateAuthenticationToken() {
        final String plaintextToken = UUID.randomUUID().toString().toUpperCase();
        final SHA1Hash tokenHash = hashToken(plaintextToken, null);

        final AuthenticationToken authenticationToken =
                new AuthenticationToken(
                        UTCTime.now(),
                        TOKEN_LIFETIME,
                        tokenHash.toString(),
                        tokenHash.getSalt());

        return new AuthenticationTokenHolder(authenticationToken, plaintextToken);
    }

    /**
     * Hashes the passed in token and returns the hashed token. If {@code salt} is given, then this salt will be used
     * during hashing, otherwise, salt will be generated on the spot while hashing.
     *
     * <em>Note</em> that the token will be converted to upper case before being hashed, hence callers have to ensure
     * that the receiver of the plaintext token always receives the token in upper case as well.
     *
     * @param plaintextToken Plaintext token.
     * @param salt           Salt to use when hashing. If {@code null}, then salt will be generated on the spot during
     *                       hashing. If not {@code null}, must be a hex-encoded string (see {@link
     *                       HexString#getHexByteArray(String)} for further requirements on non-empty salt values).
     * @return Hashed token.
     */
    @Nonnull
    private static SHA1Hash hashToken(@Nonnull final String plaintextToken, @Nullable final String salt) {
        assert plaintextToken != null;

        final String plaintextTokenToHash = plaintextToken.toUpperCase();

        if (salt == null) {
            return SHA1Hash.saltedHash(plaintextTokenToHash);
        } else {
            return SHA1Hash.saltedHash(plaintextTokenToHash, salt);
        }
    }

    /**
     * Expiry time.
     *
     * @return Expiry time.
     */
    @Nonnull
    public DateTime getExpiry() {
        return expiry;
    }

    /**
     * Hashed token (string).
     *
     * @return Hashed token, as a hex-encoded string.
     */
    @Nonnull
    public String getTokenHash() {
        return tokenHash;
    }

    /**
     * Salt used to hash the token (string).
     *
     * @return Salt, as a hex-encoded string.
     */
    @Nonnull
    public String getSalt() {
        return salt;
    }

    /**
     * Setter for {@link #getExpiry()}.
     *
     * @param expiry Expiry time.
     * @return New immutable object.
     */
    @Nonnull
    public AuthenticationToken withExpiry(@Nonnull final DateTime expiry) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                tokenHash, salt);
    }

    /**
     * Setter for {@link #getTokenHash}.
     *
     * @param tokenHash Hashed token. Must be a hex-encoded string (see {@link HexString#getHexByteArray(String)} for
     *                  further requirements on hex-encoded values).
     * @return New immutable object.
     */
    @Nonnull
    public AuthenticationToken withTokenHash(@Nonnull final String tokenHash) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                tokenHash, salt);
    }

    /**
     * Sets a hashed token by hashing the given {@code plaintextToken} with the existing salt.
     *
     * @param plaintextToken Plaintext token.
     * @return New immutable object.
     */
    @Nonnull
    public AuthenticationToken withPlaintextToken(@Nonnull final String plaintextToken) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                hashToken(plaintextToken, salt).toString(), salt);
    }

    /**
     * Setter for {@link #getSalt}.
     *
     * @param salt Salt used to hash the token. Cannot be {@code null}, but can be the empty string. If not empty, must
     *             be a hex-encoded string (see {@link HexString#getHexByteArray(String)} for further requirements on
     *             non-empty salt values).
     * @return New immutable object.
     */
    @Nonnull
    public AuthenticationToken withSalt(@Nonnull final String salt) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                tokenHash, salt);
    }

    @Nonnull
    @Override
    public AuthenticationToken withCredentialUpdateTime(@Nonnull final DateTime credentialUpdateTime) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                tokenHash, salt);
    }

    @Nonnull
    @Override
    public AuthenticationToken withCredentialStatus(@Nonnull final CredentialStatus credentialStatus) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                tokenHash, salt);
    }

    @Nonnull
    @Override
    public AuthenticationToken withCredentialStatusUpdateTime(@Nonnull final DateTime credentialStatusUpdateTime) {
        return new AuthenticationToken(credentialUpdateTime, credentialStatus, credentialStatusUpdateTime, expiry,
                tokenHash, salt);
    }

    /**
     * Return if the token is still valid, given a reference time. A token is only valid if the reference time is
     * somewhere in the interval [last update time, expiry time).
     *
     * Note that a token could have been updated after it has expired, i.e. last update time is equal or greater than
     * the expiry time. In this case, {@code false} is returned, no matter what the reference time is.
     *
     * @param time Reference time (usually 'now').
     * @return {@code true} if the token is valid at the reference time.
     */
    public boolean isValidAt(@Nonnull final DateTime time) {
        assert time != null;
        return ((credentialUpdateTime.isBefore(time) || credentialUpdateTime.isEqual(time)) && expiry.isAfter(time));
    }

    @Override
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof AuthenticationToken;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof AuthenticationToken)) {
            final AuthenticationToken that = (AuthenticationToken) obj;
            eq = that.canEqual(this);
            eq = eq && super.equals(that);
            eq = eq && expiry.equals(that.expiry);
            eq = eq && tokenHash.equals(that.tokenHash);
            eq = eq && salt.equals(that.salt);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return hashCodeSuper(expiry, tokenHash, salt);
    }

    public static class AuthenticationTokenHolder {
        @Nonnull
        private final AuthenticationToken authenticationToken;
        @Nonnull
        private final String plaintextToken;

        private AuthenticationTokenHolder(
                @Nonnull final AuthenticationToken authenticationToken,
                @Nonnull final String plaintextToken) {
            assert authenticationToken != null;
            assert plaintextToken != null;
            this.authenticationToken = authenticationToken;
            this.plaintextToken = plaintextToken;
        }

        @Nonnull
        public AuthenticationToken getAuthenticationToken() {
            return authenticationToken;
        }

        @Nonnull
        public String getPlaintextToken() {
            return plaintextToken;
        }
    }
}

