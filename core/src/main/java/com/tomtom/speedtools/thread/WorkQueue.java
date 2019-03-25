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

package com.tomtom.speedtools.thread;

import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class creates a pool of worker threads that will execute workload tasks. The amount of actual threads is
 * limited, as well as the workload queue.
 *
 * To use this class, you should create a WorkQueue with a maximum number of work packages for the work queue. Some of
 * the worker threads are started (and stand-by) immediately after creating the WorkQueue.
 *
 * After that, you can add workload (which is really an instance of a Runnable) to the work queue by using
 * startOrWait(workLoad). This call will add the workload if the queue is not filled up yet, or wait until there is room
 * to add the workload.
 *
 * You can use waitUntilFinished() to wait until the entire workload queue is processed (and all worker threads are done
 * processing). Or you can check whether processing is done with isEmptyAndFinished().
 *
 * If worker threads throw exceptions, these are caught and stored in a list which can be retrieved by
 * getRuntimeExceptions(). The worker thread that processed workload throwing such an exception is simply returned to
 * the thread pool ready to process the next piece of workload.
 *
 * To shutdown all the worker threads, use scheduleShutdown(). This will schedule a shutdown of all worker threads after
 * they have finished processing their workload. After this call, the only valid calls left is getRuntimeExceptions().
 *
 * Important: This class itself is NOT thread safe: only 1 thread should feed one particular instance of WorkQueue at a
 * time.
 */
public class WorkQueue {
    private static final Logger LOG = LoggerFactory.getLogger(WorkQueue.class);

    private static final int ISSUE_WAITING_LOG_LINE_AFTER_SECS = 10;   // Issue 'debug' log every now and then.
    private static final int MAX_THREADS_FOR_FULL_QUEUE = 32;
    private static final int BUSY_WAIT_MSECS_MIN = 5;
    private static final int BUSY_WAIT_MSECS_MAX = 250;

    @Nonnull
    private ThreadPoolExecutor executor;
    @Nonnull
    private final List<Exception> exceptions;
    private final int maxQueueSize;
    private final long feederThread;

    /**
     * Create a work queue with a maximum number of worker threads and a maximum workload queue size. Adding workload
     * past the workload queue size will block until the queue is small enough to add more workload.
     *
     * The caller should call shutdown() to shut down the threads after they have carried out their workloads.
     *
     * @param maxQueueSize Maximum work load queue size.
     */
    public WorkQueue(
            final int maxQueueSize) {

        this.exceptions = Collections.synchronizedList(new ArrayList<>());
        this.feederThread = Thread.currentThread().getId();
        this.maxQueueSize = maxQueueSize;
        this.executor = createNewExecutor();
    }

    /**
     * Schedule shutdown for all threads after they finished their work. After this call, no other calls to this class
     * should be made!
     */
    public void scheduleShutdown() {
        assert Thread.currentThread().getId() == feederThread;

        executor.shutdown();
    }

    /**
     * Start workload, or wait if there is too much workload in the queue.
     *
     * @param workLoad Workload to be started.
     * @param timeout  Timeout in millis. If there is no room left in the queue before this timeout expires, the
     *                 workload is discarded and not scheduled. Use 0 for wait 'forever'.
     */
    @SuppressWarnings("CallToNotifyInsteadOfNotifyAll")
    public void startOrWait(@Nonnull final Runnable workLoad, final long timeout) {
        assert timeout >= 0;
        assert !executor.isShutdown();
        assert Thread.currentThread().getId() == feederThread;

        final DateTime startTime = UTCTime.now();
        final long start = startTime.getMillis();
        DateTime nextDebugTime = startTime.plusSeconds(ISSUE_WAITING_LOG_LINE_AFTER_SECS);
        boolean scheduled = false;
        boolean again = false;
        int busyWait = BUSY_WAIT_MSECS_MIN;
        do {
            try {
                executor.execute(new RuntimeExceptionCatcher(workLoad));
                scheduled = true;
            } catch (final RejectedExecutionException ignored1) {
                assert !scheduled;
                try {
                    //noinspection BusyWait
                    Thread.sleep(busyWait);
                    if (busyWait < BUSY_WAIT_MSECS_MAX) {
                        ++busyWait;
                    }
                    final DateTime now = UTCTime.now();
                    final long timeWaiting = now.getMillis() - start;
                    again = ((timeout == 0) || (timeWaiting < timeout));

                    // Issue a log message only if timeout == 0 and task is rescheduled.
                    if (again && (timeout == 0) && now.isAfter(nextDebugTime)) {
                        LOG.debug("startOrWait: workLoad not executed yet, already waiting {} secs...",
                                timeWaiting / 1000);
                        nextDebugTime = now.plusSeconds(ISSUE_WAITING_LOG_LINE_AFTER_SECS);
                    }
                } catch (final InterruptedException ignored2) {
                    assert !again;
                }
            }
        }
        while (!scheduled && again);
        if (!scheduled) {
            LOG.debug("startOrWait: workLoad was not scheduled, aborted after timeout={} msecs", timeout);
        }
    }

    /**
     * Start workload, or wait if there is too much workload in the queue.
     *
     * @param workLoad Workload to be started.
     */
    public void startOrWait(@Nonnull final Runnable workLoad) {
        startOrWait(workLoad, 0);
    }

    /**
     * Wait until the work pool finished executing all work load.
     *
     * @param timeout Max. wait time in msecs. Use 0 for 'forever'.
     * @return False if exceptions were caught during executing workload packages.
     */
    public boolean waitUntilFinished(final long timeout) {
        assert timeout >= 0;
        assert !executor.isShutdown();
        assert Thread.currentThread().getId() == feederThread;

        LOG.debug("waitUntilFinished: shut down executor");
        executor.shutdown();

        final DateTime startTime = UTCTime.now();
        DateTime nextDebugTime = startTime.plusSeconds(ISSUE_WAITING_LOG_LINE_AFTER_SECS);
        boolean again = false;
        do {
            try {
                final DateTime now = UTCTime.now();
                if (now.isAfter(nextDebugTime)) {
                    LOG.debug("waitUntilFinished: awaiting termination of executor for {} secs...",
                            (now.getMillis() - startTime.getMillis()) / 1000);
                    nextDebugTime = now.plusSeconds(ISSUE_WAITING_LOG_LINE_AFTER_SECS);
                }
                again = !executor.awaitTermination(
                        (timeout == 0) ? ISSUE_WAITING_LOG_LINE_AFTER_SECS : timeout,
                        (timeout == 0) ? TimeUnit.SECONDS : TimeUnit.MILLISECONDS);
            } catch (final InterruptedException ignored) {
                // Ignored.
            }
        }
        while (again && (timeout == 0));
        LOG.debug("waitUntilFinished: executor terminated (creating new one)");

        /**
         *  Current pool is empty and done, get a new executor pool.
         *  Don't clear the exceptions list, as it is supposed to be the overall list for this
         *  WorkQueue instance (not fot the ThreadPoolExecutor instance).
         */
        executor = createNewExecutor();
        return exceptions.isEmpty();
    }

    /**
     * Wait until the work pool finished executing all work load.
     *
     * @return False if exceptions were caught during executing workload packages.
     */
    public boolean waitUntilFinished() {
        return waitUntilFinished(0);
    }

    /**
     * Check if there is workload available, or a thread is processing workload still.
     *
     * @return Workload is available, or a thread is busy processing last workload.
     */
    public boolean isEmptyAndFinished() {
        assert !executor.isShutdown();
        assert Thread.currentThread().getId() == feederThread;

        return executor.getQueue().isEmpty();
    }

    /**
     * Add a specific exception to the work queue. This method may come in handy in the run() method of workload, to
     * communicate specific exceptions to the WorkQueue during execution.
     *
     * @param exception Exception to be added.
     */
    public void addException(@Nonnull final Exception exception) {
        exceptions.add(exception);
    }

    /**
     * Return any runtime exception that occurred in the threads. This method should only be called from the feeder
     * thread.
     *
     * @return List of exceptions.
     */
    @Nonnull
    public List<Exception> getExceptions() {
        assert Thread.currentThread().getId() == feederThread;

        return exceptions;
    }

    /**
     * Create a new executor.
     *
     * @return Max work queue size.
     */
    @Nonnull
    private ThreadPoolExecutor createNewExecutor() {
        final int nrCores = Runtime.getRuntime().availableProcessors();
        final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(this.maxQueueSize);
        return new ThreadPoolExecutor(
                Math.min(nrCores, MAX_THREADS_FOR_FULL_QUEUE),  // Core pool.
                MAX_THREADS_FOR_FULL_QUEUE,                     // Max. pool.
                10, TimeUnit.SECONDS,                           // Keep-alive time.
                queue);                                         // Work queue.
    }

    private class RuntimeExceptionCatcher implements Runnable {
        @Nonnull
        private final Runnable runnable;

        RuntimeExceptionCatcher(@Nonnull final Runnable runnable) {
            assert runnable != null;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (final RuntimeException e) {
                LOG.error("Runtime exception encoutered", e);
                exceptions.add(e);
            }
        }
    }
}
