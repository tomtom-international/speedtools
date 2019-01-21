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

package com.tomtom.speedtools.services.lbs.route;

import akka.actor.ActorSystem;
import com.tomtom.speedtools.geometry.Geo;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.guice.InvalidPropertyValueException;
import com.tomtom.speedtools.services.lbs.AuthorizationException;
import com.tomtom.speedtools.services.lbs.LbsProperties;
import com.tomtom.speedtools.services.lbs.route.implementation.TomTomLbsRouteEngine;
import com.tomtom.speedtools.tilemap.MapConst;
import org.joda.time.Duration;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Preliminary unit test for LBS Router implementation.
 */
@SuppressWarnings("ProhibitedExceptionDeclared")
public final class RouteEngineTest {
    private static final Logger LOG = LoggerFactory.getLogger(RouteEngineTest.class);

    /**
     * Insert your own developer key here. All tests using this key are set to Ignored by default.
     */
    private static final String DEFAULT_DEVELOPER_API_KEY = "0-0-0-0-0";

    private static final String DEFAULT_GEOCODE_URL_V1 = "http://lbs.tomtom.com/lbs/services/geocode/1/query";
    private static final String DEFAULT_GEOCODE_URL_V2 = "http://lbs.tomtom.com/lbs/services/geocode/2/query";
    private static final String DEFAULT_ROUTE_URL = "http://lbs.tomtom.com/lbs/services/route/1";
    private static final String DEFAULT_TILEMAP_URL = "http://lbs.tomtom.com/lbs/map/1/basic";

    // Use developer key; not a property.
    private static final LbsProperties LBS_PROPS_1 = new LbsProperties(
            DEFAULT_DEVELOPER_API_KEY, DEFAULT_GEOCODE_URL_V1, DEFAULT_GEOCODE_URL_V2,
            DEFAULT_TILEMAP_URL, DEFAULT_ROUTE_URL, false, 1);
    private static final LbsProperties LBS_PROPS_5 = new LbsProperties(
            DEFAULT_DEVELOPER_API_KEY, DEFAULT_GEOCODE_URL_V1, DEFAULT_GEOCODE_URL_V2,
            DEFAULT_TILEMAP_URL, DEFAULT_ROUTE_URL, false, 5);
    private static final LbsProperties LBS_PROPS_NO_API_KEY =
            new LbsProperties("INVALID", LBS_PROPS_1.getGeoCodeUrl1(), LBS_PROPS_1.getGeoCodeUrl2(),
                    LBS_PROPS_1.getTileMapUrl(), LBS_PROPS_1.getRouteUrl1(), false, 1);

    private static final FiniteDuration TIMEOUT_SHORT = new FiniteDuration(1, TimeUnit.MICROSECONDS);
    private static final FiniteDuration TIMEOUT_LONG = new FiniteDuration(10, TimeUnit.SECONDS);

    /**
     * These geo locations can be used for test cases which require locations separated by a traffic obstacle, in this
     * case the river IJ in Amsterdam. These locations are added as generic locations here for convenience, because you
     * might want to use them in different test scenarios.
     */
    private static final GeoPoint POS_TEST_THIS_RIVERSIDE = new GeoPoint(52.378555, 4.8940997);
    private static final GeoPoint POS_TEST_THIS_RIVERSIDE_FAR = new GeoPoint(52.36693, 4.93947); // Real: 4.2km, 9min.
    private static final GeoPoint POS_TEST_OTHER_RIVERSIDE = new GeoPoint(52.38353, 4.9023); //  Real: 6.4km, 17min.

    private ActorSystem system;

    @Before
    public void before() {
        system = ActorSystem.create("routing");
    }

    @After
    public void after() {
        system.terminate();
    }

    @Ignore
    @Test
    public void testRouteOK() throws Exception {
        LOG.info("testRouteOK");
        final RouteEngine geoCoder = new TomTomLbsRouteEngine(system, LBS_PROPS_1);
        try {
            final Future<RouteEngineResponse> future = geoCoder.route(MapConst.POS_AMSTERDAM, MapConst.POS_PARIS);
            final RouteEngineResponse resp = Await.result(future, TIMEOUT_LONG);
            LOG.info("resp = {}", resp.toString());
            Assert.assertTrue(resp.getTotalTimeSeconds() > 15000);
            Assert.assertTrue(resp.getTotalDistanceMeters() > 450000);
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS_PROPS_1.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        } catch (final TimeoutException ignored) {
            LOG.info("Result takes too long... Might be a slow connection?");
        }
    }

    @Ignore
    @Test
    public void testRouteManyOK() throws Exception {
        LOG.info("testRouteManyOK");
        final RouteEngine geoCoder = new TomTomLbsRouteEngine(system, LBS_PROPS_5);
        try {
            final int total = 20;
            final List<Future<RouteEngineResponse>> futures = new ArrayList<>(total);
            for (int i = 0; i < total; ++i) {
                final Future<RouteEngineResponse> future = geoCoder.route(MapConst.POS_AMSTERDAM, MapConst.POS_PARIS);
                futures.add(future);
            }
            for (int i = 0; i < total; ++i) {
                final RouteEngineResponse resp = Await.result(futures.get(i), TIMEOUT_LONG);
                LOG.info("resp = {}", resp.toString());
                Assert.assertTrue(resp.getTotalTimeSeconds() > 15000);
                Assert.assertTrue(resp.getTotalDistanceMeters() > 450000);
            }
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS_PROPS_1.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        } catch (final TimeoutException ignored) {
            LOG.info("Result takes too long... Might be a slow connection?");
        }
    }

    @Ignore
    @Test
    public void testRouteTimeout() throws Exception {
        LOG.info("testRouteTimeout");
        final RouteEngine geoCoder = new TomTomLbsRouteEngine(system, LBS_PROPS_1);
        try {
            //noinspection UnusedDeclaration
            final Future<RouteEngineResponse> future = geoCoder.route(MapConst.POS_AMSTERDAM, MapConst.POS_PARIS);
            Await.result(future, TIMEOUT_SHORT);
            Assert.fail("Expected timeout exception in LBS call");
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS_PROPS_1.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        } catch (final TimeoutException ignored) {
            LOG.debug("Timeout - as expected");
        }
    }

    @Ignore
    @Test
    public void testQueryNoApiKey() throws Exception {
        LOG.info("testQueryNoApiKey");
        final LbsProperties lbsNoApiKey = LBS_PROPS_NO_API_KEY;
        final RouteEngine geoCoder = new TomTomLbsRouteEngine(system, lbsNoApiKey);
        try {
            final Future<RouteEngineResponse> future = geoCoder.route(MapConst.POS_AMSTERDAM, MapConst.POS_PARIS);
            Await.result(future, TIMEOUT_LONG);
            Assert.fail();
        } catch (final InvalidPropertyValueException | AuthorizationException ignored) {
            // Ok.
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        } catch (final TimeoutException ignored) {
            LOG.info("Result takes too long... Might be a slow connection?");
        }
    }

    @Ignore
    @Test
    public void testRouteAndCrowFlight() throws Exception {
        LOG.info("testRouteAndCrowFlight");
        final RouteEngine geoCoder = new TomTomLbsRouteEngine(system, LBS_PROPS_1);
        final Duration lowerNearSlow =
                Geo.estimatedMinTravelTime(POS_TEST_THIS_RIVERSIDE, POS_TEST_OTHER_RIVERSIDE);
        final Duration lowerFarFast =
                Geo.estimatedMinTravelTime(POS_TEST_THIS_RIVERSIDE, POS_TEST_THIS_RIVERSIDE_FAR);
        final double distanceNearSlow =
                Geo.distanceInMeters(POS_TEST_THIS_RIVERSIDE, POS_TEST_OTHER_RIVERSIDE);
        final double distanceFarFast =
                Geo.distanceInMeters(POS_TEST_THIS_RIVERSIDE, POS_TEST_THIS_RIVERSIDE_FAR);
        try {
            final Future<RouteEngineResponse> futureNearSlow = geoCoder.route(
                    POS_TEST_THIS_RIVERSIDE, POS_TEST_OTHER_RIVERSIDE);
            final Future<RouteEngineResponse> futureFarFast = geoCoder.route(
                    POS_TEST_THIS_RIVERSIDE, POS_TEST_THIS_RIVERSIDE_FAR);
            final RouteEngineResponse accurateNearSlow = Await.result(futureNearSlow, TIMEOUT_LONG);
            final RouteEngineResponse accurateFarFast = Await.result(futureFarFast, TIMEOUT_LONG);

            LOG.debug("lowerNearSlow   = distance={}m, time={}s, speed={}km/h",
                    distanceNearSlow, lowerNearSlow.getStandardSeconds(),
                    (distanceNearSlow / lowerNearSlow.getStandardSeconds()) * 3.6);
            LOG.debug("lowerFarFast    = distance={}m, time={}s, speed={}km/h",
                    distanceFarFast, lowerFarFast.getStandardSeconds(),
                    (distanceFarFast / lowerFarFast.getStandardSeconds()) * 3.6);
            LOG.debug("accurateNearSlow = {}, speed={}km/h", accurateNearSlow.toString(),
                    (accurateNearSlow.getTotalDistanceMeters() / accurateNearSlow.getTotalTimeSeconds()) * 3.6);
            LOG.debug("accurateFarFast  = {}, speed={}km/h", accurateFarFast.toString(),
                    (accurateFarFast.getTotalDistanceMeters() / accurateFarFast.getTotalTimeSeconds()) * 3.6);
        } catch (final AuthorizationException e) {
            Assert.fail("Test failed... Is this API key still valid? " + LBS_PROPS_1.getApiKey());
        } catch (final ServiceUnavailableException ignored) {
            LOG.info("Test cannot be executed... service unavailable");
        } catch (final IOException ignored) {
            LOG.info("Test cannot be executed... Is test running stand-alone?");
        } catch (final TimeoutException ignored) {
            LOG.info("Result takes too long... Might be a slow connection?");
        }
    }
}
