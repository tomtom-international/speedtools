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

package com.tomtom.speedtools.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tomtom.speedtools.utils.ByteUtils.fromBytesToInt;
import static com.tomtom.speedtools.utils.ByteUtils.intToByteArray;

public class ByteUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(ByteUtilsTest.class);

    @Test
    public void testIntToByteArrayAndBack() {
        LOG.info("testIntToByteArrayAndBack");

        final int input1 = 100000;
        final int input2 = -100000;
        final int input3 = Integer.MAX_VALUE;
        final int input4 = Integer.MIN_VALUE;
        final int input5 = 0;
        final int input6 = 123456789;

        Assert.assertEquals(input1, convert(input1));
        Assert.assertEquals(input2, convert(input2));
        Assert.assertEquals(input3, convert(input3));
        Assert.assertEquals(input4, convert(input4));
        Assert.assertEquals(input5, convert(input5));
        Assert.assertEquals(input6, convert(input6));
    }

    private static int convert(final int input) {
        return fromBytesToInt(intToByteArray(input));
    }
}
