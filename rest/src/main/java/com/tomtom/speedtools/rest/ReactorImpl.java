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

import akka.actor.ActorSystem;
import com.google.inject.Injector;
import com.tomtom.speedtools.akka.ActorFactory;
import com.tomtom.speedtools.akka.TypedActorFactory;
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
    @Nonnull
    private final RootActor rootActor;

    @Inject
    public ReactorImpl(
            @Nonnull final Injector injector,
            @Nonnull final ActorSystem system) {
        assert injector != null;
        assert system != null;

        LOG.debug("ReactorImpl: injector={}", System.identityHashCode(injector));

        this.system = system;

        // Couple injector to actor system.
        ActorFactory.setInjector(system, injector);
        LOG.debug("ReactorImpl: actorSystem.hashcode={}", System.identityHashCode(system));

        // Create root actor. The dispatcher name refers to a dispatcher configuration in application.conf.
        rootActor = TypedActorFactory.actorOf(system, "root", "akka.actor.rootactor-dispatcher", RootActor.class,
                RootActorImpl.class);

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

    @Nonnull
    @Override
    public <T> T createTopLevelActor(
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<? extends T> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;

        LOG.debug("createTopLevelActor: created new top-level actor, interface={}, implementation={}",
                interfaceClass.getSimpleName(), implementationClass.getSimpleName());
        final T topLevelActor = rootActor.create(interfaceClass, implementationClass, explicitParameters);

        LOG.debug("createTopLevelActor: top-level actor created, interface={}, implementation={}",
                interfaceClass.getSimpleName(), implementationClass.getSimpleName());
        return topLevelActor;
    }
}
