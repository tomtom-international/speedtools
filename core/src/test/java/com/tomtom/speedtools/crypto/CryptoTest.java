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

package com.tomtom.speedtools.crypto;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoTest {
    private static final Logger LOG = LoggerFactory.getLogger(CryptoTest.class);

    @Test
    public void testCrypto() {
        LOG.info("testCrypto");
        final String code = "123456";
        final String hash = Crypto.hash(code, "somesalt");
        LOG.info("hash(" + code + ", \"somesalt\") = {}", hash);
        Assert.assertEquals("2d7768d6bb9743b400b5525686f66c08c54fc982eb75f9b75e694c9f6d7ba7e2", hash);

        // A different salt produces a different hash.
        Assert.assertNotEquals(hash, Crypto.hash(code, "othersalt"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedUsesDefaultSalt() {
        LOG.info("testDeprecatedUsesDefaultSalt");
        final String code = "123456";

        // The deprecated single-argument overload hashes with the internal default salt.
        Assert.assertEquals("7bc95f678ab5d3301a20aea08305fd7895e82556b3d332715fe9a24464375a95", Crypto.hash(code));

        // An explicit empty salt is no longer substituted with the default salt.
        Assert.assertNotEquals(Crypto.hash(code), Crypto.hash(code, ""));
    }
}
