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

package com.tomtom.speedtools.ratelimiter;

import com.tomtom.speedtools.buffer.CircularBuffer;

/**
 * This class is used to rate limit writes to, say, MongoDB reads/writes. It's handy if the calling code can do bulk
 * write operations through the limiter, which rate-limits how fast they happen. Note you can disable the rate limiter
 * by setting the frequency to 0.
 *
 * The math is as follows (mod some conversions between seconds and nano-seconds):
 * <pre>
 *  After n samples, we can computer Ri as the "immediate rate" like:
 *      Ri = (n-1) / (timestamp - timestamp(0))
 *  which is essentially the 1/average interval.
 *
 *  Given a target rate, Rt ("maxFreqHz"), we can compute it like:
 *      Rt = n / (S + (timestamp - timestamp(0))
 *
 *  S here is how long to sleep before we invoke the next operation.  Solving for S:
 *      S = n / Rt - (timestamp - timestamp(0))
 * </pre>
 * Original author of Scala version: Eric Bowman (2012-05-16) Rewritten to Java by: Rijn Buve (2012-07-24).
 */
public class RateLimiter {

    private final CircularBuffer<Long> buffer;            // Buffer to calculate average speed.
    private final double maxFreqGigaHz;     // Max. allowed frequency.

    /**
     * Create a rate limiter that will limit calls to 'maxFreqHz' in bursts which occur approximately every
     * 'burstFreqHz'. For example, if maxFreqHz = 100 and burstFreqHz = 2, the limiter will allow no more than 100 calls
     * per second, but will burst them into 2 batches of 50 calls each, per second.
     *
     * For a more evenly distributed burst pattern, increase burstFreqHz. It's best to keep burstFreqHz reasonably low,
     * say below 100.
     *
     * Note that the implementation of the limiter does not guarantee that the frequency is reached (or that calls may
     * have been scheduled slightly faster than the frequency, even). On average, it does a pretty good job though.
     *
     * @param maxFreqHz   Max. frequency.
     * @param burstFreqHz Burts frequency.
     */
    public RateLimiter(final int maxFreqHz, final int burstFreqHz) {
        assert maxFreqHz >= 0 : "maxFreqHz must be >= 0";
        assert burstFreqHz > 0 : "burstFreqHz must be > 0";

        // Small frequencies needs some buffer.
        //noinspection NumericCastThatLosesPrecision
        this.buffer = new CircularBuffer<>(Math.max(2, (int) Math.round((double) maxFreqHz / burstFreqHz)));

        // Convert 1/s to 1/nanoseconds.
        this.maxFreqGigaHz = maxFreqHz / 1.0e9d;
    }

    public RateLimiter(final int maxFreqHz) {
        this(maxFreqHz, 10);
    }

    /**
     * Limit the execution of a work function to the frequency set in this limiter.
     *
     * @param workFunction Work function (interface, see below).
     */
    public void limit(final WorkFunction workFunction) {

        // If the frequency is specified as 0, do work immediately.
        if (maxFreqGigaHz > 0) {

            // Record the timestamp first! Otherwise delays between invocations don't count.
            buffer.add(System.nanoTime());
            if (buffer.size() > 1) {

                // At least 2 samples required to be able to define this.
                // Sum of all intervals.
                final long sum = buffer.newest() - buffer.oldest();

                // How long sleep to achieve maxFreqHz.
                final long sleepMs = Math.round((((double) buffer.maxSize() / maxFreqGigaHz) - sum) / 1.0e6d);
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (final InterruptedException ignored) {
                        // Ignore interruption here.
                    }
                }
            }
        }

        // Do what we are meant to do and return result.
        workFunction.work();
    }

    /**
     * Interface for worker function, passed as an argument to limit() function below.
     */
    public interface WorkFunction {

        @SuppressWarnings("EmptyMethod")
        public void work();
    }
}
