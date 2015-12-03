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

package com.tomtom.speedtools.services.push.implementation.apns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Wrapper around SSLSocket.
 */
public class APNSGatewayConnection {
    private static final Logger LOG = LoggerFactory.getLogger(APNSGatewayConnection.class);

    @Nonnull
    final private Socket socket;

    // Package private constructor.
    APNSGatewayConnection(@Nonnull final Socket socket) {
        super();
        assert socket != null;
        this.socket = socket;
    }

    @Nonnull
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Nonnull
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * Closing the underlying socket will also close the socket's InputStream and OutputStream.
     */
    public void close() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (final IOException e) {
            LOG.error("close: could not close socket.", e);
        }
    }
}
