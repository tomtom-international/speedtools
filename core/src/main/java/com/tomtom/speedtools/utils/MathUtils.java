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

package com.tomtom.speedtools.utils;

import javax.annotation.Nonnull;

import java.util.Random;

@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
public final class MathUtils {

    /**
     * Precision for floating point numbers; used to decide equality.
     */
    public static final double EPSILON = 1.0E-6;
    private final static Random RANDOM = new Random();

    // Utility class only has private constructor.
    private MathUtils() {
        assert false;
    }

    /**
     * Find the minimum in a double array.
     *
     * @param a An array of doubles.
     * @return The minimum value in a.
     */
    public static double findMin(@Nonnull final double[] a) {
        assert a != null;
        assert a.length > 0;
        double min = a[0];
        for (final double x : a) {
            if (min > x) {
                min = x;
            }
        }
        return min;
    }

    /**
     * Find the maximum in a double array.
     *
     * @param a An array of doubles.
     * @return The maximum value in a.
     */
    public static double findMax(@Nonnull final double[] a) {
        assert a != null;
        assert a.length > 0;
        double max = a[0];
        for (final double x : a) {
            if (max < x) {
                max = x;
            }
        }
        return max;
    }

    /**
     * Is a double zero?
     *
     * @param x The double to be tested.
     * @return true iff x is within EPS of 0.0.
     */
    public static boolean isAlmostZero(final double x) {
        return isAlmostEqual(x, 0.0);
    }

    /**
     * Are two doubles equal?
     *
     * @param x One double.
     * @param y The other double.
     * @return true iff x and y are within EPS of each other.
     */
    public static boolean isAlmostEqual(final double x, final double y) {
        return Math.abs(x - y) <= EPSILON;
    }

    /**
     * Is a value between two limits?
     *
     * @param value Value to check.
     * @param min   Minimum. Must be &lt;= max.
     * @param max   Maximum.
     * @return True if min &lt;= value &lt;= max.
     */
    public static boolean isBetween(final int value, final int min, final int max) {
        assert min <= max;
        return (min <= value) && (value <= max);
    }

    /**
     * Is a value between two limits?
     *
     * @param value Value to check.
     * @param min   Minimum. Must be &lt;= max.
     * @param max   Maximum.
     * @return True if min &lt;= value &lt;= max.
     */
    public static boolean isBetween(final double value, final double min, final double max) {
        assert min <= max;
        return (min <= value) && (value <= max);
    }

    /**
     * Limit a value to 2 boundaries.
     *
     * @param value Value to limit.
     * @param min   Minimum (inclusive). Must be &lt;= max.
     * @param max   Maximum (inclusive).
     * @return Closest value min &lt;= value &lt;= max.
     */
    public static int limitTo(final int value, final int min, final int max) {
        assert min <= max;
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Limit a value to 2 boundaries.
     *
     * @param value Value to limit.
     * @param min   Minimum (inclusive). Must be &lt;= max.
     * @param max   Maximum (inclusive).
     * @return Closest value min &lt;= value &lt;= max.
     */
    public static double limitTo(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Produce a random number between [min..max] (inclusive).
     *
     * @param min Minimum (inclusive). Must be &lt;= max.
     * @param max Maximum (inclusive).
     * @return Random number in [min, max].
     */
    public static int random(final int min, final int max) {
        assert min <= max;
        return RANDOM.nextInt(max - min) + min;
    }

    /**
     * Set the randomizer seed.
     *
     * @param seed Seed.
     */
    public static void setRandomSeed(final long seed) {
        RANDOM.setSeed(seed);
    }
}
