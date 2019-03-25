/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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

import java.util.ArrayList;
import java.util.Collection;

public class StringUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(StringUtilsTest.class);

    @Test
    public void testEmptyToNull() {
        LOG.info("testEmptyToNull");

        Assert.assertNull(StringUtils.emptyToNull(null));
        Assert.assertNull(StringUtils.emptyToNull(""));
        final String input = "a";
        Assert.assertEquals(input, StringUtils.emptyToNull(input));
    }

    @Test
    public void testToCodePoints() {
        LOG.info("testToCodePoints");

        final String[] strings = {
                "",
                "a",
                "\uD801\uDC00",
                "aa",
                "\uD801\uDC00a",
                "a\uD801\uDC00",
                "\uD801\uDC00\uD801\uDC00",
                "aaa",
                "\uD801\uDC00aa",
                "a\uD801\uDC00a",
                "aa\uD801\uDC00",
                "\uD801\uDC00\uD801\uDC00a",
                "a\uD801\uDC00\uD801\uDC00",
                "\uD801\uDC00a\uD801\uDC00",
                "\uD801\uDC00\uD801\uDC00\uD801\uDC00"
        };

        final int[][] codePoints = {
                {},
                {0x61},
                {0x10400},
                {0x61, 0x61},
                {0x10400, 0x61},
                {0x61, 0x10400},
                {0x10400, 0x10400},
                {0x61, 0x61, 0x61},
                {0x10400, 0x61, 0x61},
                {0x61, 0x10400, 0x61},
                {0x61, 0x61, 0x10400},
                {0x10400, 0x10400, 0x61},
                {0x61, 0x10400, 0x10400},
                {0x10400, 0x61, 0x10400},
                {0x10400, 0x10400, 0x10400}
        };

        assert strings.length == codePoints.length;

        for (int i = 0; i < strings.length; i++) {
            LOG.debug("testToCodePoints: strings[{}]=\"{}\", expected={}", i, strings[i], codePoints[i]);
            Assert.assertArrayEquals(codePoints[i], StringUtils.toCodePoints(strings[i]));
        }
    }

    @Test
    public void testMkStringList() {
        LOG.info("testMkStringList");

        final Collection<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        final String result = StringUtils.mkString("|", list);
        Assert.assertEquals("1|2|3", result);
    }

    @Test
    public void testMkStringArray() {
        LOG.info("testMkStringArray");

        final String result = StringUtils.mkString(", ", "joe", "bob", "alice");
        Assert.assertEquals("joe, bob, alice", result);
    }

    @Test
    public void testMkStringArrayEmpty() {
        LOG.info("testMkStringArrayEmpty");

        String result = StringUtils.mkString(", ", "", "2", "3");
        Assert.assertEquals("2, 3", result);

        result = StringUtils.mkString(", ", "1", "", "3");
        Assert.assertEquals("1, 3", result);

        result = StringUtils.mkString(", ", "1", "2", "");
        Assert.assertEquals("1, 2", result);

        result = StringUtils.mkString(", ", null, "2", "3");
        Assert.assertEquals("2, 3", result);

        result = StringUtils.mkString(", ", "1", null, "3");
        Assert.assertEquals("1, 3", result);

        result = StringUtils.mkString(", ", "1", "2", null);
        Assert.assertEquals("1, 2", result);
    }

    @Test
    public void testMkStringArrayTwoElements() {
        LOG.info("testMkStringArrayTwoElements");

        String result = StringUtils.mkString(" ", "first", "last");
        Assert.assertEquals("first last", result);

        result = StringUtils.mkRevString(" ", "first", "last");
        Assert.assertEquals("last first", result);

        result = StringUtils.mkString(" ", "first");
        Assert.assertEquals("first", result);

        result = StringUtils.mkString(" ", "");
        Assert.assertEquals("", result);
    }

    @Test
    public void testEncodeToJsonString() {
        LOG.info("testEncodeToJsonString");

        String json = "\"\"";       // JSON string.
        String java = "";           // Java string.
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\"";
        java = "a";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"ab\"";
        java = "ab";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\\"a\"";
        java = "\"a";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\\\"\"";
        java = "a\"";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\\\"b\"";
        java = "a\"b";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\\\\"";
        java = "\\";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\\\a\"";
        java = "\\a";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\\\\\"";
        java = "a\\";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\\\\b\"";
        java = "a\\b";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\\\\\\\a\"";
        java = "\\\\a";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\\\\\\\\\"";
        java = "a\\\\";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"a\\\\\\\\b\"";
        java = "a\\\\b";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\\"\"";
        java = "\"";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\b\"";
        java = "\b";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\t\"";
        java = "\t";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\n\"";
        java = "\n";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\f\"";
        java = "\f";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        json = "\"\\r\"";
        java = "\r";
        Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
        Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));

        final String controlChars = "\b\t\n\f\r";
        for (int i = 0; i < 32; ++i) {
            //noinspection NumericCastThatLosesPrecision
            final char ch = (char) i;
            final String code = ("000" + Integer.toHexString(i));
            if (controlChars.indexOf(ch) == -1) {
                json = "\"\\u" + code.substring(code.length() - 4) + '"';
                java = String.valueOf(ch);
                Assert.assertEquals(json, StringUtils.encodeToJsonString(java));
                Assert.assertEquals(java, StringUtils.decodeFromJsonString(json));
            }
        }

        // Failing decoder test.
        Assert.assertNull(StringUtils.decodeFromJsonString(""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"x"));
        Assert.assertNull(StringUtils.decodeFromJsonString("x\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"\\\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"\\u\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"\\u0\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"\\u00\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"\\u000\""));
        Assert.assertNull(StringUtils.decodeFromJsonString("\"\\u000X\""));
    }
}
