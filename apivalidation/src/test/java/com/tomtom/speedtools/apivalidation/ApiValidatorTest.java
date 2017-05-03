/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.apivalidation;

import com.tomtom.speedtools.apivalidation.exceptions.ApiBadRequestException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiValidatorTest {
    private static final Logger LOG = LoggerFactory.getLogger(ApiValidatorTest.class);

    private ApiValidator validator;

    @Before
    public void setUp() {
        validator = new ApiValidator();
    }

    @Test
    public void testCheckNullOK() {
        LOG.info("testCheckNullOk");

        validator.checkNull(false, "name", "some");
        validator.checkNull(true, "name", null);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckNullFail() {
        LOG.info("testCheckNotNullFail");

        validator.checkNull(true, "name", "some");
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(1, e.getErrors().size());
        }
    }

    @Test
    public void testCheckTrueFalseOK() {
        LOG.info("testCheckTrueFalseOk");

        validator.checkTrue("name", true);
        validator.checkFalse("name", false);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckTrueFalseFail() {
        LOG.info("testCheckTrueFalseFail");

        validator.checkTrue("name", false);
        validator.checkFalse("name", true);
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(2, e.getErrors().size());
        }
    }

    @Test
    public void testCheckIntegerOk() {
        LOG.info("testCheckIntegerOk");

        validator.checkInteger(true, "integer", 1, 1, 3);
        validator.checkInteger(true, "integer", 2, 1, 3);
        validator.checkInteger(true, "integer", 3, 1, 3);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckIntegerFail() {
        LOG.info("testCheckIntegerFail");

        validator.checkInteger(true, "integer", 0, 1, 2);
        validator.checkInteger(true, "integer", 3, 1, 2);

        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(2, e.getErrors().size());
        }
    }

    @Test
    public void testCheckDoubleOk() {
        LOG.info("testCheckDoubleOk");

        validator.checkDouble(true, "double", 1.0, 1, 3, false);
        validator.checkDouble(true, "double", 2.0, 1, 3, false);
        validator.checkDouble(true, "double", 3.0, 1, 3, false);
        validator.checkDouble(true, "double", Double.POSITIVE_INFINITY, 1, 3, true);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckDoubleFail() {
        LOG.info("testCheckDoubleFail");

        validator.checkDouble(true, "Double", 0.0, 1, 2, false);
        validator.checkDouble(true, "Double", 3.0, 1, 2, false);
        validator.checkDouble(true, "double", Double.POSITIVE_INFINITY, 1, 3, false);
        validator.checkDouble(true, "double", Double.NEGATIVE_INFINITY, 1, 3, false);
        validator.checkDouble(true, "double", Double.NEGATIVE_INFINITY, 1, 3, true);
        validator.checkDouble(true, "double", 0.0, 1, 3, true);
        validator.checkDouble(true, "double", 4.0, 1, 3, true);

        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(6, e.getErrors().size());
        }
    }

    @Test
    public void testCheckObjectOk() {
        LOG.info("testCheckObjectOk");
        Assert.assertTrue(true);

        validator.checkNotNull(false, "object", null);
        validator.checkNotNull(true, "object", "value");
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckObjectFail() {
        LOG.info("testCheckObjectFail");
        Assert.assertTrue(true);

        validator.checkNotNull(true, "object", null);
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(1, e.getErrors().size());
        }
    }

    @Test
    public void testCheckStringOk() {
        LOG.info("testCheckStringOk");
        Assert.assertTrue(true);

        validator.checkString(false, "string", null, 1, 2);
        validator.checkString(true, "string", "1", 1, 2);
        validator.checkString(true, "string", "12", 1, 2);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckStringFail() {
        LOG.info("testCheckStringFail");
        Assert.assertTrue(true);

        validator.checkString(true, "string", null, 1, 2);
        validator.checkString(true, "string", "", 1, 2);
        validator.checkString(true, "string", "123", 1, 2);
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(3, e.getErrors().size());
        }
    }

    @Test
    public void testCheckLocaleOk() {
        LOG.info("testCheckLocaleOk");
        Assert.assertTrue(true);

        validator.checkLocale(false, "locale", null);
        validator.checkLocale(true, "locale", "nl_NL");
        validator.checkLocale(true, "locale", "nl");
        validator.checkLocale(true, "locale", "en_GB");
        validator.checkLocale(true, "locale", "en");
        validator.checkLocale(true, "locale", "NL_nl");
        validator.checkLocale(true, "locale", "NL");
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckLocaleFail() {
        LOG.info("testCheckLocaleFail");
        Assert.assertTrue(true);

        validator.checkLocale(true, "locale", "nl_NLx");
        validator.checkLocale(true, "locale", "nlx");
        validator.checkLocale(true, "locale", "uk_EN");
        validator.checkLocale(true, "locale", "gb");
        validator.checkLocale(true, "locale", "GB");
        validator.checkLocale(true, "locale", "en_UK");
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(6, e.getErrors().size());
        }
    }

    @Test
    public void testColorOk() {
        LOG.info("testColorOk");

        validator.checkColor(true, "color", "#1");
        validator.checkColor(true, "color", "#010203");
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testColorFail() {
        LOG.info("testColorFail");

        validator.checkColor(true, "color", "");
        validator.checkColor(true, "color", "#");
        validator.checkColor(true, "color", "010203");
        validator.checkColor(true, "color", "0010203");
        validator.checkColor(true, "color", "#0102034");
        validator.checkColor(true, "color", "#x10203");
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(6, e.getErrors().size());
        }
    }

    @Test
    public void testCheckDateOk() {
        LOG.info("testCheckDateOk");

        final DateTime min = new DateTime(2012, 1, 1, 1, 2);
        final DateTime max = new DateTime(2012, 1, 1, 1, 3);
        validator.checkDate(false, "date", (DateTime) null, min, max);
        validator.checkDate(true, "date", new DateTime(2012, 1, 1, 1, 2), min, max);
        validator.checkDate(true, "date", new DateTime(2012, 1, 1, 1, 3), min, max);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCheckDateFail() {
        LOG.info("testCheckDateFail");

        final DateTime min = new DateTime(2012, 1, 1, 1, 2);
        final DateTime max = new DateTime(2012, 1, 1, 1, 3);
        validator.checkDate(true, "date", (DateTime) null, min, max);
        validator.checkDate(true, "date", new DateTime(2012, 1, 1, 1, 1), min, max);
        validator.checkDate(true, "date", new DateTime(2012, 1, 1, 1, 4), min, max);
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(3, e.getErrors().size());
        }
    }

    @Test
    public void testUidCheckOk() {
        LOG.info("testUidCheckOk");

        final String uid = "0-0-0-0-0";

        validator.checkUid(true, "uid", uid);
        validator.checkUid(false, "uid", null);
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testUidCheckFail() {
        LOG.info("testUidCheckFail");

        final String uid = "invalid-uid";

        validator.checkUid(true, "uid", uid);
        validator.checkUid(true, "uid", null);
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(2, e.getErrors().size());
        }
    }

    @Test
    public void testCurrencyISO3Ok() {
        LOG.info("testCurrencyISO3Ok");

        validator.checkCurrencyISO3(true, "euro", "EUR");
        validator.done();
        Assert.assertTrue(true);
    }

    @Test
    public void testCurrencyISO3Fail() {
        LOG.info("testCurrencyISO3Fail");

        validator.checkCurrencyISO3(true, "none", "NONE");
        validator.checkCurrencyISO3(true, "xyz", "XYZ");
        try {
            validator.done();
            Assert.fail();
        } catch (final ApiBadRequestException e) {
            Assert.assertEquals(3, e.getErrors().size());
        }
    }
}
