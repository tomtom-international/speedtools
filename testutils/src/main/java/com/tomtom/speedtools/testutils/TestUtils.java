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

package com.tomtom.speedtools.testutils;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is a simple utility class for unit tests.  <strong>Important note on the format of the unit tests for
 * the domain model:</strong>
 *
 * The code for the unit tests may look a bit odd at first glance, but a clear structure
 * has been used to produce them:
 *
 * For every class, an 'empty as possible' object 'x' is made (as private final).
 * Object 'x' is constructed from a number of parameters (say N), which are always called x1..xN. The objects x1..xN are
 * constructed before 'x'. This object contains null values and empty collections wherever possible.
 *
 * Next to 'x', a non-empty object 'y' is created in the @Before clause of the JUnit test case. The object 'y' is
 * constructed from parameters y1..yN, which are constructed before 'y'.
 *
 * For every getXXX and withXXX function, the following pattern is used (for example, for getId, which would be the 1st
 * parameter of a constructor of the object under test):
 * <pre>
 *      Assert.assertTrue(y1.equals(x.withId(y1).getId()));
 *      Assert.assertTrue(x1.equals(y.withId(x1).getId()));
 * </pre>
 * This will test getId and withId by taking the non-empty object, requesting an empty variant of a field and checking
 * it against the original empty variant. The same goes for the other way around: take the non-empty object, request an
 * empty variant of the field and checking it against the original empty one.
 */
public final class TestUtils {

    /**
     * Keep last issues UTCTime.now().
     */
    private static final DateTime FIRST_DATETIME = new DateTime(2012, 3, 7, 2, 30, 0, 1).withZone(DateTimeZone.UTC);
    private static final String REGEX_DATETIME1 = "\\b1234567890123\\b".replaceAll("\\d", "\\\\d");
    private static final String REGEX_DATETIME2 =
            "[\"]*9999[-]99[-]99T99[:]99[:]99[.]9*.?9*[\"]*".replaceAll("\\d", "\\\\d");
    private static final String REGEX_DATETIME3 =
            "[\"]*9999[-]99[-]99T99[:]99[:]99[.]?9*[Z][\"]*".replaceAll("\\d", "\\\\d");
    private static final String REGEX_UID = "00000000-0000-0000-0000-000000000000".replaceAll("0", "\\\\w");

    @SuppressWarnings("StaticNonFinalField")
    private static volatile DateTime lastDateTime = FIRST_DATETIME;

    private static final AtomicInteger lastUid = new AtomicInteger(1);

    private TestUtils() {
        // Prevent instantiation.
    }

    /**
     * Set seed for unique time.
     *
     * @param time Initial time, e.g. UTCTime.now().
     */
    public static void setUniqueDateTime(@Nonnull final DateTime time) {
        assert time != null;
        lastDateTime = time;
    }

    /**
     * Return now() and wait a short time (if needed) so next now is unique. This function should only be used in test
     * code. Thread-safe implementation.
     *
     * @return Now.
     */
    @Nonnull
    public static DateTime getUniqueDateTime() {
        final DateTime next = lastDateTime;
        //noinspection NonAtomicOperationOnVolatileField
        lastDateTime = lastDateTime.plusMillis(350);
        return next;
    }

    /**
     * Return a valid Uid string.
     *
     * @return Uid string.
     */
    @Nonnull
    public static String getUniqueUid() {

        // NumberFormat is NOT thread-safe, so it must be declared locally here (not static).
        final NumberFormat numberFormat = new DecimalFormat("000000000000");
        final String uid = "12345678-9012-3456-7890-" + numberFormat.format(lastUid.getAndIncrement());
        return uid;
    }

    @Nonnull
    public static String replaceUniques(@Nonnull final String str) {
        assert str != null;
        String result = str.replaceAll(REGEX_UID, "@UID");
        result = result.replaceAll(REGEX_DATETIME1, "@DATETIME");
        result = result.replaceAll(REGEX_DATETIME2, "@DATETIME");
        result = result.replaceAll(REGEX_DATETIME3, "@DATETIME");
        return result;
    }

    @Nonnull
    public static String createBasicAuthenticationCredentials(@Nonnull final String deviceId) {
        final String value = deviceId + ":none";
        final byte[] expected = Base64.encodeBase64(value.getBytes());
        return "Basic " + new String(expected);
    }
}
