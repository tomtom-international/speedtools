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

package com.tomtom.speedtools.checksums;

import com.tomtom.speedtools.utils.MathUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("ErrorNotRethrown")
public class HexStringTest {
    private static final Logger LOG = LoggerFactory.getLogger(HexStringTest.class);

    @Test
    public void testHexStringString() {
        LOG.info("testHexStringString");
        Assert.assertEquals("41", HexString.getHexString("A"));
        Assert.assertEquals("4142", HexString.getHexString("AB"));
    }

    @Test
    public void testHexString() {
        LOG.info("testHexString");
        Assert.assertEquals("00", HexString.getHexString(new byte[]{(byte) 0}));
        Assert.assertEquals("01", HexString.getHexString(new byte[]{(byte) 1}));
        Assert.assertEquals("02", HexString.getHexString(new byte[]{(byte) 2}));
        Assert.assertEquals("03", HexString.getHexString(new byte[]{(byte) 3}));
        Assert.assertEquals("04", HexString.getHexString(new byte[]{(byte) 4}));
        Assert.assertEquals("05", HexString.getHexString(new byte[]{(byte) 5}));
        Assert.assertEquals("06", HexString.getHexString(new byte[]{(byte) 6}));
        Assert.assertEquals("07", HexString.getHexString(new byte[]{(byte) 7}));
        Assert.assertEquals("08", HexString.getHexString(new byte[]{(byte) 8}));
        Assert.assertEquals("09", HexString.getHexString(new byte[]{(byte) 9}));
        Assert.assertEquals("0a", HexString.getHexString(new byte[]{(byte) 10}));
        Assert.assertEquals("0b", HexString.getHexString(new byte[]{(byte) 11}));
        Assert.assertEquals("0c", HexString.getHexString(new byte[]{(byte) 12}));
        Assert.assertEquals("0d", HexString.getHexString(new byte[]{(byte) 13}));
        Assert.assertEquals("0e", HexString.getHexString(new byte[]{(byte) 14}));
        Assert.assertEquals("0f", HexString.getHexString(new byte[]{(byte) 15}));
        Assert.assertEquals("10", HexString.getHexString(new byte[]{(byte) 16}));
        Assert.assertEquals("0000", HexString.getHexString(new byte[]{(byte) 0, (byte) 0}));
        Assert.assertEquals("0001", HexString.getHexString(new byte[]{(byte) 0, (byte) 1}));
        Assert.assertEquals("0002", HexString.getHexString(new byte[]{(byte) 0, (byte) 2}));
        Assert.assertEquals("0003", HexString.getHexString(new byte[]{(byte) 0, (byte) 3}));
        Assert.assertEquals("0004", HexString.getHexString(new byte[]{(byte) 0, (byte) 4}));
        Assert.assertEquals("0005", HexString.getHexString(new byte[]{(byte) 0, (byte) 5}));
        Assert.assertEquals("0006", HexString.getHexString(new byte[]{(byte) 0, (byte) 6}));
        Assert.assertEquals("0007", HexString.getHexString(new byte[]{(byte) 0, (byte) 7}));
        Assert.assertEquals("0008", HexString.getHexString(new byte[]{(byte) 0, (byte) 8}));
        Assert.assertEquals("0009", HexString.getHexString(new byte[]{(byte) 0, (byte) 9}));
        Assert.assertEquals("000a", HexString.getHexString(new byte[]{(byte) 0, (byte) 10}));
        Assert.assertEquals("000b", HexString.getHexString(new byte[]{(byte) 0, (byte) 11}));
        Assert.assertEquals("000c", HexString.getHexString(new byte[]{(byte) 0, (byte) 12}));
        Assert.assertEquals("000d", HexString.getHexString(new byte[]{(byte) 0, (byte) 13}));
        Assert.assertEquals("000e", HexString.getHexString(new byte[]{(byte) 0, (byte) 14}));
        Assert.assertEquals("000f", HexString.getHexString(new byte[]{(byte) 0, (byte) 15}));
        Assert.assertEquals("0010", HexString.getHexString(new byte[]{(byte) 0, (byte) 16}));
        Assert.assertEquals("41", HexString.getHexString(new byte[]{(byte) 65}));
        Assert.assertEquals("4142", HexString.getHexString(new byte[]{(byte) 65, (byte) 66}));
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Test
    public void testHexByteArray() {
        LOG.info("testHexByteArray");
        Assert.assertArrayEquals(new byte[]{(byte) 0}, HexString.getHexByteArray("0"));
        Assert.assertArrayEquals(new byte[]{(byte) 1}, HexString.getHexByteArray("1"));
        Assert.assertArrayEquals(new byte[]{(byte) 2}, HexString.getHexByteArray("2"));
        Assert.assertArrayEquals(new byte[]{(byte) 3}, HexString.getHexByteArray("3"));
        Assert.assertArrayEquals(new byte[]{(byte) 4}, HexString.getHexByteArray("4"));
        Assert.assertArrayEquals(new byte[]{(byte) 5}, HexString.getHexByteArray("5"));
        Assert.assertArrayEquals(new byte[]{(byte) 6}, HexString.getHexByteArray("6"));
        Assert.assertArrayEquals(new byte[]{(byte) 7}, HexString.getHexByteArray("7"));
        Assert.assertArrayEquals(new byte[]{(byte) 8}, HexString.getHexByteArray("8"));
        Assert.assertArrayEquals(new byte[]{(byte) 9}, HexString.getHexByteArray("9"));
        Assert.assertArrayEquals(new byte[]{(byte) 10}, HexString.getHexByteArray("a"));
        Assert.assertArrayEquals(new byte[]{(byte) 11}, HexString.getHexByteArray("b"));
        Assert.assertArrayEquals(new byte[]{(byte) 12}, HexString.getHexByteArray("c"));
        Assert.assertArrayEquals(new byte[]{(byte) 13}, HexString.getHexByteArray("d"));
        Assert.assertArrayEquals(new byte[]{(byte) 14}, HexString.getHexByteArray("e"));
        Assert.assertArrayEquals(new byte[]{(byte) 15}, HexString.getHexByteArray("f"));
        Assert.assertArrayEquals(new byte[]{(byte) 10}, HexString.getHexByteArray("0A"));
        Assert.assertArrayEquals(new byte[]{(byte) 10}, HexString.getHexByteArray("0a"));
        Assert.assertArrayEquals(new byte[]{(byte) 0}, HexString.getHexByteArray("00"));
        Assert.assertArrayEquals(new byte[]{(byte) 1}, HexString.getHexByteArray("01"));
        Assert.assertArrayEquals(new byte[]{(byte) 2}, HexString.getHexByteArray("02"));
        Assert.assertArrayEquals(new byte[]{(byte) 3}, HexString.getHexByteArray("03"));
        Assert.assertArrayEquals(new byte[]{(byte) 4}, HexString.getHexByteArray("04"));
        Assert.assertArrayEquals(new byte[]{(byte) 5}, HexString.getHexByteArray("05"));
        Assert.assertArrayEquals(new byte[]{(byte) 6}, HexString.getHexByteArray("06"));
        Assert.assertArrayEquals(new byte[]{(byte) 7}, HexString.getHexByteArray("07"));
        Assert.assertArrayEquals(new byte[]{(byte) 8}, HexString.getHexByteArray("08"));
        Assert.assertArrayEquals(new byte[]{(byte) 9}, HexString.getHexByteArray("09"));
        Assert.assertArrayEquals(new byte[]{(byte) 10}, HexString.getHexByteArray("0a"));
        Assert.assertArrayEquals(new byte[]{(byte) 11}, HexString.getHexByteArray("0b"));
        Assert.assertArrayEquals(new byte[]{(byte) 12}, HexString.getHexByteArray("0c"));
        Assert.assertArrayEquals(new byte[]{(byte) 13}, HexString.getHexByteArray("0d"));
        Assert.assertArrayEquals(new byte[]{(byte) 14}, HexString.getHexByteArray("0e"));
        Assert.assertArrayEquals(new byte[]{(byte) 15}, HexString.getHexByteArray("0f"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 0}, HexString.getHexByteArray("000"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 1}, HexString.getHexByteArray("001"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 2}, HexString.getHexByteArray("002"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 3}, HexString.getHexByteArray("003"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 4}, HexString.getHexByteArray("004"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 5}, HexString.getHexByteArray("005"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 6}, HexString.getHexByteArray("006"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 7}, HexString.getHexByteArray("007"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 8}, HexString.getHexByteArray("008"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 9}, HexString.getHexByteArray("009"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 10}, HexString.getHexByteArray("00a"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 11}, HexString.getHexByteArray("00b"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 12}, HexString.getHexByteArray("00c"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 13}, HexString.getHexByteArray("00d"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 14}, HexString.getHexByteArray("00e"));
        Assert.assertArrayEquals(new byte[]{(byte) 0, (byte) 15}, HexString.getHexByteArray("00f"));
        Assert.assertArrayEquals(new byte[]{(byte) 10}, HexString.getHexByteArray("0A"));
        Assert.assertArrayEquals(new byte[]{(byte) 10}, HexString.getHexByteArray("a"));
        Assert.assertArrayEquals(new byte[]{(byte) 255}, HexString.getHexByteArray("ff"));
        Assert.assertArrayEquals(new byte[]{(byte) 65}, HexString.getHexByteArray("41"));
        Assert.assertArrayEquals(new byte[]{(byte) 65, (byte) 66}, HexString.getHexByteArray("4142"));
    }

    @Test
    public void testHexStringByteArray() {
        LOG.info("testHexStringByteArray");

        MathUtils.setRandomSeed(1);
        for (int len = 1; len < 1000; ++len) {

            // Create a byte array of certain length.
            final byte[] bytes = new byte[len];
            for (int i = 0; i < len; ++i) {
                //noinspection NumericCastThatLosesPrecision
                bytes[i] = (byte) MathUtils.random(0, 255);
            }

            // Create the hex string.
            final String hexString = HexString.getHexString(bytes);

            // Given the hex string, convert it back to a hex byte array.
            final byte[] hexByteArray = HexString.getHexByteArray(hexString);

            Assert.assertArrayEquals("hexString='" + hexString + "' (length=" + hexString.length() +
                    "), hexByteArray.length=" + hexByteArray.length, bytes, hexByteArray);
        }
    }

    @Test
    public void testEmptyArray() {
        LOG.info("testEmptyArray");
        try {
            final byte[] emptyArray = new byte[0];
            HexString.getHexString(emptyArray);
        } catch (final AssertionError ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @Test
    public void testEmptyString() {
        LOG.info("testEmptyString");
        try {
            final String nullString = "";
            HexString.getHexString(nullString);
        } catch (final AssertionError ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @Test
    public void testNullBytesArray() {
        LOG.info("testNullBytesArray");
        try {
            final byte[] nullArray = null;
            //noinspection ConstantConditions
            HexString.getHexString(nullArray);
        } catch (final AssertionError | IllegalArgumentException ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @Test
    public void testNullString() {
        LOG.info("testNullString");
        try {
            final String nullString = null;
            //noinspection ConstantConditions
            HexString.getHexString(nullString);
        } catch (final AssertionError | IllegalArgumentException ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }
}
