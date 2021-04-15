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

import akka.actor.ActorSystem;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ReactorImpl implements Reactor {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorImpl.class);

    @Nonnull
    private final ActorSystem system;

    @Inject
    public ReactorImpl(
            @Nonnull final Injector injector,
            @Nonnull final ActorSystem system) {
        assert injector != null;
        assert system != null;

        LOG.debug("ReactorImpl: injector={}", System.identityHashCode(injector));
        this.system = system;

        LOG.info("ReactorImpl: created (reactor sucessfully started)");
    }

    @Override
    @Nonnull
    public DateTime getSystemStartupTime() {
        return new DateTime(system.startTime());
    }

    @Override
    @Nonnull
    public ExecutionContext getExecutionContext() {
        return system.dispatcher();
    }
}
