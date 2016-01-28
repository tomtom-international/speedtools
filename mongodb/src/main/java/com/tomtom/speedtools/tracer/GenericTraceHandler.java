/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

/**
 * GenericTraceHandler is an interface to catch type-safe events from event loggers. See {@link TracerFactory} for an
 * explanation on how to use this class.
 */
public interface GenericTraceHandler extends Traceable {

    /**
     * The function 'log' logs an event as type-safe objects to an EventLogger listener interface.
     *
     * @param time   Event time.
     * @param clazz  Class throwing the event.
     * @param tracer Tracer interface name.
     * @param method Method throwing the event.
     * @param args   Actual event, as a series of 0..n objects.
     */
    void handle(
            @Nonnull DateTime time,
            @Nonnull String clazz,
            @Nonnull String tracer,
            @Nonnull String method,
            @Nonnull Object[] args);
}
