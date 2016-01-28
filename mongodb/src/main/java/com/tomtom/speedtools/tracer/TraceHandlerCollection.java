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

import com.tomtom.speedtools.objects.Triple;
import com.tomtom.speedtools.objects.Tuple;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements adding and calling of event trace handlers.
 */
public class TraceHandlerCollection {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(TraceHandlerCollection.class);

    @Nonnull
    private final List<Traceable> handlers = new CopyOnWriteArrayList<>();

    @Nonnull
    private final Map<Triple<Class<? extends Traceable>, String, Tuple<String, Integer>>, List<Method>> methods =
            new ConcurrentHashMap<>();  // Handler class, tracer and (method, number of arguments).

    // Public constructor.
    public TraceHandlerCollection() {
        LOG.debug("TraceHandlerCollection: created");
    }

    /**
     * Invoked when a trace handler needs to handle a trace event.
     *
     * @param trace Trace event.
     */
    public void handleTrace(@Nonnull final Trace trace) {
        assert trace != null;
        for (final Traceable handler : handlers) {

            /**
             * Check what type of trace handler we're dealing with. There are two types of handlers:
             * Generic handlers (heavy- and lightweight) and {@link Tracer} implementations.
             */
            if (handler instanceof GenericTraceHandler) {

                // This is a generic heavy-weight trace handler.
                final GenericTraceHandler genericTraceHandler = ((GenericTraceHandler) handler);
                genericTraceHandler.handle(
                        trace.getTime(), trace.getClazz(), trace.getTracer(), trace.getMethod(), trace.getArgs());
            } else {

                /**
                 *  This is an implemented version of the {@link Tracer} interface. Find the right
                 *  method and invoke.
                 */
                final Method method =
                        findMethod(handler.getClass(), trace.getTracer(), trace.getMethod(), trace.getArgs().length);
                if (method != null) {
                    try {
                        method.invoke(handler, trace.getArgs());
                    } catch (final Exception e) {

                        // List all arguments as well (for debugging purposes of the serializer/deserializer).
                        final StringBuilder sb = new StringBuilder();
                        int i = 1;
                        for (final Object arg : trace.getArgs()) {
                            sb.append("\n   * arg(");
                            sb.append(i);
                            sb.append(") [");
                            if (arg != null) {
                                sb.append(arg.getClass().getSimpleName());
                            }
                            sb.append("]: ");

                            /**
                             * Convert the argument to a string. Theoretically, this can throw an exception. It shouldn't,
                             * but if it does, we don't want this thread to die as a result of it. Log it as an error
                             * and continue.
                             */
                            sb.append((arg == null) ? "null" : Trace.toStringJsonSafe(arg));
                            ++i;
                        }
                        final String args = sb.toString();
                        LOG.error("handleTrace: Cannot invoke event trace handler:" +
                                "\n-- method: " + method +
                                "\n-- arguments:" + args +
                                "\n-- trace: " + Trace.toStringJsonSafe(trace) +
                                "\n-- exception: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Handles multiple traces at once. Errors in individual traces are logged and otherwise ignored.
     *
     * @param traces Ordered list of events.
     * @return Time of the last trace.
     */
    @Nullable
    public DateTime handleTraces(@Nonnull final List<? extends Trace> traces) {
        assert traces != null;
        DateTime last = null;
        for (final Trace trace : traces) {
            try {
                handleTrace(trace);
                last = trace.getTime();
            } catch (final Throwable t) {
                LOG.error("Unexpected exception in trace handler: " + t.getMessage(), t);
            }
        }
        return last;
    }

    public void addTraceHandler(@Nonnull final Traceable handler) {
        assert handler != null;
        handlers.add(handler);
    }

    public void removeTraceHandler(@Nonnull final Traceable handler) {
        assert handler != null;
        handlers.remove(handler);
    }

    public void removeAllTraceHandlers() {
        handlers.clear();
    }

    @Nullable
    private Method findMethod(@Nonnull final Class<? extends Traceable> handlerClass,
                              @Nonnull final String tracer, @Nonnull final String method, final int nrArgs) {

        assert handlerClass != null;
        assert tracer != null;
        assert method != null;

        final Triple<Class<? extends Traceable>, String, Tuple<String, Integer>> key =
                new Triple<>(handlerClass, tracer,
                        new Tuple<>(method, nrArgs));
        List<Method> result = methods.get(key);

        // This might happen multiple times concurrently, but this is fine since the result will always be the same.
        if (result == null) {
            final Method theMethod = findMethodInClass(handlerClass, tracer, method, nrArgs);
            if (theMethod != null) {
                result = Collections.singletonList(theMethod);
                methods.put(key, result);
            } else {
                result = Collections.emptyList();
            }
        }
        return result.isEmpty() ? null : result.get(0);
    }

    @Nullable
    private static Method findMethodInClass(@Nonnull final Class<?> c, @Nonnull final String tracer,
                                            @Nonnull final String method, final int nrArgs) {
        assert c != null;
        assert tracer != null;
        assert method != null;

        for (final Method m : c.getDeclaredMethods()) {
            if (m.getDeclaringClass().getName().equals(tracer) &&
                    m.getName().equals(method) &&
                    (m.getParameterCount() == nrArgs)) {
                return m;
            }
        }
        for (final Class<?> superClass : c.getInterfaces()) {
            final Method m = findMethodInClass(superClass, tracer, method, nrArgs);
            if (m != null) {
                return m;
            }
        }
        return null;
    }
}
