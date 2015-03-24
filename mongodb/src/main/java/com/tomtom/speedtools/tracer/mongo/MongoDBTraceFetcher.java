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

package com.tomtom.speedtools.tracer.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.tomtom.speedtools.mongodb.SimpleMongoDBSerializer;
import com.tomtom.speedtools.time.UTCTime;

/**
 * This class implement a fetcher for events from a MongoDB database.
 *
 * This class is package private.
 */
@SuppressWarnings("ThisEscapedInObjectConstruction")
class MongoDBTraceFetcher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBTraceFetcher.class);

    private static final String TAILABLE_QUERY_DUMMY_EVENT = "@skip";
    private static final int THREAD_SLEEP_MSECS = 250;
    private static final int FETCH_QUEUE_MAX_SIZE = 500;

    @Nonnull
    private final Thread thread = new Thread(this);
    @Nullable
    private final DBCollection collection;
    @Nonnull
    private final AtomicReference<CurrentFetch> currentFetch = new AtomicReference<CurrentFetch>(null);

    @Nonnull
    private DateTime lastEventTime = UTCTime.now();

    MongoDBTraceFetcher(@Nonnull final MongoDBTraceProperties properties) throws UnknownHostException {
        assert properties != null;

        if (properties.getReadEnabled()) {
            LOG.debug("MongoDBTraceFetcher: reading traces enabled, getting traces collection and starting fetcher");
            collection = MongoDBTraceHandler.getDBCollection(
                    properties.getServers(), properties.getDatabase(),
                    properties.getUserName(), properties.getPassword(),
                    properties.getMaxDatabaseSizeMB(), properties.getConnectionTimeoutMsecs());

            //noinspection CallToThreadStartDuringObjectConstruction
            thread.start();
        } else {
            LOG.info("MongoDBTraceFetcher: reading traces disabled, fetcher not started");
            collection = null;
        }
    }

    /**
     * Get events from the stream until (not including) this time.
     *
     * @param until Until time, exclusive.
     * @return List of events.
     */
    @Nonnull
    public List<MongoDBTrace> getTraces(@Nullable final DateTime until) {
        final List<MongoDBTrace> traces = new ArrayList<MongoDBTrace>();

        // Bail-out if traces disabled.
        if (collection == null) {
            LOG.debug("getTraces: reading traces disabled, cannot get traces");
            return traces;
        }

        final CurrentFetch fetch = currentFetch.get();
        if (fetch != null) {
            while (true) {
                final MongoDBTrace trace = fetch.getQueue().peek();

                // No more events? We're done. The queue is filled in the run() method.
                if (trace == null) {
                    break;
                }

                // Event does not match 'until' constraint? We're done.
                if ((until != null) && !trace.getTime().isBefore(until)) {
                    break;
                }

                final MongoDBTrace polledTrace = fetch.getQueue().poll();
                //noinspection ObjectEquality
                assert trace == polledTrace;
                traces.add(trace);
            }
        }
        return traces;
    }

    /**
     * Move to a specific time location, or to the end of the event stream.
     *
     * @param time Move to start of this time stamp, or to end of stream if null.
     * @return Time moved to.
     */
    @Nonnull
    public DateTime moveTo(@Nullable final DateTime time) {

        // Bail-out if traces disabled.
        if (collection == null) {
            LOG.debug("getTraces: reading traces disabled, cannot move to {}", time);
            return (time == null) ? UTCTime.now() : time;
        }

        final DateTime fromTime;
        if (time == null) {
            final DateTime now = UTCTime.now();
            while (now.equals(UTCTime.now())) {

                // Move past 'now'.
                try {
                    //noinspection BusyWait
                    Thread.sleep(1);
                } catch (final InterruptedException ignored) {
                    // Ignored.
                }
            }
            fromTime = UTCTime.now();
        } else {
            fromTime = time;
        }

        /**
         * Select all events after 'fromTime'.
         *
         * Important: The query will not be tailable if the first query does not return
         * a record, so we MUST add a dummy record. The fetcher must forget (i.e. not handle())
         * this one if it encounters it.
         */
        final DBObject query = new BasicDBObject("time", new BasicDBObject("$gte", fromTime.toDate()));
        if (collection.findOne(query) == null) {

            // No object was, we need to insert at least one.
            @Nonnull final MongoDBTrace trace =
                    new MongoDBTrace(
                            fromTime,                       // Insert time stamp.
                            TAILABLE_QUERY_DUMMY_EVENT,     // Owner class.
                            TAILABLE_QUERY_DUMMY_EVENT,     // Tracer interface.
                            TAILABLE_QUERY_DUMMY_EVENT,     // Method name.
                            new Object[]{}, 0);             // Dummy object and serial number.

            // Catch exceptions from MongoDB here.
            try {
                final Object dbTrace = SimpleMongoDBSerializer.getInstance().serialize(trace);
                if (dbTrace instanceof DBObject) {
                    collection.insert((DBObject) dbTrace);
                }
            } catch (final Exception e) {
                LOG.error("moveTo: unexpected exception=" + e.getMessage(), e);

                /**
                 *  Continue execution, because errors during tracing should NOT disturb execution.
                 *  Do log this as en error, because we're not expecting this to happen.
                 */
            }
            LOG.debug("moveTo: inserted dummy trace event, fromTime={}", fromTime);
        }

        // Execute actual tailing query.
        final DBCursor cursor = collection.find(query).
                addOption(Bytes.QUERYOPTION_TAILABLE).
                addOption(Bytes.QUERYOPTION_AWAITDATA);

        // Start a new fetch
        currentFetch.set(new CurrentFetch(cursor, new ConcurrentLinkedQueue<MongoDBTrace>()));
        return fromTime;
    }

    /**
     * The run() method fill the queue with elements from the database.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        assert collection != null;

        boolean handled = false;

        // Thread loop.
        while (!thread.isInterrupted()) {
            final CurrentFetch fetch = currentFetch.get();
            if (fetch != null) {

                // Add events to the queue if the queue is not filled up yet.
                try {
                    if ((fetch.queue.size() < FETCH_QUEUE_MAX_SIZE) && fetch.cursor.hasNext()) {

                        // Fetch next event.
                        final DBObject fetched = fetch.cursor.next();
                        final Object deserialized = SimpleMongoDBSerializer.getInstance().deserialize(fetched);
                        if (deserialized instanceof MongoDBTrace) {
                            final MongoDBTrace trace = (MongoDBTrace) deserialized;
                            lastEventTime = trace.getTime();

                            /**
                             * Skip any events that had to be queued to make sure the
                             * tailable query does not fail.
                             */
                            if (!trace.getClazz().equals(TAILABLE_QUERY_DUMMY_EVENT)) {

                                // Add it to the queue.
                                fetch.queue.add(trace);
                            }

                        }
                        handled = true;
                    }
                } catch (final Throwable ignored) {
                    final DateTime now = UTCTime.now();
                    LOG.error("run: MongoDB exception" +
                            ". Last event time: " + lastEventTime + '(' + lastEventTime.toDate().getTime() + ')' +
                            ". Moving head to: " + now + '(' + now.toDate().getTime() + ')');
                    moveTo(now);
                }
            }

            // Watch out for busy waiting.
            if (!handled) {

                // No current fetch, wait some time.
                try {
                    //noinspection BusyWait
                    Thread.sleep(THREAD_SLEEP_MSECS);
                } catch (final InterruptedException ignored) {
                    // Ignore.
                }
            }
        }
    }

    /**
     * Utility class to store pointer into event collection.
     */
    private static class CurrentFetch {
        @Nonnull
        private final DBCursor cursor;
        @Nonnull
        private final ConcurrentLinkedQueue<MongoDBTrace> queue;

        private CurrentFetch(
                @Nonnull final DBCursor cursor,
                @Nonnull final ConcurrentLinkedQueue<MongoDBTrace> queue) {
            assert cursor != null;
            assert queue != null;
            this.cursor = cursor;
            this.queue = queue;
        }

        @Nonnull
        public DBCursor getCursor() {
            return cursor;
        }

        @Nonnull
        public ConcurrentLinkedQueue<MongoDBTrace> getQueue() {
            return queue;
        }
    }
}
