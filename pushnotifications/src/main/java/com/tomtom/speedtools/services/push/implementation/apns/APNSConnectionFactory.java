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

package com.tomtom.speedtools.services.push.implementation.apns;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Factory is able to create connections to a push socket and a feedback socket of APNS.
 */
public final class APNSConnectionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(APNSConnectionFactory.class);

    private static final String ALGORITHM = "TLS";
    private static final String KEY_STORE_TYPE = "PKCS12";

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

    public APNSConnectionFactory(@Nonnull final String gatewayHost,
                                 @Nonnull final Integer gatewayPort,
                                 @Nonnull final String feedbackHost,
                                 @Nonnull final Integer feedbackPort,
                                 @Nonnull final String p12KeyStorePath,
                                 @Nonnull final String p12KeyStorePassword) {
        assert gatewayHost != null;
        assert gatewayPort != null;
        assert feedbackHost != null;
        assert feedbackPort != null;
        assert p12KeyStorePath != null;
        assert p12KeyStorePassword != null;

        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.feedbackHost = feedbackHost;
        this.feedbackPort = feedbackPort;
        this.p12KeyStorePath = p12KeyStorePath;
        this.p12KeyStorePassword = p12KeyStorePassword;
    }

    /**
     * Returns a APNSGatewayConnection to APNS push socket.
     *
     * @return A APNSGatewayConnection to APNS push socket.
     * @throws APNSGatewayConnectionException in case a connection could not be created.
     */
    @Nonnull
    public APNSGatewayConnection createPushConnection() throws APNSGatewayConnectionException {
        return new APNSGatewayConnection(createConnection(gatewayHost, gatewayPort));
    }

    /**
     * Returns a APNSGatewayConnection to APNS feedback socket.
     *
     * @return A APNSGatewayConnection to APNS feedback socket.
     * @throws APNSGatewayConnectionException in case a connection could not be created.
     */
    @Nonnull
    public APNSGatewayConnection createFeedbackConnection() throws APNSGatewayConnectionException {
        return new APNSGatewayConnection(createConnection(feedbackHost, feedbackPort));
    }

    @Nonnull
    private SSLSocket createConnection(@Nonnull final String host, @Nonnull final Integer port) throws APNSGatewayConnectionException {
        assert host != null;
        assert port != null;

        final KeyStore keyStore = getKeyStore(p12KeyStorePath, p12KeyStorePassword);
        final SSLContext sslcontext = getSSLContext(keyStore, p12KeyStorePassword);

        try {
            final SSLSocketFactory factory = sslcontext.getSocketFactory();
            LOG.debug("createConnection: returning connection from SSLSocketFactory, host={}, port={}", host, port);
            return (SSLSocket) factory.createSocket(host, port);
        } catch (final IOException e) {
            LOG.warn("createConnection: could not connect, host={}, port={}", host, port);
            throw new APNSGatewayConnectionException("Could not connect to: " + host + ':' + port, e);
        }
    }

    /**
     * method returns a {@link KeyStore} with a security certificate that is required to establish a connection.
     *
     * @param pathToP12 The path to the P12 key store file.
     * @param password  The P12 key store password
     * @return KeyStore containing a security certificate.
     * @throws APNSGatewayConnectionException in case the KeyStore cannot be created.
     */
    @Nonnull
    private static KeyStore getKeyStore(@Nonnull final String pathToP12, @Nonnull final String password) throws APNSGatewayConnectionException {
        assert pathToP12 != null;
        assert password != null;

        final InputStream in = APNSConnectionFactory.class.getResourceAsStream(pathToP12);
        try {
            if (in == null) {
                LOG.error("getKeyStore: could not load key store, path={}", pathToP12);
                throw new APNSGatewayConnectionException("Resource not found on classpath: " + pathToP12);
            }
            final KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            keyStore.load(in, password.toCharArray());
            LOG.debug("getKeyStore: pathToP12={}, KeyStore.size={}", pathToP12, keyStore.size());
            return keyStore;
        } catch (final KeyStoreException e) {
            LOG.error("getKeyStore: key store exception, pathToP12={}", pathToP12);
            throw new APNSGatewayConnectionException("Key store exception", e);
        } catch (final CertificateException e) {
            LOG.error("getKeyStore: certificate exception, pathToP12={}", pathToP12);
            throw new APNSGatewayConnectionException("Certificate exception", e);
        } catch (final IOException e) {
            LOG.error("getKeyStore: possible incorrect password for key store or other IO problem, pathToP12={}",
                    pathToP12);
            throw new APNSGatewayConnectionException("Incorrect password for key store or other IO problem", e);
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("getKeyStore: key store does not support algorithm, pathToP12={}", pathToP12);
            throw new APNSGatewayConnectionException("Key store does not support algorithm", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                LOG.error("getKeyStore: IO exception, ", e);
            }
        }
    }

    /**
     * This method returns a {@link SSLContext} that should be used to get a {@link SSLSocketFactory} from.
     *
     * @param keyStore The key store with the security details.
     * @param password The password to the KeyStore.
     * @return SSLContext that can be used to obtain a connection.
     * @throws APNSGatewayConnectionException in case the SSLContext cannot be created.
     */
    private static SSLContext getSSLContext(@Nonnull final KeyStore keyStore, @Nullable final String password) throws APNSGatewayConnectionException {
        assert keyStore != null;

        final String algorithm = ALGORITHM;
        final SSLContext sslContext;

        try {
            final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            final char[] passwordCharArray = (password == null) ? null : password.toCharArray();
            kmfactory.init(keyStore, passwordCharArray);

            final KeyManager[] keymanagers = kmfactory.getKeyManagers();
            final TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            final KeyStore truststore = null;
            tmfactory.init(truststore);
            final TrustManager[] trustmanagers = tmfactory.getTrustManagers();

            sslContext = SSLContext.getInstance(algorithm);
            sslContext.init(keymanagers, trustmanagers, null);

            return sslContext;
        } catch (final KeyStoreException e) {
            LOG.error("getSSLContext: key store exception");
            throw new APNSGatewayConnectionException("Key store exception", e);
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("getSSLContext: no such algorithm, alogorith={}", algorithm);
            throw new APNSGatewayConnectionException("Unknown Algorithm", e);
        } catch (final KeyManagementException e) {
            LOG.error("getSSLContext: unable to load key");
            throw new APNSGatewayConnectionException("Unable to load key", e);
        } catch (final UnrecoverableKeyException e) {
            LOG.error("getSSLContext: incorrect password for key store");
            throw new APNSGatewayConnectionException("Incorrect password for key store", e);
        }
    }
}
