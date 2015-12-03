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

import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.tracer.TraceHandlerCollection;
import com.tomtom.speedtools.tracer.TraceStream;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Class that reads trace events from a MongoDB database.
 */
public class MongoDBTraceStream extends TraceHandlerCollection implements TraceStream {
    @Nonnull
    private final MongoDBTraceFetcher fetcher;

    @Inject
    public MongoDBTraceStream(@Nonnull final MongoDBTraceProperties properties) throws UnknownHostException {
        super();
        assert properties != null;

        // If the trace database is disabled, the fetcher will take care of that.
        fetcher = new MongoDBTraceFetcher(properties);
    }

    @Override
    @Nullable
    public DateTime moveTo(@Nonnull final DateTime time) {
        assert time != null;
        return fetcher.moveTo(time);
    }

    @Override
    @Nullable
    public DateTime moveToStart() {
        return fetcher.moveTo(UTCTime.from(new DateTime(0)));
    }

    @Override
    @Nullable
    public DateTime moveToEnd() {
        return fetcher.moveTo(null);
    }

    @Override
    public void playbackTo(@Nonnull final DateTime until) {
        assert until != null;
        final List<MongoDBTrace> traces = fetcher.getTraces(until);
        handleTraces(traces);
    }

    @Override
    public void playbackToEnd() {
        final List<MongoDBTrace> traces = fetcher.getTraces(null);
        handleTraces(traces);
    }

    /**
     * Return the trace fetcher, in case you wish to walk through events in your own code.
     *
     * @return The trace fetcher.
     */
    @Nonnull
    public MongoDBTraceFetcher getFetcher() {
        return fetcher;
    }
}
