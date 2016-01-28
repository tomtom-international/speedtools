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

package com.tomtom.speedtools.akka;

import akka.actor.*;
import akka.routing.RouterConfig;
import org.joda.time.DateTime;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.Nonnull;

/**
 * Facade of the actor system to be used by typed actors. All Akka functionality to be used from an actor is exposed
 * through this class. It also facilitates unit testing, since no static methods no longer have (and should) be called.
 * A typed actor facade can be injected automatically by {@link TypedActorFactory}.
 */
public class TypedActorContext<T> {

    @Nonnull
    private final ActorSystem system;
    @Nonnull
    private final ActorContext context;
    @Nonnull
    private final T self;

    public TypedActorContext(@Nonnull final ActorSystem system, @Nonnull final ActorContext context,
                             final Class<T> interfaceClass) {
        assert system != null;
        assert context != null;
        this.system = system;
        this.context = context;
        //noinspection unchecked
        this.self = (T) TypedActor.get(context).typedActorOf(new TypedProps(interfaceClass), context.self());
    }

    @Nonnull
    public ActorSystem getActorSystem() {
        return system;
    }

    /**
     * Returns the self reference, that is, the typed reference around the actor reference.
     *
     * @return Self reference.
     */
    @Nonnull
    public T self() {
        return self;
    }

    /**
     * Returns the execution context associated with this actor. To be used by futures. In unit tests, should be mocked
     * by a SimpleExecutionContext.
     *
     * @return Execution context.
     */
    @Nonnull
    public ExecutionContext executionContext() {
        return context.dispatcher();
    }

    @Nonnull
    public DateTime getSystemStartupTime() {
        return new DateTime(system.startTime());
    }

    /**
     * Stops given typed actor.
     *
     * @param typedActor actor to stop.
     * @return True when the actor could be found.
     */
    public boolean stop(@Nonnull final Object typedActor) {
        return TypedActor.get(context).stop(typedActor);
    }

    /**
     * Returns a reference for the given actor.
     *
     * @param typedActor The actor to get the reference for.
     * @return The ActorRef for actor.
     */
    @Nonnull
    public ActorRef actorRefFor(@Nonnull final Object typedActor) {
        return TypedActor.get(context).getActorRefFor(typedActor);
    }

    /**
     * Creates an untyped actor of given class. The class should have a constructor with parameters that match given
     * explicitParameters. If the constructor has additional parameters and it has the Inject annotation, these
     * parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <Impl>              Implementation.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public <T, Impl extends T> T actorOf(
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<Impl> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return TypedActorFactory.actorOf(context, interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <Impl>              Implementation.
     * @param name                Name of the created actor.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public <T, Impl extends T> T actorOf(
            @Nonnull final String name,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<Impl> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert name != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return TypedActorFactory.actorOf(context, name, interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Returns the typed actor with given path, proxied with given interface.
     *
     * @param <T>            Type of the proxy interface.
     * @param name           Relative path to actor.
     * @param interfaceClass Type of the proxy interface.
     * @return Typed actor.
     */
    @Nonnull
    public <T> T actorFor(
            @Nonnull final String name,
            @Nonnull final Class<T> interfaceClass) {
        assert name != null;
        assert interfaceClass != null;

        final ActorRef actorRef = context.actorFor(context.self().path().child(name));
        //noinspection unchecked
        return (T) TypedActor.get(context).typedActorOf(new TypedProps(interfaceClass), actorRef);
    }

    /**
     * Returns a typed router actor that will forward all messages using the routerConfig.
     *
     * @param <T>            The type of the proxy interface.
     * @param interfaceClass The type of the proxy interface.
     * @param routerConfig   The router configuration to use.
     * @return Typed router actor.
     */
    @Nonnull
    public <T> T routerFor(@Nonnull final Class<T> interfaceClass, @Nonnull final RouterConfig routerConfig) {
        assert interfaceClass != null;
        assert routerConfig != null;

        return TypedActorFactory.routerFor(interfaceClass, routerConfig);
    }

    /**
     * Returns a typed router actor that will forward all messages using the routerConfig.
     *
     * @param <T>            The type of the proxy interface.
     * @param name           Actor name.
     * @param interfaceClass The type of the proxy interface.
     * @param routerConfig   The router configuration to use.
     * @return Typed router actor.
     */
    @Nonnull
    public <T> T routerFor(@Nonnull final String name, @Nonnull final Class<T> interfaceClass, @Nonnull final RouterConfig routerConfig) {
        assert name != null;
        assert interfaceClass != null;
        assert routerConfig != null;

        return TypedActorFactory.routerFor(name, interfaceClass, routerConfig);
    }

    /**
     * Schedules the runnable to be run after duration. The runnable is run one. Note that the runnable is NOT run in
     * the context of the actor. Always use {@link #self()} to interact with the actor!
     *
     * @param duration Amount of time to wait before runnable is called.
     * @param runnable Code to run.
     * @return A Cancellable object for the runnable.
     */
    @Nonnull
    public Cancellable scheduleOnce(@Nonnull final FiniteDuration duration, @Nonnull final Runnable runnable) {
        assert duration != null;
        assert runnable != null;
        return system.scheduler().scheduleOnce(duration, runnable, executionContext());
    }

    /**
     * Schedules the runnable to be run at a given interval. There is an initial delay before the first time. Note that
     * the runnable is NOT run in the context of the actor. Always use {@link #self()} to interact with the actor!
     *
     * @param initialDelay Initial delay to wait.
     * @param interval     Amount of time to wait before runnable is called.
     * @param runnable     Code to run.
     * @return A Cancellable object for the runnable.
     */
    @Nonnull
    public Cancellable schedule(@Nonnull final FiniteDuration initialDelay, @Nonnull final FiniteDuration interval,
                                @Nonnull final Runnable runnable) {
        assert initialDelay != null;
        assert interval != null;
        assert runnable != null;
        return system.scheduler().schedule(initialDelay, interval, runnable, executionContext());
    }

    /**
     * Returns true if the Actor corresponding to the supplied TypedActor proxy is terminated.
     *
     * @param proxy The TypedActor.
     * @return true if the corresponding Actor is terminated.
     */
    public boolean actorForRefTerminated(@Nonnull final Object proxy) {
        assert proxy != null;
        return TypedActor.get(system).getActorRefFor(proxy).isTerminated();
    }
}
