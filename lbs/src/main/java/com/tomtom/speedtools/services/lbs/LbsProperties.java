/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.services.lbs;

import com.tomtom.speedtools.guice.HasProperties;
import com.tomtom.speedtools.guice.InvalidPropertyValueException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * TomTom LBS properties.
 */
public final class LbsProperties implements HasProperties {

    @Nonnull
    private final String apiKey;
    @Nonnull
    private final String geoCodeUrl1;
    @Nonnull
    private final String geoCodeUrl2;
    @Nonnull
    private final String tileMapUrl;
    @Nonnull
    private final String routeUrl1;
    private final boolean trafficEnabled;
    private final int numberOfRouteActors;

    /**
     * LBS properties.
     *
     * @param apiKey              API key to use for LBS connector. Cannot be empty.
     * @param geoCodeUrl1         URL of geocoding service version 1. Must include "http://" and be a valid URL.
     * @param geoCodeUrl2         URL of geocoding service version 2. Must include "http://" and be a valid URL.
     * @param tileMapUrl          URL of tile mapping service. Must include "http://" and be a valid URL.
     * @param routeUrl1           URL of routing service version 1. Must include "http://" and be a valid URL.
     * @param trafficEnabled      if true, consider traffic jams in routing calls.
     * @param numberOfRouteActors Number of route actors to use, for parallel requests to LBS. Must be &gt; 0.
     */
    @Inject
    public LbsProperties(
            @Named("LBS.apiKey") @Nonnull final String apiKey,
            @Named("LBS.geoCodeUrl1") @Nonnull final String geoCodeUrl1,
            @Named("LBS.geoCodeUrl2") @Nonnull final String geoCodeUrl2,
            @Named("LBS.tileMapUrl") @Nonnull final String tileMapUrl,
            @Named("LBS.routeUrl1") @Nonnull final String routeUrl1,
            @Named("LBS.trafficEnabled") final boolean trafficEnabled,
            @Named("LBS.numberOfRouteActors") final int numberOfRouteActors)
            throws InvalidPropertyValueException {
        assert apiKey != null;
        assert geoCodeUrl1 != null;

        if (apiKey.isEmpty()) {
            throw new InvalidPropertyValueException("LBS.apiKey cannot be empty.");
        }

        try {
            //noinspection ResultOfObjectAllocationIgnored
            new URL(geoCodeUrl1);
        } catch (final MalformedURLException ignored) {
            throw new InvalidPropertyValueException("LBS.geoCodeUrl1 is not a valid URL.");
        }

        try {
            //noinspection ResultOfObjectAllocationIgnored
            new URL(geoCodeUrl2);
        } catch (final MalformedURLException ignored) {
            throw new InvalidPropertyValueException("LBS.geoCodeUrl2 is not a valid URL.");
        }

        try {
            //noinspection ResultOfObjectAllocationIgnored
            new URL(tileMapUrl);
        } catch (final MalformedURLException ignored) {
            throw new InvalidPropertyValueException("LBS.tileMapUrl is not a valid URL.");
        }

        try {
            //noinspection ResultOfObjectAllocationIgnored
            new URL(routeUrl1);
        } catch (final MalformedURLException ignored) {
            throw new InvalidPropertyValueException("LBS.routeUrl1 is not a valid URL.");
        }

        if (numberOfRouteActors <= 0) {
            throw new InvalidPropertyValueException("LBS.numberOfRouteActors must be > 0.");
        }

        this.apiKey = apiKey;
        this.geoCodeUrl1 = geoCodeUrl1;
        this.geoCodeUrl2 = geoCodeUrl2;
        this.tileMapUrl = tileMapUrl;
        this.routeUrl1 = routeUrl1;
        this.trafficEnabled = trafficEnabled;
        this.numberOfRouteActors = numberOfRouteActors;
    }

    @Nonnull
    public String getApiKey() {
        return apiKey;
    }

    @Nonnull
    public String getGeoCodeUrl1() {
        return geoCodeUrl1;
    }

    @Nonnull
    public String getGeoCodeUrl2() {
        return geoCodeUrl2;
    }

    @Nonnull
    public String getTileMapUrl() {
        return tileMapUrl;
    }

    @Nonnull
    public String getRouteUrl1() {
        return routeUrl1;
    }

    public boolean isTrafficEnabled() {
        return trafficEnabled;
    }

    public int getNumberOfRouteActors() {
        return numberOfRouteActors;
    }
}
