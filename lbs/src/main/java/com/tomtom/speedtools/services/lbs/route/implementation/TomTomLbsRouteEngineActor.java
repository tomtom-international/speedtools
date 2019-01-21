/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.services.lbs.route.implementation;


import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.services.lbs.route.RouteEngineResponse;
import org.joda.time.DateTime;
import scala.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This interface wraps the TomTom LBS Geocoding functionality, found at http://lbs.tomtom.com.
 */
public interface TomTomLbsRouteEngineActor {

    /**
     * Calculate a route. No timeout is set.
     *
     * @param from          Departure point.
     * @param to            Arrival point.
     * @param executeBefore Throw a TimeoutException if execution starts after this time. This can happen if a number of
     *                      calls are added to this actors message queue and processing is delayed. If null, no deadline
     *                      is given
     * @return Future response with routing result.
     */
    @Nonnull
    Future<RouteEngineResponse> route(
            @Nonnull GeoPoint from,
            @Nonnull GeoPoint to,
            @Nullable DateTime executeBefore);
}
