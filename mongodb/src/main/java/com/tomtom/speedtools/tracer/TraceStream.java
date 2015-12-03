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

package com.tomtom.speedtools.tracer;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Source for event traces. A processor represents a stream of events. It has a current position, and it can be asked to
 * process subsequent events up until a point in time using processUntil, or until all events currently in the stream
 * are processed: {@link #playbackToEnd}.
 *
 * The current position can be set to a specific moment in time (setPosition), or to the end of the stream
 * (setPositionAtEnd). No events will be processed when changing the current position.
 *
 * Events are processed by calling the appropriate handlers. These handlers are to be added using addHandler(Object).
 *
 * Note: A processor is NOT thread-safe.
 */
public interface TraceStream {


    /**
     * Add handler for processing trace events. The type of the handler can be a trace class, or {@link
     * GenericTraceHandler}.
     *
     * @param handler Event trace handler.
     */
    public void addTraceHandler(@Nonnull final Traceable handler);

    /**
     * Remove handler for processing trace events. The type of the handler can be a trace class, or {@link
     * GenericTraceHandler}.
     *
     * @param handler Event trace handler.
     */
    public void removeTraceHandler(@Nonnull final Traceable handler);

    /**
     * Process all remaining events. Any previously processed events will not be processed again, except after a call to
     * setPosition.
     */
    public void playbackToEnd();

    /**
     * Process remaining events up until given time. Any previously processed events will not be processed again, except
     * after a call to setPosition.
     *
     * @param until Time until when to process events, exclusive.
     */
    public void playbackTo(@Nonnull final DateTime until);

    /**
     * Rewinds the stream to given moment in time. From now on, processing occurs for events from this time. Given time
     * is inclusive.
     *
     * @param time Time to move to.
     * @return Actual time moved to.
     */
    @Nullable
    public DateTime moveTo(@Nonnull final DateTime time);

    /**
     * Fast-rewinds to the start of the stream. All events currently in the stream will be skipped when any of the
     * process methods is called.
     *
     * @return Actual time moved to.
     */
    @Nullable
    public DateTime moveToStart();

    /**
     * Fast-forwards to the end of the stream. All events currently in the stream will be skipped when any of the
     * process methods is called.
     *
     * @return Actual time moved to.
     */
    @Nullable
    public DateTime moveToEnd();
}
