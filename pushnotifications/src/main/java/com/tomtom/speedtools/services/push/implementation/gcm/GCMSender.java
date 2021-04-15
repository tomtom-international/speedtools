/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.services.push.implementation.gcm;

import com.google.android.gcm.server.Endpoint;
import com.google.android.gcm.server.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * This class can be used to override the use of the standard endpoint.
 */
public class GCMSender extends Sender {
    private static final Logger LOG = LoggerFactory.getLogger(GCMSender.class);

    @Nonnull
    private final String endpoint;

    public GCMSender(@Nullable final String endpoint, @Nonnull final String key) {
        super(key, Endpoint.GCM);
        assert key != null;
        this.endpoint = (endpoint == null) ? Endpoint.GCM.toString() : endpoint;

        LOG.debug("GCMSender using endpoint={}", endpoint);
    }

    @Override
    @Nonnull
    protected HttpURLConnection post(
            @Nonnull final String url,
            @Nonnull final String contentType,
            @Nonnull final String body) throws IOException {

        assert url != null;
        assert contentType != null;
        assert body != null;

        return super.post(endpoint, contentType, body);
    }
}
