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

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.TypedActor;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import com.tomtom.speedtools.akka.TypedActorFactory;

/**
 * Actor that serves as the root of all singleton actors in the system. All actors created by {@link #create(Class,
 * Class, Object...)} are created with a one-for-one restart supervisor strategy.
 * <p/>
 * This actor has a dispatcher with 1 thread allocated to it (see @link{ReactorImpl}). This guarantees that calls to
 * create() will never cause a deadlock. To ensure that no deadlocks occurs, methods of this actor must never block on
 * calls to other actors in the system.
 */
public class RootActorImpl implements RootActor, TypedActor.Supervisor {
    private static final Logger LOG = LoggerFactory.getLogger(RootActorImpl.class);

    @Nonnull
    private final OneForOneStrategy strategy;

    @Inject
    public RootActorImpl(
            @Nonnull final SupervisorStrategies strategies) {
        assert strategies != null;
        this.strategy = strategies.getRestartChildStrategy();
        LOG.debug("RootActorImpl: created");
    }

    @Nonnull
    @Override
    public <T> T create(
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<? extends T> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        LOG.debug("create: create new actor, interface={}, implementation={}",
                interfaceClass.getSimpleName(), implementationClass.getSimpleName());
        final T actor = TypedActorFactory.actorOf(interfaceClass.getSimpleName(), interfaceClass, implementationClass,
                explicitParameters);

        LOG.debug("create: actor created, interface={}, implementation={}",
                interfaceClass.getSimpleName(), implementationClass.getSimpleName());
        return actor;
    }

    @Nonnull
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
