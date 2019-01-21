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

package com.tomtom.speedtools.services.lbs.geocode;

import com.tomtom.speedtools.guice.InvalidPropertyValueException;
import com.tomtom.speedtools.services.lbs.AuthorizationException;
import com.tomtom.speedtools.services.lbs.LbsProperties;
import com.tomtom.speedtools.services.lbs.geocode.implementation.TomTomLbsGeoCodeEngine;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;


/**
 * Preliminary unit test for LBS GeoCoder query implementation.
 */
public final class TomTomLbsGeoCodeEngineTest {
    private static final Logger LOG = LoggerFactory.getLogger(TomTomLbsGeoCodeEngineTest.class);

    /**
     * Insert your own developer key here. All tests using this key are set to Ignored by default.
     */
    private static final String DEFAULT_DEVELOPER_API_KEY = "0-0-0-0-0";

    private static final String DEFAULT_GEOCODE_URL_V1 = "http://lbs.tomtom.com/lbs/services/geocode/1/query";
    private static final String DEFAULT_GEOCODE_URL_V2 = "http://lbs.tomtom.com/lbs/services/geocode/2/query";
    private static final String DEFAULT_ROUTE_URL = "http://lbs.tomtom.com/lbs/router/1";
    private static final String DEFAULT_TILEMAP_URL = "http://lbs.tomtom.com/lbs/map/1/basic";

    // Use developer key; not a property.
    private static final LbsProperties LBS = new LbsProperties(
            DEFAULT_DEVELOPER_API_KEY, DEFAULT_GEOCODE_URL_V1, DEFAULT_GEOCODE_URL_V2,
            DEFAULT_TILEMAP_URL, DEFAULT_ROUTE_URL, false, 1);

    @Ignore
    @Test
    public void testEmptyQuery() {
        LOG.info("testEmptyQuery");
        final GeoCodeEngine geoCodeEngine = new TomTomLbsGeoCodeEngine(LBS);
        try {
            final GeoCodeEngineResponse resp = geoCodeEngine.query("");
            LOG.info("resp = {}", resp.toString());
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        }
    }

    @SuppressWarnings("ErrorNotRethrown")
    @Ignore
    @Test
    public void testNullQuery() {
        LOG.info("testNullQuery");
        final GeoCodeEngine geoCodeEngine = new TomTomLbsGeoCodeEngine(LBS);
        try {
            //noinspection ConstantConditions
            final GeoCodeEngineResponse resp = geoCodeEngine.query(null);
            LOG.info("resp = {}", resp.toString());
        } catch (final AssertionError | IllegalArgumentException ignored) {
            return;
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
            return;
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @Ignore
    @Test
    public void testQueryNoApiKey() {
        LOG.info("testQueryNoApiKey");
        try {
            final LbsProperties lbsNoApiKey = new LbsProperties("", LBS.getGeoCodeUrl1(), LBS.getGeoCodeUrl2(),
                    LBS.getTileMapUrl(), LBS.getRouteUrl1(), false, 1);
            final GeoCodeEngine geoCodeEngine = new TomTomLbsGeoCodeEngine(lbsNoApiKey);
            final GeoCodeEngineResponse resp = geoCodeEngine.query("leiden");
            if (resp.getCount() > 0) {
                Assert.fail();
            }
        } catch (final InvalidPropertyValueException | AuthorizationException ignored) {
            // Ok.
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        }
    }

    @Ignore
    @Test
    public void testQuery1() {
        LOG.info("testQuery1");
        final GeoCodeEngine geoCodeEngine = new TomTomLbsGeoCodeEngine(LBS);
        try {
            final GeoCodeEngineResponse resp = geoCodeEngine.query("Herengracht");
            LOG.info("resp = {}", resp.toString());
            Assert.assertTrue(resp.getCount() > 0);
            Assert.assertTrue(!resp.getApiVersion().isEmpty());
        } catch (final IllegalStateException ignored) {
            // Ok.
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        }
    }

    @Ignore
    @Test
    public void testQuery2() {
        LOG.info("testQuery2");
        final GeoCodeEngine geoCodeEngine = new TomTomLbsGeoCodeEngine(LBS);
        try {
            final GeoCodeEngineResponse resp = geoCodeEngine.query("herengracht 110,leiden,nl");
            LOG.info("resp = {}", resp);
            Assert.assertTrue(!resp.getApiVersion().isEmpty());
            if (resp.getCount() > 0) {
                LOG.info("first = {}", resp.getGeoCodeEngineResultList().get(0));
                LOG.info("resp = {}", resp.toString());
                Assert.assertFalse(resp.getGeoCodeEngineResultList().isEmpty());
            }
        } catch (final IllegalStateException ignored) {
            // Ok.
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        }
    }
}
