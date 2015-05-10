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

package com.tomtom.speedtools.testutils.resteasy;

import org.jboss.resteasy.spi.AsynchronousResponse;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Asynchronous response class to used for testing. Use an instance of this class as the last argument of an
 * asynchronous resource when testing the resource. After the resource has been 'invoked', the test can verify that the
 * application has responded with a correct response by using <pre>verifyEntity</pre> or <pre>verifyStatus</pre>.
 *
 * @param <T> Type of the entity within the response.
 */
public final class AsynchronousTestResponse<T> implements AsynchronousResponse {

    @Nonnull
    private final BlockingQueue<Response> queue = new LinkedBlockingQueue<>();
    @Nonnull
    private final AtomicReference<Response> response = new AtomicReference<>(null);

    /**
     * Creates a response instance.
     *
     * @param <T> Entity type within the response.
     * @return New Response instance.
     */
    @Nonnull
    public static <T> AsynchronousTestResponse<T> create() {
        return new AsynchronousTestResponse<>();
    }

    /**
     * Use create to create an instance. Create is less verbose because type parameter is induced.
     */
    private AsynchronousTestResponse() {
        // Ok.
    }

    /**
     * Verify that the resource invocation resulted in an entity-response within given time. The status code is verified
     * to be 200 (OK).
     *
     * @param timeout Time out value.
     * @return Entity of the response.
     * @throws UnexpectedResponseException If error.
     */
    @Nonnull
    public T verifyEntity(@Nonnull final Duration timeout) throws UnexpectedResponseException {
        return verifyEntity(timeout.getMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Verify that the resource invocation resulted in an entity-response within given time. Optionally the response
     * status code is checked simultaneously.
     *
     * @param expectedStatus When set, the expected status code of the response.
     * @param timeout        Time out value.
     * @return Entity of the response.
     * @throws UnexpectedResponseException If error.
     */
    @Nonnull
    public T verifyEntity(@Nullable final Integer expectedStatus, @Nonnull final Duration timeout) throws UnexpectedResponseException {
        return verifyEntity(expectedStatus, timeout.getMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Verify that the resource invocation resulted in an entity-response within given time. The status code is verified
     * to be 200 (OK).
     *
     * @param timeout Time out value.
     * @param unit    Unit of the time out value.
     * @return Entity of the response.
     * @throws UnexpectedResponseException If error.
     */
    @Nonnull
    public T verifyEntity(final long timeout, @Nonnull final TimeUnit unit) throws UnexpectedResponseException {
        return verifyEntity(Status.OK.getStatusCode(), timeout, unit);
    }

    /**
     * Verify that the resource invocation resulted in an entity-response within given time. Optionally the response
     * status code is checked simultaneously.
     *
     * @param expectedStatus When set, the expected status code of the response.
     * @param timeout        Time out value.
     * @param unit           Unit of the time out value.
     * @return Entity of the response.
     * @throws UnexpectedResponseException If error.
     */
    @Nonnull
    public T verifyEntity(@Nullable final Integer expectedStatus, final long timeout, @Nonnull final TimeUnit unit) throws UnexpectedResponseException {
        final Response response = waitResponse(timeout, unit);
        if ((expectedStatus != null) && (response.getStatus() != expectedStatus)) {
            throw new UnexpectedResponseException(response, "Unexpected status: expected " + expectedStatus +
                    " but got " + response.getStatus() + '.');
        }
        if (response.getEntity() == null) {
            throw new AssertionError("Response does not contain entity. Status: " + response.getStatus());
        }
        //noinspection unchecked
        return (T) response.getEntity();
    }

    /**
     * Verify that the resource invocation resulted in a response with given response code.
     *
     * @param expectedStatus When set, the expected status code of the response.
     * @param timeout        Time out value.
     */
    public void verifyStatus(final int expectedStatus, @Nonnull final Duration timeout) {
        verifyStatus(expectedStatus, timeout.getMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Verify that the resource invocation resulted in a response with given response code.
     *
     * @param expectedStatus When set, the expected status code of the response.
     * @param timeout        Time out value.
     * @param unit           Unit of the time out value.
     */
    public void verifyStatus(final int expectedStatus, final long timeout, @Nonnull final TimeUnit unit) {
        final Response response = waitResponse(timeout, unit);
        if (response.getStatus() != expectedStatus) {
            throw new AssertionError("Unexpected status: expected " + expectedStatus + " but got " + response
                    .getStatus() + '.');
        }
    }

    @Override
    public void setResponse(@Nonnull final Response response) {
        if (!this.response.compareAndSet(null, response)) {
            throw new AssertionError("More than one response given.");
        }
        this.queue.offer(response);
    }

    @Nonnull
    private Response waitResponse(final long timeout, @Nonnull final TimeUnit unit) {
        try {
            final Response currentResponse = this.response.get();
            if (currentResponse != null) {
                return currentResponse;
            } else {
                final Response response = queue.poll(timeout, unit);
                if (response == null) {
                    throw new AssertionError("Timeout while waiting for response entity.");
                }
                return response;
            }
        } catch (final InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
