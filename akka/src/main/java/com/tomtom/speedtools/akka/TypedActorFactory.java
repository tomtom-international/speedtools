/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import akka.AkkaException;
import akka.actor.*;
import akka.japi.Creator;
import akka.routing.RouterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Since the class implements {@link Creator}, instances can be used to construct {@link TypedProps} objects. As a
 * convenience, the static {@code actorOf} methods can be used to create an actor from this factory directly. These
 * convenience methods can be used when no {@link TypedProps#withDispatcher(String)} or other Props specifics
 * are necessary.
 */
public class TypedActorFactory<T, I extends T> implements Creator<I> {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(TypedActorFactory.class);

    @Nonnull
    private final ActorSystem system;
    @Nonnull
    private final Class<T> interfaceClass;
    @Nonnull
    private final Class<I> implementationClass;
    @Nonnull
    private final Object[] explicitParameters;

    /**
     * Returns the typed actor with given path, proxied with given interface.
     *
     * @param system         Akka system.
     * @param path           Absolute path to actor.
     * @param interfaceClass Type of the proxy interface.
     * @param <T>            Type of the proxy interface.
     * @return Typed actor.
     */
    @Nonnull
    public static <T> T actorFor(
            @Nonnull final ActorSystem system,
            @Nonnull final ActorPath path,
            @Nonnull final Class<T> interfaceClass) {
        assert system != null;
        assert path != null;
        assert interfaceClass != null;

        final ActorRef actorRef = system.actorFor(path);
        return TypedActor.get(system).typedActorOf(new TypedProps<>(interfaceClass), actorRef);
    }

    /**
     * Returns the typed actor with given path, proxied with given interface.
     *
     * @param context        Akka context.
     * @param path           Relative path to actor.
     * @param interfaceClass Type of the proxy interface.
     * @param <T>            Type of the proxy interface.
     * @return Typed actor.
     */
    @Nonnull
    public static <T> T actorFor(
            @Nonnull final ActorContext context,
            @Nonnull final String path,
            @Nonnull final Class<T> interfaceClass) {
        assert context != null;
        assert path != null;
        assert interfaceClass != null;

        @Nonnull final ActorRef actorRef = context.actorFor(path);
        return TypedActor.get(context).typedActorOf(new TypedProps<>(interfaceClass), actorRef);
    }

    /**
     * Returns the typed actor with given path, proxied with given interface.
     *
     * @param name           Relative path to actor.
     * @param interfaceClass Type of the proxy interface.
     * @param <T>            Type of the proxy interface.
     * @return Typed actor.
     */
    @Nonnull
    public static <T> T actorFor(
            @Nonnull final String name,
            @Nonnull final Class<T> interfaceClass) {
        assert name != null;
        assert interfaceClass != null;

        final ActorContext context = TypedActor.context();
        final ActorRef actorRef = context.actorFor(context.self().path().child(name));
        return TypedActor.get(context).typedActorOf(new TypedProps<>(interfaceClass), actorRef);
    }

    /**
     * Returns a (named) typed router actor that will forward all messages using the routerConfig.
     *
     * @param name           Actor name (or null if unnamed).
     * @param interfaceClass The type of the proxy interface.
     * @param routerConfig   The router configuration to use.
     * @param <T>            The type of the proxy interface.
     * @return Typed router actor.
     */
    @Nonnull
    public static <T> T routerFor(
            @Nullable final String name,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final RouterConfig routerConfig) {
        assert interfaceClass != null;
        assert routerConfig != null;

        final ActorContext context = TypedActor.context();

        // Create actor for router.
        final ActorRef routerActor;
        if (name == null) {
            routerActor = context.actorOf(Props.empty().withRouter(routerConfig));
        } else {
            routerActor = context.actorOf(Props.empty().withRouter(routerConfig), name);
        }

        // Wrap actor with interfaceClass.
        final TypedProps<T> props = new TypedProps<>(interfaceClass);
        return TypedActor.get(context).typedActorOf(props, routerActor);
    }

    /**
     * Returns a typed router actor that will forward all messages using the routerConfig.
     *
     * @param interfaceClass The type of the proxy interface.
     * @param routerConfig   The router configuration to use.
     * @param <T>            The type of the proxy interface.
     * @return Typed router actor.
     */
    @Nonnull
    public static <T> T routerFor(
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final RouterConfig routerConfig) {
        assert interfaceClass != null;
        assert routerConfig != null;
        return routerFor(null, interfaceClass, routerConfig);
    }

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param name                Name of the created actor.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final String name,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert name != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(TypedActor.context(), name, interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class. The class should have a constructor with parameters that match given
     * explicitParameters. If the constructor has additional parameters and it has the Inject annotation, these
     * parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(TypedActor.context(), interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param context             Actor context.
     * @param name                Name of the created actor.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final ActorContext context,
            @Nonnull final String name,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert context != null;
        assert name != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        final ActorSystem system1 = context.system();
        final akka.actor.TypedActorFactory factory = TypedActor.get(context);
        assert system1 != null;
        assert factory != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(name, null, system1, factory, interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class. The class should have a constructor with parameters that match given
     * explicitParameters. If the constructor has additional parameters and it has the Inject annotation, these
     * parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param context             Actor context.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final ActorContext context,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert context != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        final ActorSystem system1 = context.system();
        final akka.actor.TypedActorFactory factory = TypedActor.get(context);
        assert system1 != null;
        assert factory != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(null, null, system1, factory, interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param system              Actor system.
     * @param name                Name of the created actor.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final ActorSystem system,
            @Nonnull final String name,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert name != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        final akka.actor.TypedActorFactory factory = TypedActor.get(system);
        assert system != null;
        assert factory != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(name, null, system, factory, interfaceClass, implementationClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param system              Actor system.
     * @param name                Name of the created actor.
     * @param dispatcher          The name of the dispatcher to use.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final ActorSystem system,
            @Nonnull final String name,
            @Nonnull final String dispatcher,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert name != null;
        assert dispatcher != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(name, dispatcher, system, TypedActor.get(system), interfaceClass, implementationClass,
                explicitParameters);
    }

    /**
     * Creates an untyped actor of given class. The class should have a constructor with parameters that match given
     * explicitParameters. If the constructor has additional parameters and it has the Inject annotation, these
     * parameters will be injected.
     *
     * @param <T>                 Interface.
     * @param <I>                 Instance.
     * @param system              Actor system.
     * @param interfaceClass      Interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final ActorSystem system,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        final akka.actor.TypedActorFactory factory = TypedActor.get(system);
        assert system != null;
        assert factory != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(null, null, system, factory, interfaceClass, implementationClass, explicitParameters);
    }

    @Nonnull
    public static <T, I extends T> T actorOf(
            @Nonnull final String name,
            @Nonnull final ActorSystem system,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert name != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        final akka.actor.TypedActorFactory factory = TypedActor.get(system);
        assert system != null;
        assert factory != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        return actorOf(name, null, system, factory, interfaceClass, implementationClass, explicitParameters);
    }

    @Nonnull
    private static <T, I extends T> T actorOf(
            @Nullable final String name,
            @Nullable final String dispatcher,
            @Nonnull final ActorSystem system,
            @Nonnull final akka.actor.TypedActorFactory factory,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert system != null;
        assert factory != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;

        // Actor factory registers context and self as side-effect.
        final TypedActorFactory<T, I> actorFactory =
                new TypedActorFactory<>(system, interfaceClass, implementationClass, explicitParameters);

        // Props.
        final TypedProps<I> props;
        if (dispatcher != null) {
            props = new TypedProps<>(interfaceClass, actorFactory).withDispatcher(dispatcher);
        } else {
            props = new TypedProps<>(interfaceClass, actorFactory);
        }

        // Construct the actor.
        try {
            final I actor;
            if (name != null) {
                actor = factory.typedActorOf(props, name);
            } else {
                actor = factory.typedActorOf(props);
            }
            LOG.debug("actorOf: Created actor '{}' of class {}", name, implementationClass.getName());
            return actor;
        } catch (final ActorFactoryException e) {
            LOG.error("actorOf: Actor factory exception, name=" + name + ", class=" + implementationClass.getName(), e);
            throw e;
        } catch (final InvalidActorNameException e) {

            /**
             *  This is not necessarily an error as we expect this exception during a "get or create" of an actor,
             *  so don't print the exception stack trace; just the exception message.
             */
            LOG.debug("actorOf: Actor might already exist (or invalid name) name=" + name +
                    ", class=" + implementationClass.getName() + ", exception=" + e.getMessage());
            throw e;
        } catch (final AkkaException e) {
            LOG.error("actorOf: Akka exception, name=" + name + ", class=" + implementationClass.getName(), e);
            throw e;
        } catch (final Exception e) {
            LOG.error("actorOf: Unknown exception, name=" + name + ", class=" + implementationClass.getName(), e);
            throw new ActorFactoryException("Couldn't create actor " + name +
                    " of class " + implementationClass.getName(), e);
        }
    }

    /**
     * Constructor.
     *
     * @param system              Actor system.
     * @param interfaceClass      Class of the interface of the untyped actor to create.
     * @param implementationClass Class of the untyped actor to create.
     * @param explicitParameters  Explicit constructor parameters, or empty.
     */
    public TypedActorFactory(
            @Nonnull final ActorSystem system,
            @Nonnull final Class<T> interfaceClass,
            @Nonnull final Class<I> implementationClass,
            @Nonnull final Object... explicitParameters) {
        assert system != null;
        assert interfaceClass != null;
        assert implementationClass != null;
        assert explicitParameters != null;
        this.system = system;
        this.interfaceClass = interfaceClass;
        this.implementationClass = implementationClass;
        this.explicitParameters = explicitParameters;
    }

    @Nonnull
    @Override
    public I create() {

        // At this point, the actor context is set, so we can construct a context.
        final TypedActorContext<T> context = new TypedActorContext<>(system, TypedActor.context(), interfaceClass);

        // Create he actor instance.
        return ActorFactory.newInstance(system, implementationClass, context, explicitParameters);
    }
}
