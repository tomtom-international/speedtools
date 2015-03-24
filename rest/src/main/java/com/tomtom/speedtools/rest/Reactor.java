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

package com.tomtom.speedtools.rest;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import scala.concurrent.ExecutionContext;

/**
 * The reactor defines the Akka system in which the 'engine' of the system is running. The reactor provides utilities to
 * access actors through actor factories, as well as to the Akka {@link ExecutionContext}, which can be used, for
 * example, to schedule Futures in.
 *
 * The reactor interface is not normally used directly. Rather, use injection to
 * access the various actors / factories / execution context and system. Especially inside actors, injection should be
 * used, specifically, the TypedActorContext can be injected.
 */
public interface Reactor {

    /**
     * Get the Akka execution context. This context can be used to schedule Futures in. Example:
     * <pre>
     * Futures.future(
     *    new Callable... {
     *    ... Void call() ...
     *    }, reactor.getExecutionContext())
     * </pre>
     *
     * @return Akka execution context.
     */
    @Nonnull
    ExecutionContext getExecutionContext();

    /**
     * Get the system startup time.
     *
     * @return the system startup time.
     */
    @Nonnull
    DateTime getSystemStartupTime();

    /**
     * Create a top-level actor.
     *
     * @param interfaceClass      Actor interface.
     * @param implementationClass Actor implementation class.
     * @param explicitParameters  Non-injected parameters for the  actor constructor.
     * @return New top-level actor.
     */
    @Nonnull
    <T> T createTopLevelActor(
            @Nonnull Class<T> interfaceClass,
            @Nonnull Class<? extends T> implementationClass,
            @Nonnull Object... explicitParameters);
}
