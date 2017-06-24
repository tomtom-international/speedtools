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

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;

import javax.annotation.Nonnull;


/**
 * This class defines a top-level Route Engine response.
 *
 * See {@link RouteEngine}.
 */
public final class RouteEngineResponse implements JsonRenderable {

    private final int totalDelaySeconds;
    private final int totalDistanceMeters;
    private final int totalTimeSeconds;

    public RouteEngineResponse(
            final int totalDelaySeconds,
            final int totalDistanceMeters,
            final int totalTimeSeconds) {
        assert totalDelaySeconds >= 0;
        assert totalDistanceMeters >= 0;
        assert totalTimeSeconds >= 0;
        this.totalDelaySeconds = totalDelaySeconds;
        this.totalDistanceMeters = totalDistanceMeters;
        this.totalTimeSeconds = totalTimeSeconds;
    }

    /**
     * Return the total traffic delay for a route, in seconds.
     *
     * @return Total traffic delay in seconds.
     */
    public int getTotalDelaySeconds() {
        return totalDelaySeconds;
    }

    /**
     * Return the total travel distance for a route, in meters.
     *
     * @return Total travel distance in meters.
     */
    public int getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    /**
     * Return the total traffic time for a route, in seconds.
     *
     * @return Total traffic time in seconds.
     */
    public int getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    @Override
    @Nonnull
    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }
}

