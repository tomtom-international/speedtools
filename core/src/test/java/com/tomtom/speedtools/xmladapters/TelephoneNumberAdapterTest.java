/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.xmladapters;


import com.google.i18n.phonenumbers.NumberParseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelephoneNumberAdapterTest {

    private static final Logger LOG = LoggerFactory.getLogger(TelephoneNumberAdapterTest.class);

    @Test
    public void testMarshal() throws NumberParseException {
        LOG.info("Test: testMarshal");

        // Valid Dutch Numbers.
        marshal("+31 (0)6-12345678", "+31612345678");
        marshal("+31 20 1234567", "+31201234567");
        marshal("+31 020-1234567", "+31201234567");
        marshal("+31 (0)20 1234567", "+31201234567");
        marshal("+31 020 123.45.67", "+31201234567");
        marshal("+31 (0)20 123.45.67", "+31201234567");

        // Valid UK numbers.
        marshal("+44 070 1234 5678", "+447012345678");
        marshal("+44 (0)70 1234 5678", "+447012345678");
        marshal("+44 20 1234 5678", "+442012345678"); // London.
        marshal("+44 01204 12345", "+44120412345"); // Bolton.
        marshal("+44 016977 1234", "+44169771234"); // Brampton.
        marshal("+44 (0)169771234", "+44169771234"); // Brampton.
        marshal("+44 131 123 4567", "+441311234567"); // Edinburgh.
        marshal("+44 131 123 4567", "+441311234567"); // Edinburgh.

        // Valid French Numbers.
        marshal("+33 1 12345678", "+33112345678"); // Paris
        marshal("+33 6 12345678", "+33612345678"); // Mobile
        marshal("+33 7 12345678", "+33712345678"); // Mobile

        // US Numbers.
        marshal("+1 (234) 235 5678", "+12342355678");
        marshal("+12342355678", "+12342355678");

        // South African Numbers.
        marshal("+27 021 1234567", "+27211234567");
        marshal("+27(021)1234567", "+27211234567");

        // Brazilian Numbers.
        marshal("+55 2345 6789", "+5523456789");//Fixed.
        marshal("+55 7890 7890", "+5578907890");//Mobile.

        Assert.assertTrue(true);
    }

    private static void marshal(final String valueToMarshal, final String expectedValue) throws NumberParseException {
        final TelephoneNumberAdapter adapter = new TelephoneNumberAdapter();
        final String marshal = adapter.marshal(valueToMarshal);
        Assert.assertEquals(expectedValue, marshal);
    }
}
