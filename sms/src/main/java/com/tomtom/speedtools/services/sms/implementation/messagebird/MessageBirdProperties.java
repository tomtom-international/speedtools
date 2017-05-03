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

package com.tomtom.speedtools.services.sms.implementation.messagebird;

import com.tomtom.speedtools.guice.InvalidPropertyValueException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configuration properties for the MessageBird SMS Provider.
 */
public class MessageBirdProperties {

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
     * Constructor for MessageBirdProperties.
     *
     * @param baseUrl     The baseurl to connect to.
     * @param userName    The userName of the MessageBird account.
     * @param password    The password of the MessageBird account.
     * @param sender      The sender that used in the sms messages (this will be visible to the end user receiving the
     *                    message).
     * @param mockEnabled If true, the MessageBird Provider Connector uses a mock MessageBirdResource instead of going
     *                    out to the real Nexmo SMS Service.
     */
    @Inject
    public MessageBirdProperties(
            @Named("SMSProvider.MessageBird.baseUrl") @Nonnull final String baseUrl,
            @Named("SMSProvider.MessageBird.userName") @Nonnull final String userName,
            @Named("SMSProvider.MessageBird.password") @Nonnull final String password,
            @Named("SMSProvider.MessageBird.sender") @Nonnull final String sender,
            @Named("SMSProvider.MessageBird.mockEnabled") final boolean mockEnabled) throws InvalidPropertyValueException {
        assert userName != null;
        assert password != null;
        assert sender != null;
        assert baseUrl != null;

        // Check baseUrl.
        try {
            //noinspection ResultOfObjectAllocationIgnored
            new URL(baseUrl);
        } catch (final MalformedURLException ignored) {
            throw new InvalidPropertyValueException("SMSProvider.MessageBird.baseUrl is not a valid URL.");
        }

        if (userName.isEmpty()) {
            throw new InvalidPropertyValueException("SMSProvider.MessageBird.userName must not be empty.");
        }

        if (password.isEmpty()) {
            throw new InvalidPropertyValueException("SMSProvider.MessageBird.password must not be empty.");
        }

        if (sender.isEmpty()) {
            throw new InvalidPropertyValueException("SMSProvider.MessageBird.sender must not be empty.");
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
