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
import com.google.common.collect.MapMaker;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;

/**
 * Actor factory that will inject constructor parameters. Supports Google Guice. An actor system should be provided with
 * Guice injector by means for {@link #setInjector(ActorSystem, Injector)}.
 *
 * Since the class implements UntypedActorFactory, instances can be used to construct {@link Props} objects. As
 * a convenience, the static {@code actorOf} methods can be used to create an actor from this factory directly. These
 * convenience methods can be used when no {@link Props#withRouter(akka.routing.RouterConfig)}, or other Props specifics
 * are necessary.
 */
@SuppressWarnings("deprecation")
public class ActorFactory implements UntypedActorFactory {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ActorFactory.class);

    private static final long serialVersionUID = 1L;

    /**
     * Since there can be multiple actor systems, for each one an injector is maintained. This map has weak keys to
     * allow actor systems to be garbage collected. This is necessary for unit testing, where many tests might create
     * their own systems.
     */
    private static final ConcurrentMap<ActorSystem, Injector> injectorMap = new MapMaker().weakKeys().makeMap();

    @Nonnull
    private final ActorSystem system;
    @Nonnull
    private final Class<? extends UntypedActor> actorClass;
    @Nonnull
    private final Object[] explicitParameters;

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param context            Actor context.
     * @param name               Name of the created actor.
     * @param actorClass         Class of the untyped actor to create.
     * @param explicitParameters Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static ActorRef actorOf(
            @Nonnull final ActorContext context,
            @Nonnull final String name,
            @Nonnull final Class<? extends UntypedActor> actorClass,
            @Nonnull final Object... explicitParameters) {
        assert context != null;
        assert name != null;
        assert actorClass != null;
        assert explicitParameters != null;
        return actorOf(name, context.system(), context, actorClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class. The class should have a constructor with parameters that match given
     * explicitParameters. If the constructor has additional parameters and it has the Inject annotation, these
     * parameters will be injected.
     *
     * @param context            Actor context.
     * @param actorClass         Class of the untyped actor to create.
     * @param explicitParameters Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static ActorRef actorOf(
            @Nonnull final ActorContext context,
            @Nonnull final Class<? extends UntypedActor> actorClass,
            @Nonnull final Object... explicitParameters) {
        assert context != null;
        assert actorClass != null;
        assert explicitParameters != null;
        return actorOf(null, context.system(), context, actorClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class with a given name. The class should have a constructor with parameters
     * that match given explicitParameters. If the constructor has additional parameters and it has the Inject
     * annotation, these parameters will be injected.
     *
     * @param system             Actor system.
     * @param name               Name of the created actor.
     * @param actorClass         Class of the untyped actor to create.
     * @param explicitParameters Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static ActorRef actorOf(
            @Nonnull final ActorSystem system,
            @Nonnull final String name,
            @Nonnull final Class<? extends UntypedActor> actorClass,
            @Nonnull final Object... explicitParameters) {
        assert name != null;
        assert actorClass != null;
        assert explicitParameters != null;
        return actorOf(name, system, system, actorClass, explicitParameters);
    }

    /**
     * Creates an untyped actor of given class. The class should have a constructor with parameters that match given
     * explicitParameters. If the constructor has additional parameters and it has the Inject annotation, these
     * parameters will be injected.
     *
     * @param system             Actor system.
     * @param actorClass         Class of the untyped actor to create.
     * @param explicitParameters Parameters to be passed to the constructor.
     * @return Actor reference.
     */
    @Nonnull
    public static ActorRef actorOf(
            @Nonnull final ActorSystem system,
            @Nonnull final Class<? extends UntypedActor> actorClass,
            @Nonnull final Object... explicitParameters) {
        assert actorClass != null;
        assert explicitParameters != null;
        return actorOf(null, system, system, actorClass, explicitParameters);
    }

    @Nonnull
    private static ActorRef actorOf(
            @Nullable final String name,
            @Nonnull final ActorSystem system,
            @Nonnull final ActorRefFactory factory,
            @Nonnull final Class<? extends UntypedActor> actorClass,
            @Nonnull final Object... explicitParameters) {
        assert system != null;
        assert factory != null;
        assert actorClass != null;
        assert explicitParameters != null;

        @Nonnull final Props props = Props.create(actorClass, explicitParameters);

        // Construct the actor.
        try {
            if (name != null) {
                return factory.actorOf(props, name);
            } else {
                return factory.actorOf(props);
            }
        } catch (final Exception e) {
            throw new ActorFactoryException("Couldn't create actor of class " + actorClass.getName(), e);
        }
    }

    /**
     * Constructor.
     *
     * @param system             Actor system.
     * @param actorClass         Actor class.
     * @param explicitParameters Explicit constructor parameters, or empty.
     */
    public ActorFactory(
            @Nonnull final ActorSystem system,
            @Nonnull final Class<? extends UntypedActor> actorClass,
            @Nonnull final Object... explicitParameters) {
        assert system != null;
        assert actorClass != null;
        assert explicitParameters != null;
        this.system = system;
        this.actorClass = actorClass;
        this.explicitParameters = explicitParameters;
    }

    @Nonnull
    @Override
    public Actor create() {
        return newInstance(system, actorClass, null, explicitParameters);
    }

    @Nonnull
    public static <T, Impl> Impl newInstance(
            @Nonnull final ActorSystem system,
            @Nonnull final Class<Impl> implementationClass,
            @Nullable final TypedActorContext<T> context,
            @Nonnull final Object... explicitParameters) {
        assert system != null;
        assert implementationClass != null;
        assert explicitParameters != null;

        // Find the bean factory and injector for given system.
        final Injector injector = injectorMap.get(system);

        LOG.trace("newInstance: actorSystem.hashcode={}, injector.hashcode={}, implementationClass={}",
                System.identityHashCode(system), System.identityHashCode(injector), implementationClass.getSimpleName());

        // Find the right constructor.
        final Constructor<?> ctor = findConstructor(implementationClass, explicitParameters);

        // Match all constructor parameters.
        final int firstExplicitParameter = ctor.getParameterTypes().length - explicitParameters.length;
        assert firstExplicitParameter >= 0;
        final Object[] parameters = new Object[ctor.getParameterTypes().length];
        System.arraycopy(explicitParameters, 0, parameters, firstExplicitParameter, explicitParameters.length);
        for (int i = 0; i < firstExplicitParameter; ++i) {
            assert injector != null;
            final Class<?> parameterType = ctor.getParameterTypes()[i];

            // Special case: typed actor context.
            if (TypedActorContext.class.isAssignableFrom(parameterType)) {
                if (context == null) {
                    throw new ActorFactoryException("No actor context was provided for actor of class " +
                            implementationClass.getName());
                }
                parameters[i] = context;
            } else {
                LOG.trace("newInstance: i={}, parameterType={}", i, parameterType.getSimpleName());
                parameters[i] =
                        injector.getInstance(findGuiceKey(parameterType, ctor.getParameterAnnotations()[i]));
            }
        }

        try {
            LOG.trace("newInstance: calling ctor.newInstance(). implementationClass={}",
                    implementationClass.getSimpleName());
            //noinspection unchecked
            return (Impl) ctor.newInstance(parameters);
        } catch (final Exception e) {
            throw new ActorFactoryException("Couldn't create actor of class " + implementationClass.getName(), e);
        }
    }

    public static void setInjector(
            @Nonnull final ActorSystem system,
            @Nonnull final Injector injector) {
        assert system != null;
        assert injector != null;
        assert !injectorMap.containsKey(system);
        injectorMap.put(system, injector);
    }

    @Nonnull
    private static Constructor<?> findConstructor(
            @Nonnull final Class<?> c,
            @Nonnull final Object... explicitParameters) {
        assert c != null;
        assert explicitParameters != null;

        for (final Constructor<?> ctor : c.getDeclaredConstructors()) {

            // Only public constructors are allowed.
            // Compatible with explicit parameters?
            if (Modifier.isPublic(ctor.getModifiers()) &&
                    isCompatible(ctor, explicitParameters)) {

                // Constructor only has explicit parameters?
                if (ctor.getParameterTypes().length == explicitParameters.length) {
                    return ctor;
                }

                // If there are extra parameters, there should be an @Inject
                // annotation.
                assert ctor.getParameterTypes().length > explicitParameters.length;
                final Inject annotation = ctor.getAnnotation(Inject.class);
                if (annotation != null) {
                    return ctor;
                }

            }
        }
        // No constructor was found with at least explicitParameters or with more than explicitParameters but no @Inject.
        throw new ActorFactoryException("No valid public constructor for actor class " + c.getName());
    }

    private static boolean isCompatible(
            @Nonnull final Constructor<?> ctor,
            @Nonnull final Object... explicitParameters) {
        assert ctor != null;
        assert explicitParameters != null;

        final int firstExplicitParameter = ctor.getParameterTypes().length - explicitParameters.length;
        if (firstExplicitParameter < 0) {
            return false;
        }
        for (int i = 0; i < explicitParameters.length; ++i) {
            if ((explicitParameters[i] != null) && !ctor.getParameterTypes()[firstExplicitParameter + i].isInstance
                    (explicitParameters[i])) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static <T> Key<T> findGuiceKey(
            @Nonnull final Class<T> type,
            @Nonnull final Annotation[] annotations) {
        assert type != null;
        assert annotations != null;

        for (final Annotation annotation : annotations) {
            if (annotation instanceof Named) {
                return Key.get(type, annotation);
            }
            if (annotation instanceof com.google.inject.name.Named) {
                return Key.get(type, annotation);
            }
        }
        return Key.get(type);
    }
}
