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

package com.tomtom.speedtools.services.push.implementation.gcm;


import com.tomtom.speedtools.guice.InvalidPropertyValueException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Class holds properties for connecting to Google Cloud Messaging Server.
 */
public class GCMProperties {

    @Nullable
    private final String endpoint;
    @Nonnull
    private final String apiKey;
    @Nonnull
    private final Integer retries;
    @Nonnull
    private final Integer maxInstances;
    private final boolean enabled;

    /**
     * @param endpoint     The GCM endpoint. If not specified the default should be used {@link
     *                     com.google.android.gcm.server.Constants#GCM_SEND_ENDPOINT}.
     * @param apiKey       The api key for this application.
     * @param retries      The number of retries to send a message.
     * @param maxInstances The number of maximum instances of GCM connection that should be used simultaneously. This
     *                     should be 0 or greater.
     * @param enabled      When set to true messages should be pushed.
     * @throws InvalidPropertyValueException when the retries or maxInstances are out of range and when both are less
     *                                       than zero.
     */
    @Inject
    public GCMProperties(
            @Named("PushNotificationProvider.GCM.endpoint") @Nullable final String endpoint,
            @Named("PushNotificationProvider.GCM.apiKey") @Nonnull final String apiKey,
            @Named("PushNotificationProvider.GCM.retries") @Nonnull final Integer retries,
            @Named("PushNotificationProvider.GCM.maxInstances") @Nonnull final Integer maxInstances,
            @Named("PushNotificationProvider.GCM.enabled") final boolean enabled) {

        assert apiKey != null;
        assert retries != null;
        assert maxInstances != null;

        if (retries < 0) {
            throw new InvalidPropertyValueException("retries out of range, must be >= 0:" + retries);
        }

        if (maxInstances < 0) {
            throw new InvalidPropertyValueException(
                    "maxInstances out of range, must be >= 0:" + maxInstances);
        }

        this.apiKey = apiKey;
        this.retries = retries;
        this.maxInstances = maxInstances;
        this.endpoint = ((endpoint == null) || endpoint.trim().isEmpty()) ? null : endpoint.trim();
        this.enabled = enabled;
    }

    /**
     * Method returns the end point in case or null when it is not specified. In case null the default should be used
     * {@link com.google.android.gcm.server.Constants#GCM_SEND_ENDPOINT}.
     *
     * @return The end point in case or null when it is not specified.
     */
    @Nullable
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Returns API key for this application.
     *
     * @return API key for this application.
     */
    @Nonnull
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the number of retries for sending a message.
     *
     * @return Number of retries.
     */
    @Nonnull
    public Integer getRetries() {
        return retries;
    }

    /**
     * Returns maximum instances that can simultaneously exist.
     *
     * @return Maximum instances.
     */
    @Nonnull
    public Integer getMaxInstances() {
        return maxInstances;
    }

    /**
     * Returns true in case enabled, false otherwise.
     *
     * @return True in case enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
