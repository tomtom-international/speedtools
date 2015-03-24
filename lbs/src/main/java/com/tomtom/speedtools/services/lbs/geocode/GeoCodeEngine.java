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

package com.tomtom.speedtools.services.lbs.geocode;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;

import com.tomtom.speedtools.services.lbs.AuthorizationException;
import com.tomtom.speedtools.geometry.GeoRectangle;


/**
 * This interface wraps the TomTom LBS Geocoding functionality, found at http://lbs.tomtom.com.
 */
public interface GeoCodeEngine {

    /**
     * Basic query (without additional parameters). No input validation is performed on input query.
     *
     * @param query Free-text query to execute (according to LBS specification). May be empty.
     * @return Response. In case of an error, count == 0.
     * @throws AuthorizationException      Thrown if API key is rejected by server.
     * @throws ServiceUnavailableException Thrown if the service is (temporarily) unavailable.
     * @throws IOException                 Thrown if query to geo coder fails for some other reason.
     */
    @Nonnull
    GeoCodeEngineResponse query(@Nonnull String query)
            throws AuthorizationException, ServiceUnavailableException, IOException;

    /**
     * Query with additional parameters. No input validation is performed.
     *
     * @param query      Free-text query to execute (according to LBS specification). May be empty.
     * @param parameters Parameter set (see {@link com.tomtom.speedtools.urls.UrlParameterBuilder}). If null or empty,
     *                   this method equals the query method without parameters.
     * @return Response. In case of an error, count == 0.
     * @throws AuthorizationException      Thrown if API key is rejected by server.
     * @throws ServiceUnavailableException Thrown if the service is (temporarily) unavailable.
     * @throws IOException                 Thrown if query to geo coder fails for some other reason.
     */
    @Nonnull
    GeoCodeEngineResponse query(@Nonnull String query, @Nonnull String parameters)
            throws AuthorizationException, ServiceUnavailableException, IOException;

    /**
     * Query with bounding box (convenience function). Internally calls {@link com.tomtom.speedtools.urls
     * .UrlParameterBuilder} to add bounding box as a query parameter. No input validation is performed.
     *
     * @param query       Free-text query to execute (according to LBS specification). May be empty.
     * @param boundingBox Valid bounding box.
     * @return Response. In case of an error, count == 0.
     * @throws AuthorizationException      Thrown if API key is rejected by server.
     * @throws ServiceUnavailableException Thrown if the service is (temporarily) unavailable.
     * @throws IOException                 Thrown if query to geo coder fails for some other reason.
     */
    @Nonnull
    GeoCodeEngineResponse query(@Nonnull String query, @Nonnull GeoRectangle boundingBox)
            throws AuthorizationException, ServiceUnavailableException, IOException;

    /**
     * Query with bounding box and parameters (convenience function). Internally calls {@link
     * com.tomtom.speedtools.utils .urls.ParameterBuilder} to add bounding box as a query parameter. No input validation
     * is performed.
     *
     * @param query       Free-text query to execute (according to LBS specification). May not be null, but may be
     *                    empty.
     * @param boundingBox Valid bounding box (other than wrong API key).
     * @param parameters  Parameter set (see {@link com.tomtom.speedtools.urls.UrlParameterBuilder}). If null or empty,
     *                    this method equals the query method without parameters.
     * @return Response. In case of an error, count == 0.
     * @throws AuthorizationException      Thrown if API key is rejected by server.
     * @throws ServiceUnavailableException Thrown if the service is (temporarily) unavailable.
     * @throws IOException                 Thrown if query to geo coder fails for some other reason.
     */
    @Nonnull
    GeoCodeEngineResponse query(@Nonnull String query, @Nonnull GeoRectangle boundingBox,
                                @Nullable String parameters)
            throws AuthorizationException, ServiceUnavailableException, IOException;
}

