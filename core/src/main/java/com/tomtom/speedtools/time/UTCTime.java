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

package com.tomtom.speedtools.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Utility class for UTC time usage. Date/time value created with this class are always time zoned UTC.
 */
public final class UTCTime {

    public static final DateTime MIN_TIMESTAMP_DATE = parse("1901-12-13T20:45:54Z");
    public static final DateTime MAX_TIMESTAMP_DATE = parse("2038-01-19T03:14:07Z");

    private UTCTime() {
        super();
        assert false;
    }

    /**
     * Return 'now' as UTC {@link DateTime}. Using this 'now' versus {@link DateTime#now()} has the advantage of making
     * sure you always read/write UTC time-zoned times.
     *
     * @return Now, with time zone '+000'.
     */
    @SuppressWarnings("SSBasedInspection")
    @Nonnull
    public static DateTime now() {
        return DateTime.now().withZone(DateTimeZone.UTC);
    }

    /**
     * Return time in time zone UTC.
     *
     * @param time Source time, Joda time.
     * @return Same time in UTC time.
     */
    @Nonnull
    public static DateTime from(@Nonnull final DateTime time) {
        assert time != null;
        return time.withZone(DateTimeZone.UTC);
    }

    /**
     * Return time in time zone UTC.
     *
     * @param time Source time, Java time.
     * @return Same time in UTC time.
     */
    @Nonnull
    public static DateTime from(@Nonnull final Date time) {
        assert time != null;
        return new DateTime(time).withZone(DateTimeZone.UTC);
    }

    /**
     * Parses a {@code DateTime} from the specified string.
     *
     * This uses {@link org.joda.time.format.ISODateTimeFormat#dateTimeParser()}.
     *
     * @param str the string to parse, not null.
     * @return UTC time.
     */
    @Nonnull
    public static DateTime parse(@Nonnull final String str) {
        assert str != null;
        return DateTime.parse(str).withZone(DateTimeZone.UTC);
    }

    /**
     * Returns a timestamp that corresponds to the given date. The minimum representable time is 1901-12-13T20:45:54Z,
     * and the maximum representable time is 2038-01-19T03:14:07Z.
     *
     * @param dateTime The date to convert.
     * @return The timestamp that corresponds to the given date.
     * @throws IllegalArgumentException In case the given date is not within the minimum {@link #MIN_TIMESTAMP_DATE} and
     *                                  maximum {@link #MAX_TIMESTAMP_DATE}.
     */
    public static int dateTimeToUTCTimestamp(@Nonnull final DateTime dateTime) {
        assert dateTime != null;

        if (dateTime.isBefore(MIN_TIMESTAMP_DATE) || dateTime.isAfter(MAX_TIMESTAMP_DATE)) {
            throw new IllegalArgumentException(
                    "Given date " + dateTime + "not within valid tiemstamp dates " + MIN_TIMESTAMP_DATE + ", " +
                            MAX_TIMESTAMP_DATE);
        }

        //noinspection NumericCastThatLosesPrecision
        return (int) (from(dateTime).getMillis() / 1000); // Can never exceed an int.
    }
}
