/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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


import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This factory creates Traceable tracers. Tracers are specific for a class/method and are used to throw events to
 * GenericTraceHandler listeners.
 *
 * Example of usage:
 *
 * <pre>
 *   class MyClass {
 *
 *      // Create an event logger for events of type "Events" (locally defined interface).
 *      private static final Tracer TRACER = TracerFactory.getTracer(MyClass.class, Tracer.class);
 *
 *      // Define a type-safe event interface (no implementation needed).
 *      public static interface Tracer {
 *          void foundMatch(Trip trip, Collection&gt;Party&lt; drivers);
 *      }
 *
 *      // Use the event tracer in a function:
 *      void someFunction() {
 *          ...
 *          EVENTS.foundMatch(theTrip, theDrivers);
 *          ...
 *       }
 *    }
 * </pre>
 */
public class TracerFactory implements InvocationHandler {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(TracerFactory.class);

    private static final int QUEUE_CAPACITY = 15000;
    @Nonnull
    private static final Object[] EMPTY_OBJECT_ARRAY = {};

    @Nonnull
    private static final BlockingQueue<Trace> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    @Nonnull
    private static final AtomicBoolean enabled = new AtomicBoolean(false);
    @Nonnull
    private static final TraceHandlerCollection handlers = new TraceHandlerCollection();

    @Nonnull
    private final Class<?> ownerClass;

    @SuppressWarnings("StaticNonFinalField")
    @Nullable
    private static Thread thread = null;

    private static final long LOST_EVENTS_THRESHOLD = 100000;
    @Nonnull
    private static final Duration LOST_EVENTS_LOG_INTERVAL = Duration.standardSeconds(1);
    @Nonnull
    private static final DateTime SERVER_START_TIME = UTCTime.now();

    @SuppressWarnings("StaticNonFinalField")
    @Nonnull
    private static DateTime lastEventLostTime = UTCTime.now().minus(LOST_EVENTS_LOG_INTERVAL);

    private static final AtomicLong eventsLostSinceLastLog = new AtomicLong(0);
    private static final AtomicLong eventsLostTotal = new AtomicLong(0);

    /**
     * Get an event logger for a specific class. The event logger is specified as a type-safe user-defined interface.
     * The interface methods are called to simulate events and their invocation leads to calls to a proxy, which calls
     * an actual event logger handler.
     *
     * @param clazz  Class to get an event logger for.
     * @param tracer Interface class containing type-safe events.
     * @param <T>    Interface type.
     * @return Event logger.
     */
    @Nonnull
    public static <T extends Traceable> T getTracer(
            @Nonnull final Class<?> clazz,
            @Nonnull final Class<T> tracer) {
        assert clazz != null;
        assert tracer != null;

        //noinspection unchecked
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{tracer},
                new TracerFactory(clazz));
    }

    /**
     * Add an event trace handler.
     *
     * @param handler Event trace handler.
     */
    public static void addTraceHandler(@Nonnull final Traceable handler) {
        assert handler != null;
        handlers.addTraceHandler(handler);
    }

    /**
     * Remove an event trace handler.
     *
     * @param handler Event trace handler.
     */
    public static void removeTraceHandler(@Nonnull final Traceable handler) {
        assert handler != null;
        handlers.removeTraceHandler(handler);
    }

    public static void removeAllTraceHandlers() {
        handlers.removeAllTraceHandlers();
    }

    public static void flushTraces() {
        queue.clear();
    }

    /**
     * Return whether event logging is enabled or not.
     *
     * @return True if enable, false if not.
     */
    public static boolean isEnabled() {
        return enabled.get();
    }

    /**
     * Enable or disable event logging. If enabled, the handlers are called for events, otherwise they are not. Enabling
     * event logging starts a thread to call the individual event loggers.
     *
     * @param enabled Set or unset logging.
     */
    public static void setEnabled(final boolean enabled) {
        final boolean wasEnabled = TracerFactory.enabled.getAndSet(enabled);
        if (!wasEnabled && enabled) {

            // Start a new thread got the event logger handling.
            assert thread == null;
            thread = new Thread(new QueueProcessor());      // Create a new thread.
            thread.setName("Tracer " + thread.getName());   // Use original name as well to keep it unique.
            thread.start();
        } else if (wasEnabled && !enabled) {

            // Stop the event logging thread.
            assert thread != null;
            thread.interrupt();
            thread = null;
        } else {
            assert (!wasEnabled && (thread == null)) || (wasEnabled && (thread != null));
        }
    }

    /**
     * Initialize the factory.
     *
     * @param ownerClass Class that wishes to create event loggers.
     */
    public TracerFactory(@Nonnull final Class<?> ownerClass) {
        this.ownerClass = ownerClass;
    }

    /**
     * This is the 'invoke' function that gets called whenever the interface of an event logger is called. Note that
     * this function sends a "log.trace()" message to SLF4J as well. This may cause quite a lot of trace logging in a
     * production system.
     *
     * @param proxy  Proxied object.
     * @param method Method being called.
     * @param args   Additional arguments to method.
     * @return Always null; the signature should be void for these calls.
     */
    @Nullable
    @Override
    public Object invoke(@Nonnull final Object proxy, @Nonnull final Method method, @Nullable final Object[] args) {

        // Skip event when tracing is disabled.
        if (!isEnabled()) {
            return null;
        }

        // Store the event in a queue and process it asynchronously in a separate thread. Never block!
        final Trace e = new Trace(UTCTime.now(), ownerClass.getName(), method.getDeclaringClass().getName(),
                method.getName(), (args == null) ? EMPTY_OBJECT_ARRAY : args);

        if (!queue.offer(e)) {
            eventsLostSinceLastLog.incrementAndGet();
            eventsLostTotal.incrementAndGet();
        }

        if (eventsLostSinceLastLog.longValue() > 0) {
            final DateTime now = UTCTime.now();
            if ((lastEventLostTime.plus(LOST_EVENTS_LOG_INTERVAL).isBefore(now) ||
                    (eventsLostSinceLastLog.longValue() >= LOST_EVENTS_THRESHOLD))) {
                LOG.warn("invoke: Events queue is full! Lost " + eventsLostSinceLastLog +
                        " events in last " + ((now.getMillis() - lastEventLostTime.getMillis()) / 1000) +
                        " secs (for a total of " + eventsLostTotal + " events since " + SERVER_START_TIME + ')');
                eventsLostSinceLastLog.set(0);
                lastEventLostTime = now;
            }
        }
        return null;
    }

    /**
     * Queue processor thread to handle incoming events. This thread simply takes elements from the queue and processes
     * them one by one.
     */
    static class QueueProcessor implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    handlers.handleTrace(queue.take());
                } catch (final InterruptedException ignored) {
                    break;
                }
            }
        }
    }
}
