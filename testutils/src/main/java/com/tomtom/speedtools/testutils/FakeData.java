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

package com.tomtom.speedtools.testutils;

import de.svenjacobs.loremipsum.LoremIpsum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * The class "Fake" contains all sorts of the functions to generate "fake" times or texts that can
 * be used in simulations.
 */
public final class FakeData {

    // Static objects.
    private static final LoremIpsum LOREM_IPSUM = new LoremIpsum();

    // Utility class only has private constructor.
    private FakeData() {
        assert false;
    }

    /**
     * General utility to format a time string as "HH:MM". Can be used in combination with randomEta to generate a nice
     * ETA string. Example: String s = formatTimeAsHHMM(randomEta(new Date(), 60)).
     *
     * @param date Time to be formatted. If null, current time is used.
     * @return Formatted time, as "HH:MM".
     */
    @Nonnull
    public static String formatTimeAsHHMM(@Nullable final Date date) {
        final Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }

        // Use a local decimal formatter (DecimalFormat is not thread-safe).
        final DecimalFormat decimalFormat = new DecimalFormat("00");
        return decimalFormat.format(cal.get(Calendar.HOUR_OF_DAY)) + ':' +
                decimalFormat.format(cal.get(Calendar.MINUTE));
    }

    /**
     * Generate a random time (for example, an ETA) from now, or any date/time.
     *
     * @param date   Generate a time between 'date + offset' and 'date + offset + len'. If null, date is considered to
     *               be 'now'.
     * @param offset Minimum distance in minutes.
     * @param len    Length of interval.
     * @return Random time between [date + offset, date + offset + len].
     */
    @Nonnull
    public static Date randomTime(@Nullable final Date date, final int offset, final int len) {
        final Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        //noinspection NumericCastThatLosesPrecision
        cal.add(Calendar.MINUTE, offset + (int) (Math.round(Math.random() * len)));
        return cal.getTime();
    }

    @Nonnull
    public static Date randomTime(final int len) {
        return randomTime(null, 0, len);
    }

    @Nonnull
    public static Date randomTime(final int offset, final int len) {
        return randomTime(null, offset, len);
    }

    /**
     * Generate random text of a certain maximum length.
     *
     * @param maxChars Maximum length of text. Must be &gt;= 0.
     * @return Random text of 0 &lt;= length &lt;= maxChars.
     */
    @Nonnull
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static String randomText(final int maxChars) {
        assert maxChars >= 0;
        StringBuilder curr = new StringBuilder();
        final StringBuilder next = new StringBuilder();
        int loremIndex = (int) (Math.random() * 49.0);
        do {
            next.append(LOREM_IPSUM.getWords(1, loremIndex));
            loremIndex = (loremIndex + 1) % 50;
            if (next.length() < maxChars) {
                curr = next;
            }
            next.append(' ');
        }
        while (next.length() < maxChars);
        return curr.append('!').toString();
    }
}
