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

package com.tomtom.speedtools.rest;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import javax.annotation.Nonnull;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true)
public class CustomHttpServlet30Dispatcher extends HttpServlet30Dispatcher {

    @Override
    protected HttpRequest createHttpRequest(
            @Nonnull final String httpMethod,
            @Nonnull final HttpServletRequest httpServletRequest,
            @Nonnull final ResteasyHttpHeaders httpHeaders,
            @Nonnull final ResteasyUriInfo uriInfo,
            @Nonnull final HttpResponse httpResponse,
            @Nonnull final HttpServletResponse httpServletResponse) {
        assert httpMethod != null;
        assert httpServletRequest != null;
        assert httpHeaders != null;
        assert uriInfo != null;
        assert httpResponse != null;
        assert httpServletResponse != null;

        return new CustomServlet3AsyncHttpRequest(httpServletRequest, httpServletResponse, null,
                httpResponse, httpHeaders, uriInfo, httpMethod, (SynchronousDispatcher) getDispatcher());
    }
}
