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

package com.tomtom.speedtools.services.sms.implementation.messagebird;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListener.DeliveryStatus;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportParameterException;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportProcessor;
import com.tomtom.speedtools.services.sms.SMSProviderConnector;
import com.tomtom.speedtools.services.sms.implementation.ProviderNames;
import com.tomtom.speedtools.services.sms.implementation.ProviderRanking;
import com.tomtom.speedtools.services.sms.implementation.messagebird.MessageBirdResource.ResponseType;
import com.tomtom.speedtools.services.sms.implementation.messagebird.dto.MessageBirdMessageResponse;
import com.tomtom.speedtools.services.sms.implementation.messagebird.dto.MessageBirdMessageResponse.Item;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Provides SMS functionality using MessageBird (www.messagebird.com). Api documentation can be found here:
 * https://www.messagebird.com/nl/api
 */
public class MessageBird implements SMSProviderConnector {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(MessageBird.class);

    /**
     * These parameter names and values are defined by the MessageBird api.
     */
    private static final String STATUS_REPORT_PARAMETER = "STATUS";
    private static final String DELIVERED_STATUS = "delivered";
    private static final String BUFFERED_STATUS = "bufferd";
    private static final String FAILED_STATUS = "not delivered";
    private static final String REFERENCE_PARAMETER = "REFERENCE";
    private static final String REPLACE_CHARS = "true"; // Attempt to send as GSM-7 by replacing chars.

    @Nonnull
    private final MessageBirdResource messageBirdResource;
    @Nonnull
    private final String userName;
    @Nonnull
    private final String password;
    @Nonnull
    private final String sender;

    /**
     * Constructor.
     *
     * @param messageBirdResource   The Resteasy resource to do REST calls to MessageBird.
     * @param messageBirdProperties The properties for MessageBird.
     */
    @Inject
    public MessageBird(
            @Nonnull final MessageBirdResource messageBirdResource,
            @Nonnull final MessageBirdProperties messageBirdProperties) {
        assert messageBirdResource != null;
        assert messageBirdProperties != null;

        this.messageBirdResource = messageBirdResource;
        userName = messageBirdProperties.getUserName();
        password = messageBirdProperties.getPassword();
        sender = messageBirdProperties.getSender();
    }

    @Nonnull
    @Override
    public Status sendTextMessage(
            @Nonnull final String recipient,
            @Nonnull final String message,
            @Nullable final Long referenceNumber) {
        assert recipient != null;
        assert message != null;

        ClientResponse<MessageBirdMessageResponse> response = null;
        try {

            // Format phone number.
            final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

            // Use E164 format, but remove the '+'.
            final String destination =
                    util.format(util.parse(recipient, null), PhoneNumberFormat.E164).substring(1);

            // Send the message.
            LOG.debug("sendTextMessage: sender={}, recipient={}, ref={}, message={}",
                    sender, recipient, referenceNumber, message);
            response =
                    messageBirdResource.sendMessage(userName, password, referenceNumber, sender, destination, message,
                            ResponseType.XML, REPLACE_CHARS);

            // Process the result.
            if (response.getResponseStatus() == Response.Status.OK) {

                // MessageBird incorrectly sets 'text/html' as the content type. Therefore, we override it here.
                response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

                final MessageBirdMessageResponse messageBirdMessageResponse = response.getEntity();

                final Item item = messageBirdMessageResponse.getItem();
                switch (item.getResponseCode()) {

                    case REQUEST_SUCCESSFUL:
                        return Status.SENT;

                    case DATE_NOT_ALLOWED:
                        // Fall through.
                    case INVALID_TIMESTAMP:
                        // Fall through.
                    case TIMESTAMP_IN_PAST:
                        // Not sending any timestamp, so this is weird.
                        LOG.error(
                                "sendTextMessage: Date not allowed. error={}, recipient={}, message={}, ref={}.",
                                item.getResponseMessage(), recipient, message, referenceNumber);
                        return Status.FAILED_PERMANENTLY;

                    case MESSAGE_TOO_LONG:
                        LOG.info(
                                "sendTextMessage: Message too long. error={}, recipient={}, message={}, ref={}.",
                                item.getResponseMessage(), recipient, message, referenceNumber);
                        return Status.INVALID_PARAMETERS;

                    case INVALID_SENDER:
                        LOG.error("sendTextMessage: Invalid sender address. error={}. sender={}.",
                                item.getResponseMessage(), sender);
                        return Status.FAILED_PERMANENTLY;

                    case INVALID_DESTINATION:
                        LOG.info(
                                "sendTextMessage: Invalid destination. error={}, recipient={}, message={}, ref={}.",
                                item.getResponseMessage(), recipient, message, referenceNumber);
                        return Status.INVALID_PARAMETERS;

                    case EMPTY_BODY:
                        LOG.info(
                                "sendTextMessage: Empty body. error={}, recipient={}, message={}, ref={}.",
                                item.getResponseMessage(), recipient, message, referenceNumber);
                        return Status.INVALID_PARAMETERS;

                    case INSUFFICIENT_CREDITS:
                        LOG.warn("sendTextMessage: Insufficient credits. error={}, remaining credits={}.",
                                item.getResponseMessage(), item.getCredits());
                        return Status.TEMPORARILY_UNAVAILABLE;

                    case INVALID_USERNAME_PASSWORD:
                        LOG.error("sendTextMessage: Invalid userName/password. error={}, userName={}.",
                                item.getResponseMessage(), userName);
                        return Status.FAILED_PERMANENTLY;

                    case IP_NOT_ALLOWED:
                        LOG.error("sendTextMessage: IP address not allowed. error={}.",
                                item.getResponseMessage());
                        return Status.FAILED_PERMANENTLY;

                    case CANNOT_CONNECT:
                        LOG.warn(
                                "sendTextMessage: Internal error at MessageBird. error={}, recipient={}, message={}. " +
                                        "Ref.Nr: {}", item.getResponseMessage(), recipient, message);
                        return Status.FAILED;

                    default:
                        LOG.error("sendTextMessage: Unknown status code. error={}", item.getResponseMessage());
                        assert false;
                        break;
                }
            }
        } catch (final NumberParseException e) {
            LOG.info(
                    "sendTextMessage: Failed to parse telephone number. recipient: {}. message: {}. ref: {}.",
                    recipient, message, referenceNumber, e);
            return Status.INVALID_PARAMETERS;
        } catch (final Throwable t) {
            // Resteasy throws several runtime exceptions, but it is not exactly clear which.
            LOG.error("sendTextMessage: Failed to send message to MessageBird. recipient={}, message={}, ref={}.",
                    recipient, message, referenceNumber, t);
        } finally {
            if (response != null) {
                // Always release connection.
                response.releaseConnection();
            }
        }

        return Status.FAILED;
    }

    @Override
    @Nonnull
    public String getProviderName() {
        return ProviderNames.MESSAGE_BIRD.toString();
    }

    @Override
    public int getDefaultRanking() {
        return ProviderRanking.MESSAGE_BIRD;
    }

    @Nonnull
    @Override
    public SMSDeliveryReportProcessor getProcessor(@Nonnull final Map<String, String[]> parameterMap)
            throws SMSDeliveryReportParameterException {
        assert parameterMap != null;

        // Process parameters.
        final int referenceNumber = getReferenceNumber(parameterMap);
        final DeliveryStatus deliveryStatus = getDeliveryStatus(parameterMap);

        // Return a processor based on these parameters.
        return new SMSDeliveryReportProcessor() {

            @Override
            public boolean skip() {
                return deliveryStatus == null;
            }

            @Override
            public long getReferenceNumber() {
                assert deliveryStatus != null;

                return referenceNumber;
            }

            @Override
            @Nonnull
            public DeliveryStatus getDeliveryStatus() {
                assert deliveryStatus != null;

                return deliveryStatus;
            }
        };
    }

    @Nullable
    private static DeliveryStatus getDeliveryStatus(
            @Nonnull final Map<String, String[]> parameterMap)
            throws SMSDeliveryReportParameterException {
        assert parameterMap != null;

        // Obtain status from the parameter map.
        final String[] clientRef = parameterMap.get(STATUS_REPORT_PARAMETER);
        if ((clientRef != null) && (clientRef.length == 1)) {
            if (clientRef[0].equalsIgnoreCase(DELIVERED_STATUS)) {
                return DeliveryStatus.DELIVERED;
            } else if (clientRef[0].equalsIgnoreCase(BUFFERED_STATUS)) {
                return DeliveryStatus.BUFFERED;
            } else if (clientRef[0].equalsIgnoreCase(FAILED_STATUS)) {
                return DeliveryStatus.FAILED;
            } else {
                // Ignore this status.
                return null;
            }
        } else {
            LOG.error("getDeliveryStatus: Incorrect number of values. parameter={}, values={}.",
                    STATUS_REPORT_PARAMETER, clientRef);
            throw new SMSDeliveryReportParameterException(
                    "Parameter " + STATUS_REPORT_PARAMETER + " contains incorrect number of values. Values: " +
                            Json.toStringJson(clientRef));
        }
    }

    private static int getReferenceNumber(@Nonnull final Map<String, String[]> parameterMap) throws SMSDeliveryReportParameterException {
        assert parameterMap != null;

        // Obtain reference from the parameter map.
        final String[] reference = parameterMap.get(REFERENCE_PARAMETER);
        if ((reference != null) && (reference.length == 1)) {
            try {
                return Integer.parseInt(reference[0]);
            } catch (final NumberFormatException ex) {
                LOG.error("getReferenceNumber: Failed to parse parameter. parameter={}, value={}",
                        REFERENCE_PARAMETER, reference[0], ex);
                throw new SMSDeliveryReportParameterException(ex);
            }
        } else {
            LOG.error("getReferenceNumber: Incorrect number of values. parameter={}, values={}.",
                    REFERENCE_PARAMETER, reference);
            throw new SMSDeliveryReportParameterException(
                    "Parameter " + REFERENCE_PARAMETER + " contains incorrect number of values. Values: " +
                            Json.toStringJson(reference));
        }
    }
}
