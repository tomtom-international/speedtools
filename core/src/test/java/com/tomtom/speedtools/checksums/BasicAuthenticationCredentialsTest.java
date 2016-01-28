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

package com.tomtom.speedtools.checksums;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthenticationCredentialsTest {
    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticationCredentialsTest.class);

    @Test
    public void testToBase64() {
        LOG.info("testToBase64");
        final String deviceId = "O12022A00028:none";
        final String expected = "Basic TzEyMDIyQTAwMDI4Om5vbmU=";

        Assert.assertEquals(expected, BasicAuthenticationCredentials.encodeBase64(deviceId));
    }
}
