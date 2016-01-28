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


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APNSConnectionFactoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(APNSConnectorTest.class);
    private APNSConnectionFactory apnsConnectionFactory;

    @Ignore
    @Test(expected = APNSGatewayConnectionException.class)
    public void testCreateConnectionInvalidP12Path() throws APNSGatewayConnectionException {
        LOG.info("testCreateConnectionInvalidP12Path");

        apnsConnectionFactory = new APNSConnectionFactory("gateway.sandbox.push.apple.com", 2195,
                "feedback.sandbox.push.apple.com", 2196,
                "/com/tomtom/speedtools/connectors/push/implementation/apns/place-your-certificate-here-for-apns-sandbox.p12-XYZ",
                "<password>");

        apnsConnectionFactory.createPushConnection();
    }

    @Ignore
    @Test(expected = APNSGatewayConnectionException.class)
    public void testCreateConnectionInvalidP12Password() throws APNSGatewayConnectionException {
        LOG.info("testCreateConnectionInvalidP12Password");

        apnsConnectionFactory = new APNSConnectionFactory("gateway.sandbox.push.apple.com", 2195,
                "feedback.sandbox.push.apple.com", 2196,
                "/com/tomtom/speedtools/push/implementation/apns/place-your-certificate-here-for-apns-sandbox.p12",
                "<wrong-password>");

        apnsConnectionFactory.createPushConnection();
    }

    @Ignore
    @Test
    public void testCreateConnection() throws APNSGatewayConnectionException {
        LOG.info("testCreateConnection");

        apnsConnectionFactory = new APNSConnectionFactory("gateway.sandbox.push.apple.com", 2195,
                "feedback.sandbox.push.apple.com", 2196,
                "/com/tomtom/speedtools/push/implementation/apns/place-your-certificate-here-for-apns-sandbox.p12",
                "<password>");

        final APNSGatewayConnection connection = apnsConnectionFactory.createPushConnection();
        Assert.assertNotNull(connection);
        connection.close();
    }
}
