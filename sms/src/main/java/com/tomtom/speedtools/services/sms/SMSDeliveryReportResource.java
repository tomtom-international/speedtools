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

package com.tomtom.speedtools.services.sms;

import org.jboss.resteasy.annotations.Suspend;
import org.jboss.resteasy.spi.AsynchronousResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;


/**
 * Resteasy resource that will be called by the message provider to report about the delivery of a text message.
 */
@Path("/sms/1/reports")
public interface SMSDeliveryReportResource {

    public static final int SUSPEND_TIMEOUT = 30000;    // 30 seconds.

    String PARAM_PROVIDER = "provider";

    /**
     * This operation sends a delivery report to the registered listener.
     * <p/>
     * Return HTTP status code 200.
     *
     * @param provider The provider that this report pertains to.
     * @param request  Injected HttpServletRequest.
     * @param response {@link AsynchronousResponse} which is empty.
     */
    @GET
    @Path('{' + PARAM_PROVIDER + '}')
    void deliverReport(
            @Nullable @PathParam(PARAM_PROVIDER) String provider,
            @Nonnull @Context HttpServletRequest request,
            @Nonnull @Suspend(SUSPEND_TIMEOUT) AsynchronousResponse response);
}
