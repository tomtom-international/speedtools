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

package com.tomtom.speedtools.services.push.implementation.apns;


import com.tomtom.speedtools.guice.InvalidPropertyValueException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Class holds properties for connecting to Apple Push Notification Server.
 */
public class APNSProperties {

    private static final int MIN_PORT_NR = 1;
    private static final int MAX_PORT_NR = 0xFFFF;

    @Nonnull
    private final String gatewayHost;
    @Nonnull
    private final Integer gatewayPort;
    @Nonnull
    private final String feedbackHost;
    @Nonnull
    private final Integer feedbackPort;
    @Nonnull
    private final String p12KeyStorePath;
    @Nonnull
    private final String p12KeyStorePassword;
    @Nonnull
    private final Integer maxInstances;
    private final boolean enabled;

    /**
     * @param gatewayHost         The host name of the gateway. This can be DNS name or IP number.
     * @param gatewayPort         The port of the gateway. The port number must be in range: [1-65535].
     * @param feedbackHost        The host name of the feedback service. This can be DNS name or IP number.
     * @param feedbackPort        The port of the feedback service. The port number must be in range: [1-65535].
     * @param p12KeyStorePath     The path to the P12 keys tore file.
     * @param p12KeyStorePassword The password of the P12 key store.
     * @param maxInstances        The number of maximum instances of APNS connection that should be used simultaneously.
     *                            This should be 0 or greater.
     * @param enabled             When set to true messages should be pushed.
     * @throws InvalidPropertyValueException when the gatewayPort or feedbackPort are out of range and when the
     *                                       maxInstances is less than zero.
     */
    @Inject
    public APNSProperties(
            @Named("PushNotificationProvider.APNS.gateway.host") @Nonnull final String gatewayHost,
            @Named("PushNotificationProvider.APNS.gateway.port") @Nonnull final Integer gatewayPort,
            @Named("PushNotificationProvider.APNS.feedback.host") @Nonnull final String feedbackHost,
            @Named("PushNotificationProvider.APNS.feedback.port") @Nonnull final Integer feedbackPort,
            @Named("PushNotificationProvider.APNS.p12KeyStore.path") @Nonnull final String p12KeyStorePath,
            @Named("PushNotificationProvider.APNS.p12KeyStore.password") @Nonnull final String p12KeyStorePassword,
            @Named("PushNotificationProvider.APNS.maxInstances") @Nonnull final Integer maxInstances,
            @Named("PushNotificationProvider.APNS.enabled") final boolean enabled) {

        assert gatewayHost != null;
        assert gatewayPort != null;
        assert feedbackHost != null;
        assert feedbackPort != null;
        assert p12KeyStorePath != null;
        assert p12KeyStorePassword != null;
        assert maxInstances != null;

        if ((gatewayPort < MIN_PORT_NR) || (gatewayPort > MAX_PORT_NR)) {
            throw new InvalidPropertyValueException("gatewayPort out of range, must be in range [" +
                    MIN_PORT_NR + ',' + MAX_PORT_NR + "]:" + gatewayPort);
        }

        if ((feedbackPort < MIN_PORT_NR) || (feedbackPort > MAX_PORT_NR)) {
            throw new InvalidPropertyValueException("feedbackPort out of range, must be in range [" +
                    MIN_PORT_NR + ',' + MAX_PORT_NR + "]:" + feedbackPort);
        }

        if (p12KeyStorePath.isEmpty()) {
            throw new InvalidPropertyValueException("p12KeyStore.path cannot be empty");
        }

        if (p12KeyStorePassword.isEmpty()) {
            throw new InvalidPropertyValueException("p12KeyStore.password cannot be empty");
        }

        if (maxInstances < 0) {
            throw new InvalidPropertyValueException(
                    "maxInstances out of range, must be greater than 0:" + maxInstances);
        }


        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.feedbackHost = feedbackHost;
        this.feedbackPort = feedbackPort;
        this.p12KeyStorePath = p12KeyStorePath;
        this.p12KeyStorePassword = p12KeyStorePassword;
        this.maxInstances = maxInstances;
        this.enabled = enabled;
    }


    /**
     * Returns host name of push service. For the sandbox use gateway.sandbox.push.apple.com and for production
     * gateway.push.apple.com should be used.
     *
     * @return Host name of push service.
     */
    @Nonnull
    public String getGatewayHost() {
        return gatewayHost;
    }

    /**
     * Returns TCP port of push service. The default is 2195.
     *
     * @return Port of push service.
     */
    @Nonnull
    public Integer getGatewayPort() {
        return gatewayPort;
    }

    /**
     * Returns host name of feedback service. Used to be notified of expired push tokens. For the sandbox use
     * feedback.sandbox.push.apple.com and for production feedback.push.apple.com should be used.
     *
     * @return Host name of feedback service.
     */
    @Nonnull
    public String getFeedbackHost() {
        return feedbackHost;
    }

    /**
     * Returns TCP port of feedback service. The default is 2195.
     *
     * @return Port of feedback service.
     */
    @Nonnull
    public Integer getFeedbackPort() {
        return feedbackPort;
    }

    /**
     * Returns path to certificate.
     *
     * @return Path to certificate.
     */
    @Nonnull
    public String getP12KeyStorePath() {
        return p12KeyStorePath;
    }

    /**
     * Returns password of certificate.
     *
     * @return Password of certificate.
     */
    @Nonnull
    public String getP12KeyStorePassword() {
        return p12KeyStorePassword;
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
