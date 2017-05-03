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

package com.tomtom.speedtools.services.lbs.route.implementation;

import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedActorExtension;
import akka.actor.TypedProps;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.services.lbs.LbsProperties;
import com.tomtom.speedtools.services.lbs.route.RouteEngine;
import com.tomtom.speedtools.services.lbs.route.RouteEngineResponse;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.Duration;
import scala.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Implementation of a {@link RouteEngine} using the TomTom LBS Router. This
 * implementation creates a number of actors to dispatch the routing calls to. This ensures LBS will not be flooded by
 * too many parallel calls. Should there be too many calls, then the calls will timeout prematurely to the caller of the
 * actor to avoid the original caller waiting too long.
 */
public final class TomTomLbsRouteEngine implements RouteEngine {

    /**
     * The routing actor is actually a router to worker routing actors.
     */
    @Nonnull
    private final
    ConcurrentLinkedQueue<TomTomLbsRouteEngineActor> routeActorList;

    @Inject
    public TomTomLbsRouteEngine(
            @Nonnull final ActorSystem system,
            @Nonnull final LbsProperties lbsProperties) {
        assert lbsProperties != null;

        // TODO ST-3: Create a top-level supervisor actor which implements a strategy for these workers.
        // Create a bunch of worker actors.
        final TypedActorExtension typedActorExtension = TypedActor.get(system);
        routeActorList = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < lbsProperties.getNumberOfRouteActors(); ++i) {
            final String name = "route-" + Integer.toString(i + 1);
            final TypedProps<TomTomLbsRouteEngineActor> typedProps =
                    new TypedProps<>(TomTomLbsRouteEngineActor.class, () -> {
                        final TomTomLbsRouteEngineRouteEngineActorImpl actor =
                                new TomTomLbsRouteEngineRouteEngineActorImpl(name, lbsProperties);
                        return actor;
                    });
            final TomTomLbsRouteEngineActor worker = typedActorExtension.typedActorOf(typedProps);
            routeActorList.offer(worker);
        }
    }

    @Nonnull
    @Override
    public Future<RouteEngineResponse> route(
            @Nonnull final GeoPoint from,
            @Nonnull final GeoPoint to,
            @Nullable final Duration timeout) {
        assert from != null;
        assert to != null;

        // Get least actor from head.
        final TomTomLbsRouteEngineActor actor = routeActorList.poll();

        // And add it to the tail again.
        routeActorList.offer(actor);

        /**
         * Call the routing actor. Note that this "method call" is really a message that ends up in the
         * message box the actor. By the time it gets processed, the deadline (executeBefore) may have passed
         * already. The actor can then decide not to process the message at all anymore.
         */
        final Future<RouteEngineResponse> route = actor.route(from, to,
                (timeout == null) ? null : UTCTime.now().plus(timeout));
        return route;
    }

    @Nonnull
    @Override
    public Future<RouteEngineResponse> route(
            @Nonnull final GeoPoint from,
            @Nonnull final GeoPoint to) {
        final Future<RouteEngineResponse> route = route(from, to, null);
        return route;
    }
}
