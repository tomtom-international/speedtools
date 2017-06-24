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

package com.tomtom.speedtools.services.lbs.route;


import com.tomtom.speedtools.geometry.GeoPoint;
import org.joda.time.Duration;
import scala.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This interface wraps the TomTom LBS Geocoding functionality, found at http://lbs.tomtom.com.
 *
 * The interface is set up to return Futures, because calculating a routing is a pretty lengthy operation, so an
 * asynchronous interface makes sense.
 */
public interface RouteEngine {

    /**
     * Calculate a route. No timeout is set.
     *
     * @param from    Departure point.
     * @param to      Arrival point.
     * @param timeout Timeout after this period. Null if no timeout set.
     * @return Future response with routing result.
     */
    @Nonnull
    Future<RouteEngineResponse> route(
            @Nonnull GeoPoint from,
            @Nonnull GeoPoint to,
            @Nullable Duration timeout);

    /**
     * Calculate a route. No timeout is set.
     *
     * @param from Departure point.
     * @param to   Arrival point.
     * @return Future response with routing result.
     */
    @Nonnull
    Future<RouteEngineResponse> route(
            @Nonnull GeoPoint from,
            @Nonnull GeoPoint to);
}
