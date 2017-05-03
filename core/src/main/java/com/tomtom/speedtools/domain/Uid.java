/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.domain;

import com.tomtom.speedtools.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.UUID;


/**
 * Generic Unique ID class, immutable. Basically just an abstraction of UUIDs. Used to unique identify... well,
 * anything, really. No 2 Uid objects shall be equal unless they are the same instance of the Uid class or their
 * underlying UUIDs are equal.
 *
 * Note that type parameter T is not actually used by this class. It just serves to model type-safe references in model
 * code.
 *
 * Note also that this class represents UUID as a String internally, to avoid loads of UUID to String conversions all
 * the time. This makes the class considerably faster in use than the regular {@link Uid} class.
 */
@SuppressWarnings("UnusedDeclaration")
public final class Uid<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final String uuid;

    private static final char UUID_DASH = '-';
    private static final int UUID_MIN_LENGTH = 9;
    private static final int UUID_MAX_LENGTH = 36;
    private static final int[] UUID_DASH_POS = {8, 13, 18, 23};


    /**
     * Constructor. Simply creates a random UUID.
     */
    public Uid() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Instantiates an id with a string. Mainly used when de-serializing existing entities.
     *
     * @param uuid Existing string id.
     * @throws IllegalArgumentException If name does not conform to the string representation as described in {@link
     *                                  UUID#toString}. Use {@link #isValid(String)} to make sure the string is valid.
     */
    public Uid(@Nonnull final String uuid) {
        assert uuid != null;

        /**
         * This code has been optimized to NOT just call UUID.fromString(uuid) to convert the uuid-String into
         * a String (and catch an IllegalArgumentException).
         *
         * So, the format of 'uuid' is checked to comply with a standard UUID format, which is:
         *
         * - Dashes at positions 8, 13, 18, 23 (base 0).
         *
         * - Characters 0-9 and a-f (lowercase only).
         *
         * If the uuid does not comply, the expensive call to UUID.fromString is made after all.
         */
        // Check length.
        final int length = uuid.length();
        if (!MathUtils.isBetween(length, UUID_MIN_LENGTH, UUID_MAX_LENGTH)) {
            throw new IllegalArgumentException("Length of UUID must be [" + UUID_MIN_LENGTH + ", " +
                    UUID_MAX_LENGTH + "], but is " + uuid.length() + ", uuid=" + uuid);
        }

        // Check dashes.
        for (final int i : UUID_DASH_POS) {
            if ((length < (i + 1)) || (uuid.charAt(i) != UUID_DASH)) {

                // This will throw an IllegalArgumentException if it went wrong.
                this.uuid = UUID.fromString(uuid).toString().toLowerCase();
                return;
            }
        }

        // Make sure the UUID is lowercase.
        this.uuid = uuid.toLowerCase();

        // UUID seems to be OK.
        if (!onlyContainsValidUUIDCharacters(this.uuid)) {
            throw new IllegalArgumentException("Incorrect UUID format, uuid=" + uuid);
        }
    }

    /**
     * Instantiates an id with a {@link UUID}.
     *
     * @param uuid Existing {@link UUID}.
     */
    private Uid(@Nonnull final UUID uuid) {
        assert uuid != null;
        this.uuid = uuid.toString();
    }

    /**
     * Return whether an ID is a valid UUID.
     *
     * @param id String representation of UUID.
     * @return True if this is a valid UUID. The construction of a Uid() from this id is guaranteed succeed in that
     * case.
     */
    public static boolean isValid(@Nullable final String id) {
        if (id == null) {
            return false;
        }
        try {
            //noinspection ResultOfObjectAllocationIgnored
            new Uid(id);
            return true;
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * Instantiates a Uid with given id. Mainly used for deserialization. Opposite of {@link #toString()}.
     *
     * @param <T> Uid type.
     * @param id  String representation of id.
     * @return Uid.
     * @throws IllegalArgumentException If name does not conform to the string representation as described in {@link
     *                                  UUID#toString}. Use {@link #isValid(String)} to make sure the string is valid.
     */
    @Nonnull
    public static <T> Uid<T> fromString(@Nonnull final String id) {
        assert id != null;
        return new Uid<>(id);
    }

    /**
     * Instantiates a Uid with given id. Opposite of {@link #toHexString()}.
     *
     * @param <T> Uid type.
     * @param id  Hex string representation of id, must be exactly 32 characters long.
     * @return Uid.
     * @throws IllegalArgumentException If name does not conform to the string representation as described in {@link
     *                                  UUID#toString}. Use {@link #isValid(String)} to make sure the string is valid.
     */
    @Nonnull
    public static <T> Uid<T> fromHexString(@Nonnull final String id) {
        assert id != null;
        assert id.length() == 32;

        final long msb = (Long.parseLong(id.substring(0, 8), 16) << 32) | (Long.parseLong(id.substring(8, 16), 16));
        final long lsb = (Long.parseLong(id.substring(16, 24), 16) << 32) | (Long.parseLong(id.substring(24, 32), 16));

        return new Uid<>(new UUID(msb, lsb));
    }

    /**
     * Returns hex string representation of this Uid. Opposite of {@link #fromHexString(String)}.
     *
     * @return Hex string representation of id, exactly 32 characters long.
     */
    @Nonnull
    public String toHexString() {
        final UUID uuid2 = UUID.fromString(uuid);
        final String msb = Long.toHexString(uuid2.getMostSignificantBits());
        final String lsb = Long.toHexString(uuid2.getLeastSignificantBits());
        final StringBuilder sb = new StringBuilder();
        for (int i = 16 - msb.length(); i > 0; i--) {
            sb.append('0');
        }
        sb.append(msb);
        for (int i = 16 - lsb.length(); i > 0; i--) {
            sb.append('0');
        }
        sb.append(lsb);
        final String value = sb.toString();
        assert value.length() == 32;
        return value;
    }

    @SuppressWarnings({"unchecked", "UnusedParameters"})
    @Nonnull
    public <T> Uid<T> as(@Nonnull final Class<T> ignored) {
        return (Uid<T>) this;
    }

    /**
     * Method converts given String representation to Uid and compares it with this instance. A String value of
     * "0-0-0-0-0" would match a UID of "00000-0000-0000-000000000-00" or so.
     *
     * @param uid String representation od Uid.
     * @return True in case String representation matches instance. False otherwise.
     */
    public boolean matchesFromString(@Nonnull final String uid) {
        assert uid != null;
        return this.equals(Uid.<T>fromString(uid));
    }

    /**
     * Return if string contains valid UUID characters only. Must be converted to lowercase already.
     *
     * @param uuid Input UUID. Should be lowercase already.
     * @return True if valid characters only.
     */
    private static boolean onlyContainsValidUUIDCharacters(@Nonnull final String uuid) {
        for (final char ch : uuid.toCharArray()) {
            if (!((('0' <= ch) && (ch <= '9')) || (('a' <= ch) && (ch <= 'f')) || (ch == UUID_DASH))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        final boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj instanceof Uid)) {
            final Uid<?> that = (Uid) obj;
            eq = that.uuid.equals(uuid);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * Returns string representation of this Uid. Opposite of {@link #fromString(String)}.
     *
     * @return String representation of id.
     */
    @Override
    @Nonnull
    public String toString() {
        return uuid;
    }
}
