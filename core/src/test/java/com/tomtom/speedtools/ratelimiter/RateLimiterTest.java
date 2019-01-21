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

import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.utils.MathUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("SSBasedInspection")
public class RateLimiterTest {
    private static final Logger LOG = LoggerFactory.getLogger(RateLimiterTest.class);

    @Test
    public void testBufferSlow() {
        LOG.info("testBufferSlow");

        final RateLimiter limiter = new RateLimiter(5);

        final DateTime start = DateTime.now();
        for (int i = 0; i < 15; ++i) {
            limiter.limit(() -> {
                // Empty.
            });
        }
        final DateTime end = DateTime.now();

        final long delay = end.toDate().getTime() - start.toDate().getTime();
        LOG.info("Delay is {} msecs", delay);
        Assert.assertTrue(MathUtils.isBetween(delay, 2500, 3500));
    }

    @Test
    public void testBufferFast() {
        LOG.info("testBufferFast");

        final RateLimiter limiter = new RateLimiter(100);

        final DateTime start = UTCTime.now();
        for (int i = 0; i < 300; ++i) {
            limiter.limit(() -> {
                // Empty.
            });
        }
        final DateTime end = DateTime.now();

        final long delay = end.toDate().getTime() - start.toDate().getTime();
        LOG.info("Delay is {} msecs", delay);
        Assert.assertTrue(MathUtils.isBetween(delay, 2500, 4000));
    }
}
