/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.objects.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.HashMap;

/**
 * This class deals with GeoHash functionality. Base source taken from LBS team (thanks Eric Bowman).
 *
 * See http://en.wikipedia.org/wiki/Geohash and http://geospatialweb.googlecode.com.
 */
public final class GeoHash {
    final private String hash;
    final private GeoPoint point;

    private static final int NUMBER_OF_BITS = 30;

    private final static char[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    @Nonnull
    private final static HashMap<Character, Integer> lookup = new HashMap<>();

    /**
     * Static initialization of class. Fill 'lookup' table.
     */
    static {
        int value = 0;
        for (final char c : digits) {
            lookup.put(c, value);
            ++value;
        }
    }

    /**
     * Create a GeoHash from a hash string.
     *
     * @param hash Geo hash string.
     */
    public GeoHash(
            @Nonnull final String hash) throws IllegalArgumentException {
        assert hash != null;
        this.hash = hash;
        this.point = decode(hash);
    }

    /**
     * Create a GeoHash from a point.
     *
     * @param point GeoPoint.
     */
    public GeoHash(
            @Nonnull final GeoPoint point) {
        assert point != null;
        this.point = point;
        this.hash = encode(point);
    }

    /**
     * Return the geo hash string of this object. This is not the same as the hashCode.
     *
     * @return Geo hash string.
     */
    @Nonnull
    public String getHash() {
        return hash;
    }

    /**
     * Return the point (coordinate) of this geo hash.
     *
     * @return GeoPoint coordinate.
     */
    @Nonnull
    public GeoPoint getPoint() {
        return point;
    }

    /**
     * Creates a new GeoHash using the same resolution (length) as the current object to contain a new point. This
     * method essentially moves the GeoHash rectangle around if needed.
     *
     * @param point Point to be contained by new object.
     * @return New GeoHash with same resolution that contains the specified point.
     */
    @Nonnull
    public GeoHash moveTo(@Nonnull final GeoPoint point) {
        assert point != null;
        final String child = encode(point);
        final int len = hash.length();
        assert len > 0;
        final GeoHash resized = setResolution(child, len);
        assert resized != null;
        return resized;
    }

    /**
     * Returns a new GeoHash object with the same resolution as the specified object.
     *
     * @param other GeoHash object that specifies the resolution.
     * @return New GeoHash object with same resolution.
     */
    @Nonnull
    public GeoHash useResolution(@Nonnull final GeoHash other) {
        assert other != null;
        final int len = other.length();
        assert len > 0;
        final GeoHash resized = this.setResolution(len);
        assert resized != null;
        return resized;
    }

    /**
     * Shortens the size of the GeoHash by one character, effectively decreasing the resolution.
     *
     * @return GeoHash with one character removed from the right. May be null.
     */
    @Nullable
    public GeoHash decreaseResolution() {
        return decreaseResolution(1);
    }

    /**
     * Shortens the size of the GeoHash, so removes characters from the right, effectively decreasing the resolution.
     *
     * @param amount The amount of characters to remove, must be 0 or bigger.
     * @return GeoHash with characters removed from the right. May be null.
     */
    @Nullable
    public GeoHash decreaseResolution(final int amount) {
        assert amount >= 0;

        if (amount >= hash.length()) {
            return null;
        }

        try {
            return new GeoHash(hash.substring(0, hash.length() - amount));
        } catch (final IllegalArgumentException ignored) {
            // This should not happen.
            assert false;
            return null;
        }
    }

    /**
     * Shortens the size of the GeoHash by an absolute value, so removes characters from the right.
     *
     * @param length The new size of the GeoHash, must be &gt; 0.
     * @return GeoHash with characters removed from the right.
     */
    @Nullable
    public GeoHash setResolution(final int length) {
        assert length > 0;
        return setResolution(hash, length);
    }

    /**
     * Creates a GeoHash from a given has string and length.
     *
     * @param hash   String to be encoded.
     * @param length Required length as number of characters.
     * @return The created GeoHash.
     */
    @Nullable
    private GeoHash setResolution(@Nonnull final String hash, final int length) {
        assert hash != null;
        assert length > 0;

        try {
            // Forgive any value of length that is too big.
            return new GeoHash(hash.substring(0, Math.min(length, this.hash.length())));
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Specifies whether a GeoHash is contained within this one. Note that a GeoHash always contains itself.
     *
     * @param other Other GeoHash.
     * @return True if this GeoHash contains the other one.
     */
    public boolean contains(@Nonnull final GeoHash other) {
        assert other != null;
        return other.hash.startsWith(hash);
    }

    /**
     * Gets the hash length.
     *
     * @return The length in number of characters.
     */
    public int length() {
        return hash.length();
    }

    /**
     * Encodes a GeoPoint into a geoHash string.
     *
     * @param point GeoPoint to be encoded.
     * @return The created String.
     */
    @Nonnull
    public static String encode(@Nonnull final GeoPoint point) {
        assert point != null;
        return encode(point.getLat(), point.getLon());
    }

    @Nonnull
    public static String encode(final double lat, final double lon) {
        final BitSet latBits = getBits(lat, -90.0, 90.0);
        final BitSet lonBits = getBits(lon, -180.0, 180.0);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NUMBER_OF_BITS; ++i) {
            sb.append((lonBits.get(i)) ? '1' : '0');
            sb.append((latBits.get(i)) ? '1' : '0');
        }
        return base32(Long.parseLong(sb.toString(), 2));
    }

    /**
     * Decodes a geoHash string into a GeoPoint.
     *
     * @param hash String representation of a GeoHash.
     * @return The created GeoPoint.
     * @throws IllegalArgumentException when an invalid hash was given.
     */
    @Nonnull
    public static GeoPoint decode(@Nonnull final String hash) throws IllegalArgumentException {
        assert hash != null;

        if (!isValid(hash)) {
            throw new IllegalArgumentException("Invalid GeoHash value.");
        }

        final StringBuilder sb = new StringBuilder();
        for (final char c : hash.toCharArray()) {

            final int i = lookup.get(c) + 32; // Should not return null, already checked.
            sb.append(Integer.toString(i, 2).substring(1));
        }

        final BitSet lonSet = new BitSet();
        final BitSet latSet = new BitSet();

        // Even bits.
        int j = 0;
        for (int i = 0; i < (NUMBER_OF_BITS * 2); i += 2) {
            boolean isSet = false;
            if (i < sb.length()) {
                isSet = (sb.charAt(i) == '1');
            }
            lonSet.set(j, isSet);
            ++j;
        }

        // Odd bits.
        j = 0;
        for (int i = 1; i < (NUMBER_OF_BITS * 2); i += 2) {
            boolean isSet = false;
            if (i < sb.length()) {
                isSet = (sb.charAt(i) == '1');
            }
            latSet.set(j, isSet);
            ++j;
        }
        final double lon = decode(lonSet, -180.0, 180.0);
        final double lat = decode(latSet, -90.0, 90.0);

        return new GeoPoint(lat, lon);
    }

    /**
     * Return whether a hash is a valid Geo Hash.
     *
     * @param hash String representation of GeoHash.
     * @return True if this is a valid GeoHash.
     */
    public static boolean isValid(@Nullable final String hash) {
        if (hash == null) {
            return false;
        }
        for (final char c : hash.toCharArray()) {
            if (lookup.get(c) == null) {
                return false;
            }
        }
        // Hashes cannot be zero length.
        return !hash.isEmpty();
    }

    /**
     * Decodes a given bitSet.
     *
     * @param bitSet The BitSet to be decoded.
     * @return True if this GeoHash contains the other one.
     */
    private static double decode(@Nonnull final BitSet bitSet, final double min, final double max) {
        assert bitSet != null;
        double floor = min;
        double ceil = max;
        double mid = 0;
        for (int i = 0; i < bitSet.length(); ++i) {
            mid = (floor + ceil) / 2;
            if (bitSet.get(i)) {
                floor = mid;
            } else {
                ceil = mid;
            }
        }
        return mid;
    }

    /**
     * Converts a latitude or longitude within a range into a BitSet.
     *
     * @param degrees Double representation of the latitude or longitude.
     * @param min     Bottom value of the degree range.
     * @param max     Top value of the degree range.
     * @return The created BitSet.
     */
    @Nonnull
    private static BitSet getBits(final double degrees, final double min, final double max) {
        double floor = min;
        double ceil = max;
        final BitSet bitSet = new BitSet(NUMBER_OF_BITS);
        for (int i = 0; i < NUMBER_OF_BITS; i++) {
            final double mid = (floor + ceil) / 2;
            if (degrees >= mid) {
                bitSet.set(i);
                floor = mid;
            } else {
                ceil = mid;
            }
        }
        return bitSet;
    }

    /**
     * Converts a long value into a base32 string.
     *
     * @param value Value to be encoded.
     * @return The created base32 string.
     */
    @Nonnull
    private static String base32(final long value) {
        long nr = value;
        final char[] buf = new char[65];
        int charPos = 64;
        final boolean negative = (nr < 0);
        if (!negative) {
            nr = -nr;
        }
        while (nr <= -32) {
            // Note: nr % 32 is always in the range of int; hence the noinspection.
            //noinspection NumericCastThatLosesPrecision
            buf[charPos] = digits[(int) (-(nr % 32))];
            --charPos;
            nr /= 32;
        }
        assert (-32 < nr) && (nr <= 0);
        // Note: nr is always in the range of int; hence the noinspection.
        //noinspection NumericCastThatLosesPrecision
        buf[charPos] = digits[(int) (-nr)];

        if (negative) {
            --charPos;
            buf[charPos] = '-';
        }
        return new String(buf, charPos, (65 - charPos));
    }

    /**
     * Detects whether an object can be compared to this instance.
     *
     * @param obj The Object to be tested.
     * @return True if obj is a GeoHash instance.
     */
    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof GeoHash;
    }

    /**
     * Converts the instance to a JSON encoded string.
     *
     * @return The created string.
     */
    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }

    /**
     * Detects whether a given object is equal to this instance.
     *
     * @param obj The object to be tested.
     * @return True if obj is equal to this instance.
     */
    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof GeoHash)) {
            final GeoHash that = (GeoHash) obj;
            eq = that.canEqual(this);
            // Top-level entity, so don't: super.equals(that)
            eq = eq && hash.equals(that.hash);
            eq = eq && point.equals(that.point);
        } else {
            eq = false;
        }

        return eq;
    }

    /**
     * Returns the hashCode of this instance.
     *
     * @return The int value of the hasCode.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(hash, point);
    }
}
