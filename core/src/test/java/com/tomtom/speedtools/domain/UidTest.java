/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UidTest {
    private static final Logger LOG = LoggerFactory.getLogger(UidTest.class);

    @Test
    public void testCreate() {
        LOG.info("testCreate");
        final int size = 100;
        final Map<String, Boolean> map = new HashMap<>(size);
        for (int i = 0; i < size; ++i) {
            final Uid<?> id = new Uid();
            LOG.info("UUID = {}", id);
            Assert.assertNull(map.put(id.toString(), true));
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testEquals() {
        LOG.info("testEquals");
        EqualsVerifier.forClass(Uid.class).
                withPrefabValues(UUID.class, new UUID(1L, 2L), new UUID(3L, 4L)).
                verify();
    }

    @Test
    public void testIsValid() {
        LOG.info("testIsValid");
        Assert.assertTrue(Uid.isValid("d32b6789-bfbb-4194-87f3-72ce34609902"));
        Assert.assertTrue(Uid.isValid("0-0-0-0-0"));
        Assert.assertFalse(Uid.isValid("0-0-0-0-0-0"));
        Assert.assertFalse(Uid.isValid("d32b6789bfbb419487f372ce34609902"));
        Assert.assertFalse(Uid.isValid(""));
        Assert.assertFalse(Uid.isValid(null));
    }

    @Test
    public void testFromString() {
        LOG.info("testFromString");
        final Uid<Object> a = Uid.fromString("d32b6789-bfbb-4194-87f3-72ce34609902");
        final String s = "d32b6789-bfbb-4194-87f3-72ce34609902";
        final Uid<Object> b = Uid.fromString(s);
        Assert.assertEquals(a, b);
    }

    @Test
    public void testToString() {
        LOG.info("testToString");
        final Uid<Object> x = new Uid<>();
        try {
            LOG.info("UUID = {}", x.toString());
        } catch (final Exception ignored) {
            Assert.fail();
        }
    }

    @Test
    public void testAs() {
        LOG.info("testAs");
        final Uid<Long> a = new Uid<>();
        final Uid<Integer> b = a.as(Integer.class);
        Assert.assertTrue(a.equals(b));
    }

    @Test
    public void testMatchesFromString() {
        LOG.info("testMatchesFromString");
        final Uid<String> a = Uid.fromString("0-0-0-0-0");
        Assert.assertTrue(a.matchesFromString("0000-0000-000-0-0"));
    }

    @Test
    public void testFromHexString() {
        LOG.info("testFromString");
        final String s = "d32b6789-bfbb-4194-87f3-72ce34609902";
        final Uid<Object> a = Uid.fromString(s);
        final Uid<Object> b = Uid.fromHexString(s.replaceAll("-", ""));
        Assert.assertEquals(a, b);
    }

    @Test
    public void testToHexString() {
        LOG.info("testToHexString");
        final Uid<Object> a = new Uid<>();
        Assert.assertEquals(a.toString().replaceAll("-", ""), a.toHexString());
    }

    @Test
    public void testToAppendedHexString() {
        LOG.info("testToAppendedHexString");
        final String s = "00000000-0000-0001-0000-000000000001";
        final Uid<Object> a = Uid.fromString(s);
        Assert.assertEquals(s.replaceAll("-", ""), a.toHexString());
    }

    @Test
    public void testNegativeHexId() {
        LOG.info("testNegativeHexId");
        final String s = "ffffffff-ffff-ffff-ffff-ffffffffffff";
        final Uid<Object> a = Uid.fromString(s);
        final Uid<Object> b = Uid.fromHexString(s.replaceAll("-", ""));
        Assert.assertEquals(a, b);
    }
}
