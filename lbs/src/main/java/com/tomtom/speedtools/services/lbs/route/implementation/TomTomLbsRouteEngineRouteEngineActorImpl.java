/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.services.lbs.route.implementation;

import akka.dispatch.Futures;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.services.lbs.AuthorizationException;
import com.tomtom.speedtools.services.lbs.LbsProperties;
import com.tomtom.speedtools.services.lbs.route.RouteEngineResponse;
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.urls.UrlParameterBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.ServiceUnavailableException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;


/**
 * Implementation of a {@link com.tomtom.speedtools.services.lbs.route.RouteEngine} using the TomTom LBS Router.
 */
final class TomTomLbsRouteEngineRouteEngineActorImpl implements TomTomLbsRouteEngineActor {
    private static final Logger LOG = LoggerFactory.getLogger(TomTomLbsRouteEngineRouteEngineActorImpl.class);

    private static final String LBS_OUTPUT_TYPE = "xml";
    private static final String LBS_ROUTE_TYPE = "Quickest";
    private static final String LBS_OPTIONS_TRAFFIC = ";avoidTraffic=true;includeTraffic=true";
    private static final String LBS_OPTIONS_OTHERS = ";day=today;time=now;iqRoutes=2;map=basic;pathPoints=1";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Nonnull
    private final String name;
    @Nonnull
    private final LbsProperties lbsProperties;

    TomTomLbsRouteEngineRouteEngineActorImpl(
            @Nonnull final String name,
            @Nonnull final LbsProperties lbsProperties) {
        assert name != null;
        assert lbsProperties != null;
        this.name = name;
        this.lbsProperties = lbsProperties;
    }

    @Override
    @Nonnull
    public Future<RouteEngineResponse> route(
            @Nonnull final GeoPoint from,
            @Nonnull final GeoPoint to,
            @Nullable final DateTime executeBefore) {
        assert from != null;
        assert to != null;

        // TODO ST-3: For as long as this is a route actor, we shall prevent crashing this actor at all cost.
        try {
            LOG.debug("route: {} Request, from={}, to={}, traffic={}", name, from, to,
                    lbsProperties.isTrafficEnabled());
            final DateTime now = UTCTime.now();
            if ((executeBefore == null) || now.isBefore(executeBefore)) {
                final String url = getBaseQueryString(from, to);
                final TomTomLbsRouteEngineResponse lbsResponse;
                lbsResponse = executeLbsQuery(url);
                LOG.debug("route: {} Success, from={}, to={}, response={}", name, from, to, lbsResponse);

                final RouteEngineResponse routeEngineResponse = lbsResponse.convertToRouteEngineResponse();
                return Futures.successful(routeEngineResponse);
            } else {
                final String message = "Expected execution before: " + executeBefore.toString(DATE_FORMAT) +
                        " but started at: " + now.toString(DATE_FORMAT);
                LOG.debug("route: {} Too busy, from={}, to={}, {}", name, from, to, message);
                return Futures.failed(new TimeoutException(message));
            }
        } catch (final AuthorizationException | ServiceUnavailableException | IOException e) {
            return Futures.failed(e);
        } catch (final Throwable e) {

            // Catch all to make sure the actor won't die and takes its parent down.
            return Futures.failed(new IllegalStateException(e));
        }
    }

    /**
     * Convenience method: return a fully qualified URL to which parameters can be appended.
     *
     * @param from Departure point.
     * @param to   Arrival point.
     * @return From-to URL.
     */
    @Nonnull
    private String getBaseQueryString(@Nonnull final GeoPoint from, @Nonnull final GeoPoint to) {
        assert from != null;
        assert to != null;
        return lbsProperties.getRouteUrl1() + '/' +
                toTomTomLbsUriString(from, to) + '/' +
                LBS_ROUTE_TYPE + '/' + LBS_OUTPUT_TYPE + '/' +
                lbsProperties.getApiKey() +
                (lbsProperties.isTrafficEnabled() ? LBS_OPTIONS_TRAFFIC : "") +
                LBS_OPTIONS_OTHERS;
    }

    /**
     * Perform actual LBS HTTP query.
     *
     * @param queryUrl URL to query.
     * @return Valid route engine response object.
     * @throws AuthorizationException      Thrown in case API key is rejected by server
     * @throws ServiceUnavailableException Thrown if service is not available.
     * @throws IOException                 Thrown if GET could not be executed for some reason.
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    @Nonnull
    private static TomTomLbsRouteEngineResponse executeLbsQuery(@Nonnull final String queryUrl)
            throws AuthorizationException, ServiceUnavailableException, IOException {
        assert queryUrl != null;
        LOG.trace("executeLbsQuery: url={}", queryUrl);
        final GetMethod get = new GetMethod(queryUrl);
        final int status = new HttpClient().executeMethod(get);

        // Log error message.
        if (status != HttpStatus.SC_OK) {
            LOG.info("executeLbsQuery: routing service failure, url={}, status={} ",
                    queryUrl, get.getStatusLine());
        }
        final TomTomLbsRouteEngineResponse response;
        switch (status) {

            case HttpStatus.SC_OK:
                try {
                    response = unmarshalRouterResponseBody(get.getResponseBodyAsStream());
                } catch (final JAXBException e) {
                    LOG.warn("executeLbsQuery: cannot unmarshal response", e);
                    throw new IOException("Cannot unmarshal response", e);
                } finally {
                    get.releaseConnection();
                }
                assert response != null;
                break;

            case HttpStatus.SC_UNAUTHORIZED:
                // Fall through.
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
                // Fall through.
            case HttpStatus.SC_NOT_ACCEPTABLE:
                // Fall through.
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                throw new AuthorizationException();

            case HttpStatus.SC_NO_CONTENT:
                // Fall through.
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                // Fall through.
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                // Fall through.
            case HttpStatus.SC_GATEWAY_TIMEOUT:
                // Fall through.
            case HttpStatus.SC_INSUFFICIENT_STORAGE:
                throw new ServiceUnavailableException();

            default:
                throw new IOException("Cannot call route engine, status=" + status + " (" + get.getStatusLine() + ')');
        }
        assert response != null;
        return response;
    }

    /**
     * Do the heavy lifting of binding the input stream to the JAX-B annotated objects.
     *
     * @param stream Stream to read from.
     * @return Valid route response object or null on error.
     * @throws JAXBException Thrown if unmarshalling fails.
     */
    @Nonnull
    private static TomTomLbsRouteEngineResponse unmarshalRouterResponseBody(@Nonnull final InputStream stream)
            throws JAXBException {
        assert stream != null;
        final JAXBContext context = JAXBContext.newInstance(
                TomTomLbsRouteEngineResponse.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final TomTomLbsRouteEngineResponse response =
                (TomTomLbsRouteEngineResponse) unmarshaller.unmarshal(stream);
        if (response == null) {
            throw new JAXBException("Response cannot be unmarshalled");
        }
        return response;
    }

    /**
     * Return a LBS formatted route request string.
     *
     * @return Route request string.
     */
    @Nonnull
    private static String toTomTomLbsUriString(@Nonnull final GeoPoint from, @Nonnull final GeoPoint to) {
        assert from != null;
        assert to != null;
        return encodeGeoPoint(from) + ':' + encodeGeoPoint(to);
    }

    @Nonnull
    private static String encodeGeoPoint(@Nonnull final GeoPoint point) {
        assert point != null;
        final String result = point.getLat().toString() + ',' + point.getLon();
        return UrlParameterBuilder.encode(result);
    }
}
