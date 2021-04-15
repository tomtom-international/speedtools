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

package com.tomtom.speedtools.rest;

import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import com.tomtom.speedtools.apivalidation.exceptions.ApiException;
import org.jboss.resteasy.spi.AsynchronousResponse;
import org.slf4j.Logger;
import scala.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;


/**
 * Processes API resources. It uses the reactor to call resources inside the Akka system.
 */
public final class ResourceProcessor {

    /**
     * The {@link Reactor}, needed to schedule Futures in.
     */
    @Nonnull
    private final Reactor reactor;

    @Inject
    public ResourceProcessor(
            @Nonnull final Reactor reactor) {
        assert reactor != null;
        this.reactor = reactor;
    }

    /**
     * Process an API resource. The actual resource handling is delegated to an {@link ResourceHandler}, which is an
     * interface with an implementation for each individual resource.
     *
     * @param name     Name of processor; used for logging purposes.
     * @param log      Logger of caller, so log message appear to come from the caller, which makes more sense than from
     *                 this generic class.
     * @param response Asynchronous rest response object.
     * @param handler  Handler that actually calculates the REST response. If the handler returns a non-null value in
     *                 the future success, it will be wrapped in an "200 OK" response.
     */
    @SuppressWarnings("InstanceofCatchParameter")
    public void process(
            @Nonnull final String name,
            @Nonnull final Logger log,
            @Nonnull final AsyncResponse response,
            @Nonnull final ResourceHandler handler) {
        assert response != null;
        assert log != null;
        assert handler != null;

        /**
         * Send the handler to an actor for in-Akka execution. The result of the Future is omitted.
         * The purpose of the Future is to schedule the handler only.
         *
         * Note that the Future call() closure has access to the parameter values above.
         */
        log.debug("process (WEB): handler={}", name);
        Futures.future(() -> {

                    /**
                     * Catch exceptions to map them to proper HTTP status code.
                     */
                    try {

                        /**
                         * Call the actual resource handler. The call to "process()" returns a {@link Future},
                         * which is scheduled asynchronously for execution. The {@link Future} will hold a specific response.
                         * As this is a generic resource processor, the response is passed as an {@link Object} here.
                         */
                        @SuppressWarnings("unchecked") final Future<Object> future = (Future<Object>) handler.process();

                        /**
                         * If the {@link Future} completes, the {@link OnComplete}.onComplete is called,
                         * which passes the result of the {@link Future} into the {@link AsyncResponse} object,
                         * which will be passed bac to the caller of the resource.
                         */
                        //noinspection unchecked
                        future.onComplete(new OnComplete<Object>() {

                            /**
                             * This function is called whenever the {@link Future} completes, dies or times out.
                             *
                             * @param failure Exception in case something went wrong, null if OK.
                             * @param success Result of {@link Future} computation. If the handler set the response itself,
                             *              it should pass null as a value. This can be used, for example,
                             *              for a handler to set a specific HTTP status code,
                             *              other than this processor would.
                             */
                            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
                            @Override
                            public void onComplete(
                                    @Nullable final Throwable failure,
                                    @Nullable final Object success) {

                                if (failure != null) {
                                    log.info("process: resource exception, handler=" + name, failure);
                                    response.resume(GeneralExceptionMapper.toResponse(log, failure));
                                } else if (success != null) {
                                    response.resume(Response.ok(success).build());
                                } else {

                                    /**
                                     * Response was already set by resource handler.
                                     * Do nothing.
                                     */
                                    assert true;
                                }
                            }
                        }, reactor.getExecutionContext());
                    } catch (final Throwable e) {
                        if (((e instanceof RuntimeException) || (e instanceof Error)) &&
                                !(e instanceof ApiException)) {
                            // Something went wrong, probably a bug in the code.
                            log.error("process: exception encountered, handler={}", name, e);
                        } else {
                            // Some-one is firing requests that cause errors.
                            log.info("process: exception encountered, handler={}, exception={}",
                                    name, e.getMessage());
                        }
                        response.resume(GeneralExceptionMapper.toResponse(log, e));
                    }

                    //noinspection ConstantConditions
                    return null;
                },

                /**
                 * Execute Future in reactor context.
                 */
                reactor.getExecutionContext());
    }

    /**
     * Deprecated resource processor. Supported for legacy clients only.
     *
     * @param name     Name of processor; used for logging purposes.
     * @param log      Logger of caller, so log message appear to come from the caller, which makes more sense than from
     *                 this generic class.
     * @param response Asynchronous rest response object.
     * @param handler  Handler that actually calculates the REST response. If the handler returns a non-null value in
     *                 the future success, it will be wrapped in an "200 OK" response.
     * @deprecated Replaced by {@link #process(String, Logger, AsyncResponse, ResourceHandler)}.
     */
    @Deprecated
    @SuppressWarnings("InstanceofCatchParameter")
    public void process(
            @Nonnull final String name,
            @Nonnull final Logger log,
            @Nonnull final AsynchronousResponse response,
            @Nonnull final ResourceHandler handler) {
        assert response != null;
        assert log != null;
        assert handler != null;

        log.debug("[DEPRECATED] process (WEB): handler={}", name);
        Futures.future(() -> {
                    try {
                        @SuppressWarnings("unchecked") final Future<Object> future = (Future<Object>) handler.process();
                        //noinspection unchecked
                        future.onComplete(new OnComplete<Object>() {
                            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
                            @Override
                            public void onComplete(
                                    @Nullable final Throwable failure,
                                    @Nullable final Object success) {

                                if (failure != null) {
                                    log.info("[DEPRECATED] process: resource exception, handler=" + name, failure);
                                    response.setResponse(GeneralExceptionMapper.toResponse(log, failure));
                                } else if (success != null) {
                                    response.setResponse(Response.ok(success).build());
                                } else {
                                    assert true;
                                }
                            }
                        }, reactor.getExecutionContext());
                    } catch (final Throwable e) {
                        if (((e instanceof RuntimeException) || (e instanceof Error)) &&
                                !(e instanceof ApiException)) {
                            // Something went wrong, probably a bug in the code.
                            log.error("[DEPRECATED] process: exception encountered, handler={}", name, e);
                        } else {
                            // Some-one is firing requests that cause errors.
                            log.info("[DEPRECATED] process: exception encountered, handler={}, exception={}",
                                    name, e.getMessage());
                        }
                        response.setResponse(GeneralExceptionMapper.toResponse(log, e));
                    }
                    //noinspection ConstantConditions
                    return null;
                },
                reactor.getExecutionContext());
    }
}
