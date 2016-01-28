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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA1HashTest {
    private static final Logger LOG = LoggerFactory.getLogger(SHA1HashTest.class);


    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(SHA1Hash.class).
                verify();
        // Do not add: allFieldsShouldBeUsed()
    }

    @Test
    public void testCalculateChecksum() {
        LOG.info("testCalculateChecksum");

        String t = "http://first.com/file1(22)http://second.com/file2(23)http://first.com/file1(22)";
        SHA1Hash s = SHA1Hash.hash(t);
        LOG.info("Checksum(\"" + t + "\") = " + s);
        Assert.assertEquals("ab80b970c0848d1819e3cfe53a4ee70f62c5ad2b", s.toString());

        t = "";
        s = SHA1Hash.hash(t);
        LOG.info("Checksum(\"" + t + "\") = " + s);
        Assert.assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", s.toString());

        t = " ";
        s = SHA1Hash.hash(t);
        LOG.info("Checksum(\"" + t + "\") = " + s);
        Assert.assertEquals("b858cb282617fb0956d960215c8e84d1ccf909c6", s.toString());

        t = "abc";
        s = SHA1Hash.hash(t);
        LOG.info("Checksum(\"" + t + "\") = " + s);
        Assert.assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", s.toString());

        t = "the quick brown fox jumps over the lazy dog";
        s = SHA1Hash.hash(t);
        LOG.info("Checksum(\"" + t + "\") = " + s);
        Assert.assertEquals("16312751ef9307c3fd1afbcb993cdc80464ba0f1", s.toString());
    }

    @Test
    public void testSaltedHash() {
        LOG.info("testSaltedHash");

        final String token = "123456";
        final SHA1Hash hash = SHA1Hash.saltedHash(token);

        Assert.assertNotNull(hash);

        // Check that hashed token is not empty, and it not the same as the plaintext token.
        final String hashedToken = hash.toString();
        Assert.assertNotNull(hashedToken);
        Assert.assertFalse(hashedToken.isEmpty());
        Assert.assertFalse(token.equals(hashedToken));

        // Check that actual salt was generated.
        final String salt = hash.getSalt();
        Assert.assertNotNull(salt);
        Assert.assertFalse(salt.isEmpty());
    }


    @Test
    public void testSaltedHashWithSalt() {
        LOG.info("testSaltedHashWithSalt");

        final String token = "123456";
        final String referenceSalt = "7890";
        final SHA1Hash hash = SHA1Hash.saltedHash(token, referenceSalt);

        Assert.assertNotNull(hash);

        // Check that hashed token is not empty, and it not the same as the plaintext token.
        final String hashedToken = hash.toString();
        Assert.assertNotNull(hashedToken);
        Assert.assertFalse(hashedToken.isEmpty());
        Assert.assertFalse(token.equals(hashedToken));

        // Check that the salt used during hashing is the salt that was passed in.
        final String salt = hash.getSalt();
        Assert.assertEquals(referenceSalt, salt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaltedHashWithInvalidSalt() {
        LOG.info("testSaltedHashWithInvalidSalt");

        final String token = "123456";
        final String referenceSalt = "78G0";
        SHA1Hash.saltedHash(token, referenceSalt);
    }
}
