/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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
import akka.actor.Scheduler;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.tomtom.speedtools.maven.MavenProperties;
import com.tomtom.speedtools.rest.security.SecurityInterceptor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.ExecutionContext;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

/**
 * Guice module for services.
 */
public class ServicesModule implements Module {

    @Override
    public void configure(@Nonnull final Binder binder) {
        assert binder != null;

        // Web services exception mapper and JSON context resolver.
        binder.bind(GeneralExceptionMapper.class).in(Singleton.class);

        // Reactor.
        binder.bind(Reactor.class).to(ReactorImpl.class).in(Singleton.class);
        binder.bind(SupervisorStrategies.class);

        // Web services security interceptor.
        binder.bind(SecurityInterceptor.class).in(Singleton.class);

        // Web services authentication service; needs to be provided by application:
        // binder.bind(AuthenticationService.class).to(AuthenticationServiceImpl.class).in(Singleton.class);

        // API related bindings.
        binder.bind(MavenProperties.class).in(Singleton.class);
        binder.bind(ResourceProcessor.class).in(Singleton.class);

        // Register the injector. An eager singleton is used because no-one actually retrieves the registry
        // through Guice. Thus it must be eager to be started.
        binder.bind(InjectorRegistry.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    @Nonnull
    public ActorSystem provideActorSystem() {
        // Initialize actor system.
        final Config akkaConfig = ConfigFactory.load();
        return ActorSystem.create("akka", akkaConfig);
    }

    @Provides
    @Singleton
    @Nonnull
    public Scheduler provideScheduler(
            @Nonnull final ActorSystem actorSystem) {
        assert actorSystem != null;
        return actorSystem.scheduler();
    }

    @Provides
    @Singleton
    @Nonnull
    public ExecutionContext provideExecutionContext(
            @Nonnull final Reactor reactor) {
        assert reactor != null;
        return reactor.getExecutionContext();
    }
}
