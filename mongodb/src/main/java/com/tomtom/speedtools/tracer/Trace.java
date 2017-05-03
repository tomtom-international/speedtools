/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import com.tomtom.speedtools.json.Json;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Class to store a single logging event. A Trace can contain any (serializable) object, or a class which extends
 * SimpleTraceEvent.
 */
public class Trace {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(Trace.class);

    @Nonnull
    private final DateTime time;
    @Nonnull
    private final String clazz;
    @Nonnull
    private final String tracer;
    @Nonnull
    private final String method;
    @Nonnull
    private final Object[] args;

    /**
     * An event is specific for an 'owning class'. The event is type-safe and its type is defined by the
     * 'eventInterface'.
     *
     * @param clazz  Class throwing the event.
     * @param tracer Type-safe event interface.
     * @param method Method throwing the event.
     * @param time   Event time.
     * @param args   Additional arguments passed with the method.
     */
    public Trace(
            @Nonnull final DateTime time,
            @Nonnull final String clazz,
            @Nonnull final String tracer,
            @Nonnull final String method,
            @Nonnull final Object[] args) {
        assert time != null;
        assert clazz != null;
        assert tracer != null;
        assert method != null;
        assert args != null;
        this.time = time;
        this.clazz = clazz;
        this.tracer = tracer;
        this.method = method;
        this.args = args;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    protected Trace() {
        super();
        time = null;
        clazz = null;
        tracer = null;
        method = null;
        args = null;
    }

    @Nonnull
    public String getClazz() {
        return clazz;
    }

    @Nonnull
    public String getTracer() {
        return tracer;
    }

    @Nonnull
    public String getMethod() {
        return method;
    }

    @Nonnull
    public DateTime getTime() {
        return time;
    }

    @Nonnull
    public Object[] getArgs() {
        return args;
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }

    /**
     * Package private method to convert an object to a string catching runtime and assertion exceptions.
     */
    static String toStringJsonSafe(final Object o) {
        String result;
        //noinspection ErrorNotRethrown
        try {
            result = Json.toStringJson(o);
        } catch (final AssertionError e) {
            LOG.error("handle: Assertion error converting trace argument to string. Exception:", e);
            result = "(assertion error)";
        } catch (final RuntimeException e) {
            LOG.error("handle: Runtime exception converting trace argument to string. Exception:", e);
            result = "(runtime exception)";
        }
        return result;
    }
}
