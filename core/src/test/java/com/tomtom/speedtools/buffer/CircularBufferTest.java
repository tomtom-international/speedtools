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

package com.tomtom.speedtools.buffer;

import com.tomtom.speedtools.buffer.CircularBuffer.OverflowException;
import com.tomtom.speedtools.buffer.CircularBuffer.UnderflowException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularBufferTest {
    private static final Logger LOG = LoggerFactory.getLogger(CircularBufferTest.class);

    @Test
    public void testBuffer() {
        LOG.info("testBuffer");

        final CircularBuffer<Integer> buffer = new CircularBuffer<>(3);
        LOG.info("Empty buffer = {}", buffer.toString());

        try {
            buffer.get();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

        try {
            buffer.newest();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

        try {
            buffer.oldest();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

        buffer.add(1);
        LOG.info("Buffer of length 1 = {}", buffer.toString());

        Assert.assertEquals(Integer.valueOf(1), buffer.newest());
        Assert.assertEquals(Integer.valueOf(1), buffer.oldest());
        Assert.assertEquals(Integer.valueOf(1), buffer.get());

        buffer.add(2);
        buffer.add(3);
        buffer.add(4);

        buffer.add(5);

        LOG.info("Buffer of length 3 = {}", buffer.toString());

        Assert.assertEquals(Integer.valueOf(5), buffer.newest());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(3, buffer.size());
        Assert.assertEquals(Integer.valueOf(3), buffer.oldest());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(3, buffer.size());
        Assert.assertEquals(Integer.valueOf(3), buffer.get());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals(Integer.valueOf(4), buffer.get());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(1, buffer.size());
        Assert.assertEquals(Integer.valueOf(5), buffer.get());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(0, buffer.size());

        try {
            buffer.get();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }
    }

    @Test
    public void testBufferStress() {
        LOG.info("testBufferStress");

        final CircularBuffer<Integer> buffer = new CircularBuffer<>(5, true);

        for (int i = 0; i < 100000; ++i) {
            if (buffer.size() < 5) {
                if (Math.random() < 0.2) {
                    buffer.add(1);
                }
            }

            if (buffer.size() > 0) {
                if (Math.random() < 0.2) {
                    buffer.get();
                }
            }
        }

        // This is actually a smoke test. It would try to fire asserts in the called code.
        Assert.assertTrue(true);
    }

    @Test
    public void testBufferString() {
        LOG.info("testBufferString");

        final CircularBuffer<Integer> buffer = new CircularBuffer<>(3, true);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);

        Assert.assertEquals("{\"size\":3,\"buffer\":[\"1\",\"2\",\"3\"]}", buffer.toString());
    }

    @Test
    public void testBufferOverflow() {
        LOG.info("testBufferOverflow");

        final CircularBuffer<Integer> buffer = new CircularBuffer<>(3, true);
        LOG.info("Empty buffer = {}", buffer.toString());

        try {
            buffer.get();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

        try {
            buffer.newest();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

        try {
            buffer.oldest();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

        buffer.add(1);
        LOG.info("Buffer of length 1 = {}", buffer.toString());

        Assert.assertEquals(Integer.valueOf(1), buffer.newest());
        Assert.assertEquals(Integer.valueOf(1), buffer.oldest());
        Assert.assertEquals(Integer.valueOf(1), buffer.get());

        buffer.add(2);
        buffer.add(3);
        buffer.add(4);

        try {
            buffer.add(5);
            Assert.fail();
        } catch (final OverflowException ignored) {
            // Ok.
        }

        LOG.info("Buffer of length 3 = {}", buffer.toString());

        Assert.assertEquals(Integer.valueOf(4), buffer.newest());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(3, buffer.size());
        Assert.assertEquals(Integer.valueOf(2), buffer.oldest());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(3, buffer.size());
        Assert.assertEquals(Integer.valueOf(2), buffer.get());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(2, buffer.size());
        Assert.assertEquals(Integer.valueOf(3), buffer.get());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(1, buffer.size());
        Assert.assertEquals(Integer.valueOf(4), buffer.get());
        Assert.assertEquals(3, buffer.maxSize());
        Assert.assertEquals(0, buffer.size());

        try {
            buffer.get();
            Assert.fail();
        } catch (final UnderflowException ignored) {
            // Ok.
        }

    }
}
