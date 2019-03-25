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

import java.util.Locale;

public class AddressUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(AddressUtilsTest.class);

    @Test
    public void testIsValidCountryISO2() {
        LOG.info("testIsValidCountryISO2");

        Assert.assertTrue(AddressUtils.isValidCountryISO2("NL"));
        Assert.assertTrue(AddressUtils.isValidCountryISO2("GB"));
        Assert.assertTrue(AddressUtils.isValidCountryISO2("US"));

        Assert.assertFalse(AddressUtils.isValidCountryISO2("UK"));
        Assert.assertFalse(AddressUtils.isValidCountryISO2("UW"));
        Assert.assertFalse(AddressUtils.isValidCountryISO2("AA"));
    }

    @Test
    public void testGetDisplayCountry() {
        LOG.info("testGetDisplayCountry");

        Assert.assertEquals("Netherlands", AddressUtils.getDisplayCountry("NL"));
        Assert.assertEquals("Germany", AddressUtils.getDisplayCountry("DE"));
        Assert.assertEquals("United Kingdom", AddressUtils.getDisplayCountry("GB"));
        Assert.assertEquals("", AddressUtils.getDisplayCountry("UK"));

        Assert.assertEquals("Nederland", AddressUtils.getDisplayCountry("NL", new Locale("NL")));
        Assert.assertEquals("Deutschland", AddressUtils.getDisplayCountry("DE", new Locale("DE")));
        Assert.assertEquals("United Kingdom", AddressUtils.getDisplayCountry("GB", new Locale("GB")));
        Assert.assertEquals("", AddressUtils.getDisplayCountry("UK", new Locale("UK")));

    }

    @Test
    public void testFormatStreetWithHouseNumber() {
        LOG.info("testFormatStreetWithHouseNumber");

        final Locale nl = new Locale("nl", "NL");
        final Locale e1 = new Locale("en", "UK");
        final Locale e2 = Locale.ENGLISH;

        Assert.assertEquals("s", AddressUtils.formatStreetWithHouseNumber(nl, "s", null, null));
        Assert.assertEquals("s", AddressUtils.formatStreetWithHouseNumber(e1, "s", null, null));
        Assert.assertEquals("s", AddressUtils.formatStreetWithHouseNumber(e2, "s", null, null));

        Assert.assertEquals("s 1", AddressUtils.formatStreetWithHouseNumber(nl, "s", 1, null));
        Assert.assertEquals("1 s", AddressUtils.formatStreetWithHouseNumber(e1, "s", 1, null));
        Assert.assertEquals("1 s", AddressUtils.formatStreetWithHouseNumber(e2, "s", 1, null));

        Assert.assertEquals("s a", AddressUtils.formatStreetWithHouseNumber(nl, "s", null, "a"));
        Assert.assertEquals("a s", AddressUtils.formatStreetWithHouseNumber(e1, "s", null, "a"));
        Assert.assertEquals("a s", AddressUtils.formatStreetWithHouseNumber(e2, "s", null, "a"));

        Assert.assertEquals("s 1-a", AddressUtils.formatStreetWithHouseNumber(nl, "s", 1, "a"));
        Assert.assertEquals("1-a s", AddressUtils.formatStreetWithHouseNumber(e1, "s", 1, "a"));
        Assert.assertEquals("1-a s", AddressUtils.formatStreetWithHouseNumber(e2, "s", 1, "a"));
    }

    @Test
    public void testFormatPostalCodeAndCity() {
        LOG.info("testFormatPostalCodeAndCity");

        final Locale nl = new Locale("nl", "NL");
        final Locale e1 = new Locale("en", "UK");
        final Locale e2 = Locale.ENGLISH;

        Assert.assertEquals("1  2", AddressUtils.formatPostcalCodeAndCity(nl, "1", "2"));
        Assert.assertEquals("2, 1", AddressUtils.formatPostcalCodeAndCity(e1, "1", "2"));
        Assert.assertEquals("2, 1", AddressUtils.formatPostcalCodeAndCity(e2, "1", "2"));
    }
}
