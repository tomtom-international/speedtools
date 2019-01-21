/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.tracer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Event trace handler that writes events to standard logger.
 */
@SuppressWarnings("ThisEscapedInObjectConstruction")
public class LoggingTraceHandler implements GenericTraceHandler {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(LoggingTraceHandler.class);

    // A DateTimeFormatter is thread-safe, so it can safely be declared static here.
    @Nonnull
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public LoggingTraceHandler() {
        TracerFactory.addTraceHandler(this);
    }

    @Override
    public void handle(
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

        // If logging is globally enabled and this particular SLF4J log handler is enabled, create readable message.
        if (LOG.isTraceEnabled()) {
            final StringBuilder message = new StringBuilder();
            message.append(time.toString(FORMATTER)).append(": ").
                    append(clazz).append('.').append(method).append('(');
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    message.append(", ");
                }

                /**
                 * Convert the argument to a string. Theoretically, this can throw an exception. It shouldn't,
                 * but if it does, we don't want this thread to die as a result of it. Log it as an error
                 * and continue.
                 */
                message.append((args[i] == null) ? "null" : Trace.toStringJsonSafe(args[i]));
            }
            message.append(')');
            LOG.trace("{}", message);
        }
    }
}
