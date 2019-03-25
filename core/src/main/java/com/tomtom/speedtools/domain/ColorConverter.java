/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.domain;

import javax.annotation.Nonnull;

/**
 * A simple utility class for color conversions from a CSS-type format (#RRGGBB) to an actual
 * RGB color class.
 */
public final class ColorConverter {

    public static final int COLOR_MIN_LENGTH = 2;
    public static final int COLOR_MAX_LENGTH = 7;

    /**
     * Prevent construction of utility class.
     */
    private ColorConverter() {
        assert false;
    }

    /**
     * Return a RGB color or null if not defined.
     *
     * @param colorCSS Color string, a la CSS ("#RRGGBB").
     * @return RGB color.
     * @throws IllegalArgumentException If the format is nor recognized.
     */
    @Nonnull
    public static ColorRGB8 toColorRGB(@Nonnull final String colorCSS) throws IllegalArgumentException {
        assert colorCSS != null;

        // Trim white space.
        final String value = colorCSS.trim().toUpperCase();

        // Must not be empty and must be more than '#' alone.
        if (value.length() < COLOR_MIN_LENGTH) {
            throw new IllegalArgumentException("Expected format: #RRGGBB");
        }

        // Must start with '#'.
        if (value.charAt(0) != '#') {
            throw new IllegalArgumentException("Should start with '#'");
        }

        // Cannot be longer than '#123456'.
        if (value.length() > COLOR_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Length should be [" + COLOR_MIN_LENGTH + "src/main" + COLOR_MAX_LENGTH + ']');
        }

        // Calculate RGB value.
        int total = 0;
        int factor = 1;
        int i = value.length() - 1;
        while (i >= 1) {
            final char ch = value.charAt(i);
            final int val = Character.digit(ch, 16);
            if (val < 0) {
                throw new IllegalArgumentException("Unrecognized hex digit: " + ch);
            }
            total = total + (val * factor);
            factor = factor * 16;
            --i;
        }

        return new ColorRGB8(total);
    }

    /**
     * Return a CSS color.
     *
     * @param colorRGB8 RGB color string.
     * @return CSS color string.
     */
    @Nonnull
    public static String toColorCSS(@Nonnull final ColorRGB8 colorRGB8) {
        assert colorRGB8 != null;
        final String hex = "000000" + Integer.toHexString(colorRGB8.toIntegerRGB());
        return '#' + hex.substring(hex.length() - 6);
    }
}
