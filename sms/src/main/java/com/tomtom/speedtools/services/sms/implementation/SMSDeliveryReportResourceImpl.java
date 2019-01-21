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

package com.tomtom.speedtools.services.sms.implementation;

import com.tomtom.speedtools.apivalidation.exceptions.ApiParameterMissingException;
import com.tomtom.speedtools.services.sms.*;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListener.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.util.Set;

public class SMSDeliveryReportResourceImpl implements SMSDeliveryReportResource {
    private static final Logger LOG = LoggerFactory.getLogger(SMSDeliveryReportResourceImpl.class);

    @Nonnull
    private final Set<SMSProviderConnector> smsProviderConnectors;
    @Nonnull
    private final SMSDeliveryReportListenerRegistry smsDeliveryReportListenerRegistry;

    /**
     * Constructor.
     *
     * @param smsProviderConnectors             The connectors that are currently available.
     * @param smsDeliveryReportListenerRegistry The registry to obtain a listener to invoke when a delivery report comes in.
     */
    @Inject
    public SMSDeliveryReportResourceImpl(
            @Nonnull final Set<SMSProviderConnector> smsProviderConnectors,
            @Nonnull final SMSDeliveryReportListenerRegistry smsDeliveryReportListenerRegistry) {
        assert smsProviderConnectors != null;
        assert smsDeliveryReportListenerRegistry != null;

        this.smsProviderConnectors = smsProviderConnectors;
        this.smsDeliveryReportListenerRegistry = smsDeliveryReportListenerRegistry;
    }

    @Override
    public void deliverReport(
            @Nullable final String provider,
            @Nonnull final HttpServletRequest request,
            @Nonnull final AsyncResponse response) {

        LOG.debug("deliverReport: provider={}, uri={}, query string={}.",
                provider, request.getRequestURI(), request.getQueryString());

        if (provider == null) {
            throw new ApiParameterMissingException(PARAM_PROVIDER);
        }

        // If we have a listener, process message.
        final SMSDeliveryReportListener smsDeliveryReportListener = smsDeliveryReportListenerRegistry.getListener();
        if (smsDeliveryReportListener != null) {

            // Find the provider.
            for (final SMSProviderConnector smsProviderConnector : smsProviderConnectors) {
                if (smsProviderConnector.getProviderName().equalsIgnoreCase(provider)) {

                    try {

                        final SMSDeliveryReportProcessor smsDeliveryReportProcessor =
                                smsProviderConnector.getProcessor(request.getParameterMap());

                        // Must we process this report?
                        if (!smsDeliveryReportProcessor.skip()) {

                            // Obtain necessary parameters using the connector.
                            final long referenceNumber = smsDeliveryReportProcessor.getReferenceNumber();
                            final DeliveryStatus status =
                                    smsDeliveryReportProcessor.getDeliveryStatus();

                            // Pass on the report.
                            smsDeliveryReportListener.messageDeliveryReport(referenceNumber, status);
                        }
                    } catch (final SMSDeliveryReportParameterException e) {
                        LOG.warn("deliverReport: Errors while processing delivery report. provider={}.", provider, e);
                    }
                }
            }
        } else {
            LOG.warn("deliverReport: No delivery report listener registered. Ignoring report. provider={}.", provider);
        }

        // Can only reply success.
        response.resume(Response.ok().build());
    }
}
