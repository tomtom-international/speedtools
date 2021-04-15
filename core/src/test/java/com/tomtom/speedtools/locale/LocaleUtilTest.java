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

package com.tomtom.speedtools.locale;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class LocaleUtilTest {

    @Test
    public void testValidLocales() {
        assertEquals(new Locale("nl"), LocaleUtil.parse("nl"));
        assertEquals(new Locale("nl", "NL"), LocaleUtil.parse("nl_nl"));
        assertEquals(new Locale("", "NL"), LocaleUtil.parse("_NL"));
        assertEquals(new Locale("nl", "NL"), LocaleUtil.parse("nl_NL"));
        assertEquals(new Locale("nl", "", "MAC"), LocaleUtil.parse("nl__MAC"));
        assertEquals(new Locale("nl", "NL", "MAC"), LocaleUtil.parse("nl_NL_MAC"));
        assertEquals(new Locale("", "NL", "MAC"), LocaleUtil.parse("_NL_MAC"));
        assertEquals(new Locale("es", "ES", "Traditional_WIN"), LocaleUtil.parse("es_ES_Traditional_WIN"));

        // Maybe not a very useful case, but since variant can contain '_' the following is allowed.
        assertEquals(new Locale("nl", "", "_"), LocaleUtil.parse("nl___"));
    }

    @Test
    public void testInvalidLocales() {
        try {
            LocaleUtil.parse("");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
        try {
            LocaleUtil.parse("__MAC");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
        try {
            LocaleUtil.parse("nl_G");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
        try {
            LocaleUtil.parse("nl_GBR_MAC");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
        try {
            LocaleUtil.parse("nl_GBR_MA");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
        try {
            LocaleUtil.parse("ac_GB");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
        try {
            LocaleUtil.parse("nl_UK");
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ignored) {
            // Nothing to do.
        }
    }
}
