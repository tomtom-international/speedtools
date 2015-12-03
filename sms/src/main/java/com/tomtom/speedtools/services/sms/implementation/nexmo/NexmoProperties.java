/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.services.sms.implementation.nexmo;

import com.tomtom.speedtools.guice.InvalidPropertyValueException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configuration properties for the Nexmo SMS Provider.
 */
public class NexmoProperties {

    @Nonnull
    private final String baseUrl;
    @Nonnull
    private final String userName;
    @Nonnull
    private final String password;
    @Nonnull
    private final String sender;
    private final boolean mockEnabled;

    /**
     * Constructor for NexmoProperties.
     *
     * @param baseUrl     The baseurl to connect to.
     * @param userName    The userName of the Nexmo account.
     * @param password    The password of the Nexmo account.
     * @param sender      The sender that used in the sms messages.
     * @param mockEnabled If true, the Nexmo Provider Connector uses a mock NexmoResource instead of going out to the
     *                    real Nexmo SMS Service.
     */
    @Inject
    public NexmoProperties(
            @Named("SMSProvider.Nexmo.baseUrl") @Nonnull final String baseUrl,
            @Named("SMSProvider.Nexmo.userName") @Nonnull final String userName,
            @Named("SMSProvider.Nexmo.password") @Nonnull final String password,
            @Named("SMSProvider.Nexmo.sender") @Nonnull final String sender,
            @Named("SMSProvider.Nexmo.mockEnabled") final boolean mockEnabled) throws InvalidPropertyValueException {
        assert userName != null;
        assert password != null;
        assert sender != null;
        assert baseUrl != null;

        // Check baseUrl.
        try {
            //noinspection UnusedDeclaration,UnusedAssignment
            final URL url = new URL(baseUrl);
        } catch (final MalformedURLException ignored) {
            throw new InvalidPropertyValueException("SMSProvider.Nexmo.baseUrl is not a valid URL.");
        }

        if (userName.isEmpty()) {
            throw new InvalidPropertyValueException("SMSProvider.Nexmo.userName must not be empty.");
        }

        if (password.isEmpty()) {
            throw new InvalidPropertyValueException("SMSProvider.Nexmo.password must not be empty.");
        }

        if (sender.isEmpty()) {
            throw new InvalidPropertyValueException("SMSProvider.Nexmo.sender must not be empty.");
        }

        this.baseUrl = baseUrl.trim();
        this.userName = userName.trim();
        this.password = password.trim();
        this.sender = sender.trim();
        this.mockEnabled = mockEnabled;
    }

    @Nonnull
    public String getBaseUrl() {
        return baseUrl;
    }


    @Nonnull
    public String getUserName() {
        return userName;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    @Nonnull
    public String getSender() {
        return sender;
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }
}
