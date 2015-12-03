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

package com.tomtom.speedtools.services.sms.implementation.nexmo;

import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListener;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListener.DeliveryStatus;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListenerRegistry;
import com.tomtom.speedtools.services.sms.implementation.nexmo.dto.NexmoMessage;
import com.tomtom.speedtools.services.sms.implementation.nexmo.dto.NexmoMessage.Status;
import com.tomtom.speedtools.services.sms.implementation.nexmo.dto.NexmoMessageResponse;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.util.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Mock that always returns SENT when used to send a message.
 */
public class NexmoResourceMock implements NexmoResource {
    private static final Logger LOG = LoggerFactory.getLogger(NexmoResourceMock.class);

    @Nonnull
    private final SMSDeliveryReportListenerRegistry smsDeliveryReportListenerRegistry;

    public NexmoResourceMock(@Nonnull final SMSDeliveryReportListenerRegistry smsDeliveryReportListenerRegistry) {
        assert smsDeliveryReportListenerRegistry != null;

        this.smsDeliveryReportListenerRegistry = smsDeliveryReportListenerRegistry;
    }

    @Override
    public ClientResponse<NexmoMessageResponse> sendMessage(
            @Nonnull final String userName,
            @Nonnull final String password,
            @Nonnull final String from,
            @Nonnull final String to,
            @Nullable final String type,
            @Nullable final String text,
            @Nullable final String statusReportRequired,
            @Nullable final Long clientReference) {
        assert userName != null;
        assert password != null;
        assert from != null;
        assert to != null;

        final Status status = Status.SUCCESS;

        LOG.debug("sendMessage: status={}, userName={}, password={}, from={}, to={}, text={}, " +
                        "statusReportRequired={}, clientReference={}",
                status, userName, password, from, to, text, statusReportRequired, clientReference);

        final NexmoMessageResponse nexmoMessageResponse = new NexmoMessageResponse();
        final NexmoMessage message = new NexmoMessage();
        nexmoMessageResponse.setMessages(Immutables.listOf(message));

        nexmoMessageResponse.setMessageCount(1);
        message.setStatus(status);
        if (clientReference != null) {
            message.setClientRef(clientReference.toString());
        }

        // Return a delivery notification first (This only works because the listener is the SMSActor which will
        // process the delivery report asynchronously).
        final SMSDeliveryReportListener listener = smsDeliveryReportListenerRegistry.getListener();
        if ((listener != null) && (clientReference != null)) {
            listener.messageDeliveryReport(clientReference, DeliveryStatus.DELIVERED);
        }

        return new ClientResponse<NexmoMessageResponse>() {

            @Override
            public MultivaluedMap<String, String> getResponseHeaders() {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nonnull
            public Status getResponseStatus() {
                return Status.OK;
            }

            @Override
            @Nullable
            public NexmoMessageResponse getEntity() {
                return nexmoMessageResponse;
            }

            @Override
            public <T> T readEntity(final Class<T> tClass) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T readEntity(final javax.ws.rs.core.GenericType<T> tGenericType) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T readEntity(final Class<T> tClass, final Annotation[] annotations) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T readEntity(final javax.ws.rs.core.GenericType<T> tGenericType, final Annotation[] annotations) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasEntity() {
                return false;
            }

            @Override
            public boolean bufferEntity() {
                return false;
            }

            @Override
            public void close() {
            }

            @Override
            public MediaType getMediaType() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Locale getLanguage() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getLength() {
                return 0;
            }

            @Override
            public Set<String> getAllowedMethods() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, NewCookie> getCookies() {
                throw new UnsupportedOperationException();
            }

            @Override
            public EntityTag getEntityTag() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Date getDate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Date getLastModified() {
                throw new UnsupportedOperationException();
            }

            @Override
            public URI getLocation() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<javax.ws.rs.core.Link> getLinks() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasLink(final String s) {
                return false;
            }

            @Override
            public javax.ws.rs.core.Link getLink(final String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Builder getLinkBuilder(final String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nullable
            public <T2> T2 getEntity(@Nonnull final Class<T2> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nullable
            public <T2> T2 getEntity(
                    @Nonnull final Class<T2> type,
                    @Nonnull final Type genericType) {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nullable
            public <T2> T2 getEntity(
                    @Nonnull final Class<T2> type,
                    @Nonnull final Type genericType,
                    @Nonnull final Annotation[] annotations) {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nullable
            public <T2> T2 getEntity(@Nonnull final GenericType<T2> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nullable
            public <T2> T2 getEntity(@Nonnull final GenericType<T2> type, @Nonnull final Annotation[] annotations) {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nonnull
            public LinkHeader getLinkHeader() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Link getLocationLink() {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nonnull
            public Link getHeaderAsLink(final String headerName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void resetStream() {
            }

            @Override
            public void releaseConnection() {
            }

            @Override
            @Nonnull
            public Map<String, Object> getAttributes() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getStatus() {
                return getResponseStatus().getStatusCode();
            }

            @Override
            public StatusType getStatusInfo() {
                throw new UnsupportedOperationException();
            }

            @Override
            @Nonnull
            public MultivaluedMap<String, Object> getMetadata() {
                throw new UnsupportedOperationException();
            }

            @Override
            public MultivaluedMap<String, String> getStringHeaders() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getHeaderString(final String s) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
