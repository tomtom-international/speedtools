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

package com.tomtom.speedtools.services.lbs.geocode.implementation;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.naming.ServiceUnavailableException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

import com.tomtom.speedtools.services.lbs.AuthorizationException;
import com.tomtom.speedtools.services.lbs.Lbs;
import com.tomtom.speedtools.services.lbs.LbsProperties;
import com.tomtom.speedtools.services.lbs.geocode.GeoCodeEngine;
import com.tomtom.speedtools.services.lbs.geocode.GeoCodeEngineResponse;
import com.tomtom.speedtools.geometry.GeoRectangle;
import com.tomtom.speedtools.urls.UrlParameterBuilder;


/**
 * Implementation of a {@link com.tomtom.speedtools.services.lbs.geocode.GeoCodeEngine} using the TomTom LBS
 * Geocoder.
 */
public final class TomTomLbsGeoCodeEngine implements GeoCodeEngine {
    private static final Logger LOG = LoggerFactory.getLogger(TomTomLbsGeoCodeEngine.class);

    // LBS properties.
    @Nonnull
    private final LbsProperties lbsProperties;

    @Inject
    public TomTomLbsGeoCodeEngine(
            @Nonnull final LbsProperties lbsProperties) {
        assert lbsProperties != null;
        this.lbsProperties = lbsProperties;
    }

    @Nonnull
    @Override
    public GeoCodeEngineResponse query(@Nonnull final String query)
            throws AuthorizationException, ServiceUnavailableException, IOException {
        assert query != null;
        final String url = getBaseQueryString(query);
        return executeLbsQuery(url);
    }

    @Nonnull
    @Override
    public GeoCodeEngineResponse query(@Nonnull final String query, @Nonnull final String parameters)
            throws AuthorizationException, ServiceUnavailableException, IOException {
        assert query != null;
        assert parameters != null;
        final String url = getBaseQueryString(query) + ';' + parameters;
        return executeLbsQuery(url);
    }

    @Nonnull
    @Override
    public GeoCodeEngineResponse query(@Nonnull final String query, @Nonnull final GeoRectangle boundingBox)
            throws AuthorizationException, ServiceUnavailableException, IOException {
        assert query != null;
        assert boundingBox != null;
        final String url = getBaseQueryString(query) + ";bbox=" + toTomTomLbsUriString(boundingBox);
        return executeLbsQuery(url);
    }

    @Nonnull
    @Override
    public GeoCodeEngineResponse query(@Nonnull final String query, @Nonnull final GeoRectangle boundingBox,
                                       @Nullable final String parameters)
            throws AuthorizationException, ServiceUnavailableException, IOException {
        assert query != null;
        assert boundingBox != null;

        // Escape.
        if (parameters == null) {
            return query(query, boundingBox);
        }
        final String url = getBaseQueryString(query) + ";bbox=" + toTomTomLbsUriString(boundingBox) + ';' + parameters;
        return executeLbsQuery(url);
    }

    /**
     * Convenience method: return a fully qualified URL to which parameters can be appended.
     *
     * @param query Query.
     * @return Query URL.
     */
    @Nonnull
    private String getBaseQueryString(@Nonnull final String query) {
        assert query != null;
        return lbsProperties.getGeoCodeUrl1() + '/' +
                UrlParameterBuilder.encode(query) + '/' +
                Lbs.LBS_GEOCODE_FORMAT + '/' +
                lbsProperties.getApiKey();
    }

    /**
     * Perform actual LBS HTTP query.
     *
     * @param queryUrl URL to query.
     * @return Valid GeoCoderResponse object.
     * @throws AuthorizationException      Thrown in case API key is rejected by server
     * @throws ServiceUnavailableException Thrown if service is not available.
     * @throws IOException                 Thrown if GET could not be executed for some reason.
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    @Nonnull
    private static GeoCodeEngineResponse executeLbsQuery(@Nonnull final String queryUrl)
            throws AuthorizationException, ServiceUnavailableException, IOException {
        assert queryUrl != null;
        LOG.debug("executeLbsQuery: url={}", queryUrl);
        final GetMethod get = new GetMethod(queryUrl);
        final int status = new HttpClient().executeMethod(get);

        // Log error message.
        if (status != HttpStatus.SC_OK) {
            LOG.warn("executeLbsQuery: geocoding service failure, url={}, status={} ",
                    queryUrl, get.getStatusLine());
        }
        final GeoCodeEngineResponse response;
        switch (status) {

            case HttpStatus.SC_OK:
                try {
                    response = unmarshalGeoCoderResponseBody(get.getResponseBodyAsStream());
                } catch (final JAXBException e) {
                    LOG.warn("executeLbsQuery: cannot unmarshal response", e);
                    throw new IOException("Cannot unmarshal response", e);
                } finally {
                    get.releaseConnection();
                }
                assert response != null;
                break;

            case HttpStatus.SC_NO_CONTENT:
                response = new GeoCodeEngineResponse();
                break;

            case HttpStatus.SC_UNAUTHORIZED:
                // Fall through.
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
                // Fall through.
            case HttpStatus.SC_NOT_ACCEPTABLE:
                // Fall through.
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                throw new AuthorizationException();

            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                // Fall through.
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                // Fall through.
            case HttpStatus.SC_GATEWAY_TIMEOUT:
                // Fall through.
            case HttpStatus.SC_INSUFFICIENT_STORAGE:
                throw new ServiceUnavailableException();

            default:
                throw new IOException("Cannot call geocoder, status=" + status + " (" + get.getStatusLine() + ')');
        }
        assert response != null;
        return response;
    }

    /**
     * Do the heavy lifting of binding the input stream to the JAX-B annotated objects.
     *
     * @param stream Stream to read from.
     * @return Valid GeoCoderResponse object or null on error.
     * @throws JAXBException If an error occurs unmarshalling.
     */
    @Nullable
    private static GeoCodeEngineResponse unmarshalGeoCoderResponseBody(@Nonnull final InputStream stream)
            throws JAXBException {
        assert stream != null;
        final JAXBContext context = JAXBContext.newInstance(
                GeoCodeEngineResponse.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final GeoCodeEngineResponse response = (GeoCodeEngineResponse) unmarshaller.unmarshal(stream);
        return response;
    }

    /**
     * Return a LBS formatted boundary box string.
     *
     * @return Boundary box formatted for use with TomTom LBS.
     */
    @Nonnull
    private static String toTomTomLbsUriString(@Nonnull final GeoRectangle rect) {
        assert rect != null;
        return rect.getSouthWest().getLat().toString() + ',' + rect.getSouthWest().getLon() + ',' +
                rect.getNorthEast().getLat() + ',' + rect.getNorthEast().getLon();
    }
}
