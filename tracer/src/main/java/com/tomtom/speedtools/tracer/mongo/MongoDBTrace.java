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

package com.tomtom.speedtools.tracer.mongo;

import com.tomtom.speedtools.tracer.Trace;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;

/**
 * POJO to store events in MongoDB.
 */
public class MongoDBTrace extends Trace {
    private final long serial;  // Serial number to make traces (somewhat) unique in the database.

    /**
     * An event is specific for an 'owning class'. The event is type-safe and its type is defined by the
     * 'eventInterface'.
     *
     * @param time   Time of event.
     * @param clazz  Class throwing the event.
     * @param tracer Type-safe event interface.
     * @param method Method throwing the event.
     * @param args   Additional arguments passed with the method.
     * @param serial Serial number of event, unique within time stamp.
     */
    protected MongoDBTrace(
            @Nonnull final DateTime time,
            @Nonnull final String clazz,
            @Nonnull final String tracer,
            @Nonnull final String method,
            @Nonnull final Object[] args,
            final long serial) {
        super(time, clazz, tracer, method, args);
        assert time != null;
        this.serial = serial;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    protected MongoDBTrace() {
        super();
        serial = 0;
    }

    public long getSerial() {
        return serial;
    }
}
