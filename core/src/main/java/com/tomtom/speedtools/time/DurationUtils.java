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

package com.tomtom.speedtools.time;

import org.joda.time.DateTime;
import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for Akka related functions.
 */
public final class DurationUtils {

    public final static int ACTOR_NAME_PREFIX_MAX_LEN = 32;

    // Private ctor for utility class.
    private DurationUtils() {
        super();
        assert false;
    }

    /**
     * Returns the time that is left between "now" and given time instance as an Akka duration instance.
     *
     * @param endTime Target time. Normally after "now" (or result will be 0). Null means "now".
     * @return Duration indication of time left until "now". Always &gt;= 0.
     */
    @Nonnull
    public static FiniteDuration durationFromNowUntil(@Nullable final DateTime endTime) {
        return duration(null, endTime);
    }

    /**
     * Converts a joda duration to an Akka duration.
     *
     * @param duration Joda duration.
     * @return Akka duration.
     */
    @Nonnull
    public static FiniteDuration duration(@Nonnull final org.joda.time.Duration duration) {
        assert duration != null;
        return Duration.create(duration.getMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Converts the difference between two joda times to an Akka duration.
     *
     * @param startTime Start time. Null means "now".
     * @param endTime   End time. Null means "now".
     * @return Akka duration. Always &gt;= 0.
     */
    @Nonnull
    public static FiniteDuration duration(@Nullable final DateTime startTime, @Nullable final DateTime endTime) {
        final FiniteDuration result;
        final org.joda.time.Duration posOrNegDuration = new org.joda.time.Duration(startTime, endTime);
        final long millis = posOrNegDuration.getMillis();
        if (millis >= 0) {
            result = Duration.create(millis, TimeUnit.MILLISECONDS);
        } else {
            result = Duration.create(0, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    /**
     * Returns the deadline to expire last.
     *
     * @param d1 first deadline
     * @param d2 second deadline
     * @return d1 if d1.timeLeft() &gt;= d2.timeLeft() else d2.  If either is null, the other is returned.
     */
    @Nullable
    public static Deadline max(@Nullable final Deadline d1, @Nullable final Deadline d2) {
        if (d1 == null) {
            return d2;
        }
        if (d2 == null) {
            return d1;
        }

        if (d1.timeLeft().gteq(d2.timeLeft())) {
            return d1;
        } else {
            return d2;
        }
    }
}
