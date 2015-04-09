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

package com.tomtom.speedtools.thread;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class WorkQueueTest {
    private static final Logger LOG = LoggerFactory.getLogger(WorkQueueTest.class);

    private final AtomicInteger total = new AtomicInteger(0);

    @Test
    public void testWorkQueue() throws InterruptedException {
        LOG.info("testWorkQueue");

        total.set(0);
        final int maxQueueSize = 10000;
        final WorkQueue queue = new WorkQueue(maxQueueSize);

        final int totalTasks = maxQueueSize * 10;
        for (int i = 0; i < totalTasks; ++i) {
            queue.startOrWait(() -> {
                total.incrementAndGet();
                for (int x = 0; x < 1000; ++x) {
                    assert Math.sqrt((double) x) >= 0;
                }
            });
            if ((i % 100) == 0) {
                LOG.debug("testWorkQueue: i={}", i);
            }
        }
        Assert.assertTrue(queue.waitUntilFinished());
        Assert.assertEquals(totalTasks, total.get());
        queue.scheduleShutdown();
    }
}
