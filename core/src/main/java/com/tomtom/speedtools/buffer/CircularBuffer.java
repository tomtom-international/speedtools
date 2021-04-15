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

package com.tomtom.speedtools.buffer;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Simple circular buffer class. This class is thread safe if every thread creates its own buffer though.
 * It is not thread-safe if the buffer object is shared between threads.
 *
 * @param <T> Type of buffer elements.
 */
public class CircularBuffer<T> {
    private final T[] buffer;
    private final boolean checkBufferOverflow;
    private int tail;
    private int head;
    private int count;

    /**
     * Create a new circular buffer.
     *
     * @param size                Size of buffer, must be &gt;= 1.
     * @param checkBufferOverflow True if buffer overflow needs to be checked. If false, a buffer overflow will simply
     *                            overwrite older elements. Note that buffer underflows can still occur and are thrown
     *                            as RuntimeExceptions.
     */
    public CircularBuffer(final int size, final boolean checkBufferOverflow) {
        assert size >= 1;
        //noinspection unchecked
        buffer = (T[]) new Object[size];
        Arrays.fill(buffer, null);
        this.checkBufferOverflow = checkBufferOverflow;
        tail = 0;
        head = 0;
        count = 0;
    }

    /**
     * Create a new circular buffer. Convenience constructor, which does not check buffer overflows.
     *
     * @param size Size of buffer, must be &gt;= 1.
     */
    public CircularBuffer(final int size) {
        this(size, false);
        assert size >= 1;
    }

    /**
     * Add a new element.
     *
     * @param newElement The element to be added to the buffer.
     * @throws OverflowException Thrown when a checked instance does not have space left.
     */
    public void add(@Nonnull final T newElement) {
        assert newElement != null;

        if (count == buffer.length) {
            // Buffer is full.
            if (checkBufferOverflow) {
                throw new OverflowException();
            }
            // No overflow checked, overwrite the oldest value.
            // Fake a get() to make room.
            tail = (tail + 1) % buffer.length;
        } else {
            // Buffer is not full. Add 1 element.
            count = count + 1;
        }
        buffer[head] = newElement;

        // Move head, potentially past end of array.
        head = (head + 1) % buffer.length;

        // We always have an element after adding.
        assert count > 0;

        // We have head == tail only if the buffer is full or empty.
        assert (head != tail) || (count == buffer.length);
    }

    /**
     * Return oldest element and move cursor.
     *
     * @return Oldest element.
     * @throws UnderflowException Thrown if buffer is empty.
     */
    @Nonnull
    public T get() {
        final T tailElement = oldest();
        tail = (tail + 1) % buffer.length;
        count = count - 1;

        assert ((count == 0) && (head == tail)) ||                // Buffer is empty.
                ((count == buffer.length) && (head == tail)) ||   // Buffer is full.
                (head != tail);                                   // Buffer is not empty and not full.

        return tailElement;
    }

    /**
     * Return newest element, but do not move markers.
     *
     * @return Newest element.
     * @throws UnderflowException Thrown if buffer does not contain a newest element.
     */
    @Nonnull
    public T newest() {
        if (count == 0) {
            assert tail == head;
            throw new UnderflowException();
        }
        return buffer[((head - 1) + buffer.length) % buffer.length];
    }

    /**
     * Return oldest element, but do not move markers.
     *
     * @return Oldest element.
     * @throws UnderflowException Thrown if buffer does not contain an oldest element.
     */
    @Nonnull
    public T oldest() {
        if (count == 0) {
            assert tail == head;
            throw new UnderflowException();
        }
        return buffer[tail];
    }

    /**
     * Return size of buffer.
     *
     * @return Size of buffer.
     */
    public int maxSize() {
        return buffer.length;
    }

    /**
     * Return number of element in buffer.
     *
     * @return Number of elements used in buffer.
     */
    public int size() {
        return count;
    }

    /**
     * Creates JSON string containing the buffer with its elements.
     *
     * @return the String containing the JSON. Numeric values will be double-quoted (not usual in JSON).
     */
    @Override
    @Nonnull
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\"size\":");
        sb.append(buffer.length);
        sb.append(",\"buffer\":[");

        int index = tail;

        // Iterate all entries.
        for (int i = 0; i < count; ++i) {
            if (i != 0) {
                sb.append(',');
            }

            sb.append('"');
            sb.append(buffer[index]);
            sb.append('"');

            // Next entry, clip if needed.
            index++;
            index = index % buffer.length;
        }
        sb.append("]}");
        return sb.toString();
    }

    public static class OverflowException extends RuntimeException {
        // Empty.
    }

    public static class UnderflowException extends RuntimeException {
        // Empty.
    }
}
