/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

/**
 * Please refer to {@link RateLimiter} for documentation.
 *
 * Fairly heavy-handed thread safe version of the above. There's not a lot of point to trying to avoid locking here,
 * since the whole point of this thing is to block writes, so we might as well keep it simple.
 */
public class ThreadSafeRateLimiter extends RateLimiter {

    public ThreadSafeRateLimiter(final int maxFreqHz) {
        super(maxFreqHz);
    }

    @SuppressWarnings("SynchronizedMethod")
    @Override
    public synchronized void limit(final WorkFunction workFunction) {
        super.limit(workFunction);
    }
}
