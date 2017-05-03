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

package com.tomtom.speedtools.rest;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.Servlet3AsyncHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CustomServlet3AsyncHttpRequest extends Servlet3AsyncHttpRequest {

    public CustomServlet3AsyncHttpRequest(
            @Nonnull final HttpServletRequest httpServletRequest,
            @Nonnull final HttpServletResponse response,
            @Nullable final ServletContext servletContext,
            @Nonnull final HttpResponse httpResponse,
            @Nonnull final ResteasyHttpHeaders httpHeaders,
            @Nonnull final ResteasyUriInfo uriInfo,
            @Nonnull final String s,
            @Nonnull final SynchronousDispatcher synchronousDispatcher) {
        super(httpServletRequest, response, servletContext, httpResponse, httpHeaders, uriInfo, s,
                synchronousDispatcher);
        assert httpServletRequest != null;
        assert response != null;
        assert httpResponse != null;
        assert httpHeaders != null;
        assert uriInfo != null;
        assert s != null;
        assert synchronousDispatcher != null;
    }

    @Nonnull
    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    @Nullable
    public HttpSession getSession(final boolean create) {
        // May return null.
        return request.getSession(create);
    }

    @Nonnull
    public HttpSession getSession() {
        return request.getSession();
    }
}
