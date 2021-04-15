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

package com.tomtom.speedtools.urls;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UrlParameterBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(UrlParameterBuilderTest.class);

    @Test
    public void testAddAfterClear() {
        LOG.info("testAddAfterClear");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("key1", "value1");
        builder.addParameter("key2", "value2");
        builder.clear();
        builder.addParameter("key3", "value3");
        builder.addParameter("key4", "value4");
        final String result = builder.toString();
        Assert.assertEquals("key3=value3&key4=value4", result);
    }

    @Test
    public void testAddSingleParameter() {
        LOG.info("testAddSingleParameter");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("key", "value");
        final String result = builder.toString();
        Assert.assertEquals("key=value", result);
    }

    @Test
    public void testAddEncodedParameter() {
        LOG.info("testAddEncodedParameter");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("Some Key", "Any&Value");
        final String result = builder.toString();
        Assert.assertEquals("Some+Key=Any%26Value", result);
    }

    @Test
    public void testAddTwoParameters() {
        LOG.info("testAddTwoParameters");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("key1", "value1");
        builder.addParameter("key2", "value2");
        final String result = builder.toString();
        Assert.assertEquals("key1=value1&key2=value2", result);
    }

    @Test
    public void testAddTwoParametersSameKey() {
        LOG.info("testAddTwoParametersSameKey");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("key", "value1");
        builder.addParameter("key", "value2");
        final String result = builder.toString();
        Assert.assertEquals("key=value1&key=value2", result);
    }

    @SuppressWarnings("ErrorNotRethrown")
    @Test
    public void testAttemptAddWithEmptyKey() {
        LOG.info("testAttemptAddWithEmptyKey");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        try {
            builder.addParameter("", "a value");
        } catch (final AssertionError ignored) {
            return;
        }
        Assert.fail("Wrong: empty key accepted!");
    }

    @Test
    public void testAttemptAddWithEmptyValue() {
        LOG.info("testAttemptAddWithEmptyValue");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("akey", "");
        final String result = builder.toString();
        Assert.assertEquals("akey=", result);
    }

    @SuppressWarnings("ErrorNotRethrown")
    @Test
    public void testAttemptAddWithNullKey() {
        LOG.info("testAttemptAddWithNullKey");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        try {
            //noinspection ConstantConditions
            builder.addParameter(null, "a value");
        } catch (final AssertionError | IllegalArgumentException ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @Test
    public void testClearEmptyBuilder() {
        LOG.info("testClearEmptyBuilder");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.clear();
        final String result = builder.toString();
        Assert.assertEquals(0, result.length());
    }

    @Test
    public void testClearFilledBuilder() {
        LOG.info("testClearFilledBuilder");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        builder.addParameter("key", "value");
        builder.clear();
        final String result = builder.toString();
        Assert.assertEquals(0, result.length());
    }

    @Test
    public void testCreateEmptyString() {
        LOG.info("testCreateEmptyString");
        final UrlParameterBuilder builder = new UrlParameterBuilder();
        final String result = builder.toString();
        Assert.assertEquals(0, result.length());
    }
}
