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

import com.mongodb.DBCollection;
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.tracer.Traceable;
import com.tomtom.speedtools.tracer.TracerFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

public class MongoDBTraceTest {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBTraceTest.class);
    private static final Tracer TRACER = TracerFactory.getTracer(MongoDBTraceTest.class, Tracer.class);

    private static final String server = "127.0.0.1:27017";
    private static final String database = "trace";
    private static final String userName = "";
    private static final String password = "";
    private static final int maxDatabaseSizeMB = 10;
    private static final int connectionTimeoutMsecs = 30000;
    private static final MongoDBTraceProperties mongoDBTraceProperties = new MongoDBTraceProperties(
            server, database, userName, password, maxDatabaseSizeMB, connectionTimeoutMsecs, true, true);

    private static DateTime startOfTest;

    private static int someInt;
    private static String someString;
    private static DateTime someTime;

    private static boolean foundInt = false;
    private static boolean foundString = false;
    private static boolean foundDateTime = false;

    @BeforeClass
    static public void before() throws Exception {
        LOG.info("before");
        startOfTest = UTCTime.now();

        /**
         * The first part of the test case creates an empty traces
         * data base collection and enables tracing.
         */
        LOG.info("before: dropping database '{}' on {}", database, server);
        final DBCollection collection = MongoDBTraceHandler.getDBCollection(
                server, database, userName, password, maxDatabaseSizeMB, connectionTimeoutMsecs);
        collection.drop();

        LOG.info("before: start tracer");
        final MongoDBTraceProperties mongoDBTraceProperties = new MongoDBTraceProperties(
                server, database, userName, password, maxDatabaseSizeMB, connectionTimeoutMsecs, true, true);

        final MongoDBTraceHandler handler = new MongoDBTraceHandler(mongoDBTraceProperties);
        TracerFactory.setEnabled(true);

        // Create random values for events.
        someInt = (int) (Math.random() * 1000.0) + 1000;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; ++i) {
            sb.append((char) ((int) (Math.random() * 25.999) + 65));
        }
        someString = sb.toString();
        someTime = UTCTime.now();
    }

    @Test
    public void testTrace() throws Exception {
        LOG.info("testTrace");

        /**
         * The second part of the test case writes a number of test traces
         * to the database, asynchronously (hence the sleeps).
         */

        LOG.info("testTrace: trace an int");
        TRACER.traceInteger(someInt);

        LOG.info("testTrace: trace an string");
        TRACER.traceString(someString);

        LOG.info("testTrace: trace an date/time");
        TRACER.traceDateTime(someTime);

        LOG.info("testTrace: sleep... flushing traces to database");
        sleep(2000);
        TracerFactory.setEnabled(false);
        LOG.info("testTrace: done");

        /**
         * The third part of the test case replays the traces using the
         * streamer and checks if written events were encountered.
         */
        LOG.info("testTrace: fetch traces");
        final MongoDBTraceStream stream = new MongoDBTraceStream(mongoDBTraceProperties);
        stream.addTraceHandler(new Streamer());
        stream.moveTo(startOfTest);

        // Playback events.
        final int testDuration = 2;
        LOG.info("Start playback of events during {}s", testDuration);
        final DateTime until = UTCTime.now().plus(Duration.standardSeconds(testDuration));
        while (UTCTime.now().isBefore(until)) {
            stream.playbackToEnd();
            try {
                //noinspection BusyWait
                Thread.sleep(100);
            }
            catch (InterruptedException ignored) {
                // Ignore.
            }
        }

        LOG.info("testTrace: checking if traces were found in database");
        assertTrue(foundInt);
        assertTrue(foundString);
        assertTrue(foundDateTime);
    }

    // This interface is used when tracing events.
    public static interface Tracer extends Traceable {
        public void traceInteger(int i);

        public void traceString(@Nonnull String string);

        public void traceDateTime(@Nonnull DateTime dateTime);
    }

    // This class is used when streaming/playing back traced events back.
    public static class Streamer implements Tracer {

        @Override
        public void traceInteger(final int i) {
            LOG.info("traceInteger: {}", i);
            foundInt = (i == someInt);
        }

        @Override
        public void traceString(@Nonnull final String string) {
            LOG.info("traceString: {}", string);
            foundString = string.equals(someString);
        }

        @Override
        public void traceDateTime(@Nonnull final DateTime dateTime) {
            LOG.info("traceDateTime: {}", dateTime);
            foundDateTime = dateTime.equals(someTime);
        }
    }
}
