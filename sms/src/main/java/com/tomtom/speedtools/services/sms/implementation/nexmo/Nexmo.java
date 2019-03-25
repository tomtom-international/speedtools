/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.services.sms.implementation.nexmo;

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportListener.DeliveryStatus;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportParameterException;
import com.tomtom.speedtools.services.sms.SMSDeliveryReportProcessor;
import com.tomtom.speedtools.services.sms.SMSProviderConnector;
import com.tomtom.speedtools.services.sms.implementation.ProviderNames;
import com.tomtom.speedtools.services.sms.implementation.ProviderRanking;
import com.tomtom.speedtools.services.sms.implementation.nexmo.dto.NexmoMessage;
import com.tomtom.speedtools.services.sms.implementation.nexmo.dto.NexmoMessageResponse;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Provides SMS functionality through Nexmo (www.nexmo.com).
 *
 * The API documentation can be found here: http://www.nexmo.com/documentation
 */
public class Nexmo implements SMSProviderConnector {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(Nexmo.class);

    private static final String STATUS_REPORT_REQUIRED = "1";
    private static final String CLIENT_REF_PARAMETER = "client-ref";
    private static final String STATUS_REPORT_PARAMETER = "status";
    private static final String DELIVERED_STATUS = "delivered";
    private static final String BUFFERED_STATUS = "buffered";
    private static final String FAILED_STATUS = "failed";
    private static final String TYPE = "text";

    @Nonnull
    private final String userName;
    @Nonnull
    private final String password;
    @Nonnull
    private final String sender;
    @Nonnull
    private final String baseUrl;

    /**
     * Constructor.
     *
     * @param nexmoProperties Properties for Nexmo.
     */
    @Inject
    public Nexmo(@Nonnull final NexmoProperties nexmoProperties) {
        assert nexmoProperties != null;

        userName = nexmoProperties.getUserName();
        password = nexmoProperties.getPassword();
        sender = nexmoProperties.getSender();
        baseUrl = nexmoProperties.getBaseUrl();
    }

    @Nonnull
    @Override
    public Status sendTextMessage(
            @Nonnull final String recipient,
            @Nonnull final String message,
            @Nullable final Long referenceNumber) {
        assert recipient != null;
        assert message != null;

        final ResteasyClient client = new ResteasyClientBuilder().build();
        final ResteasyWebTarget target = client.target(baseUrl);
        final NexmoResource proxy = target.proxy(NexmoResource.class);
        Response response = null;
        try {

            // Send the message.
            LOG.debug("sendTextMessage: recipient={}, ref={}, message: {}",
                    recipient, referenceNumber, message);
            response = proxy.sendMessage(userName, password, sender, recipient, TYPE, message, STATUS_REPORT_REQUIRED,
                    referenceNumber);
            LOG.debug("sendTextMessage: response={}", Json.toStringJson(response));

            // Process the response.
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final NexmoMessageResponse nexmoMessageResponse = (NexmoMessageResponse) response.getEntity();

                // Process messages in the response. Nexmo supports splitting into multiple messages, but we do not.
                for (final NexmoMessage nexmoMessage : nexmoMessageResponse.getMessages()) {
                    switch (nexmoMessage.getStatus()) {

                        case SUCCESS:
                            return Status.SENT;

                        case THROTTLED:
                            return Status.THROTTLE;

                        case MISSING_PARAMS:
                            LOG.error(
                                    "sendTextMessage: Missing parameters. error={}, recipient={}, message={}, ref={}.",
                                    nexmoMessage.getErrorText(), recipient, message, referenceNumber);
                            return Status.FAILED_PERMANENTLY;

                        case INVALID_PARAMS:
                            LOG.warn(
                                    "sendTextMessage: Invalid parameters. error={}, recipient={}, message={}, ref={}.",
                                    nexmoMessage.getErrorText(), recipient, message, referenceNumber);
                            return Status.INVALID_PARAMETERS;

                        case INVALID_CREDENTIALS:
                            LOG.error("sendTextMessage: Invalid credentials. error={}, userName: {}.",
                                    nexmoMessage.getErrorText(), userName);
                            return Status.FAILED_PERMANENTLY;

                        case INTERNAL_ERROR:
                            LOG.warn(
                                    "sendTextMessage: Internal error at Nexmo. error={}, recipient={}, message={}, ref={}.",
                                    nexmoMessage.getErrorText(), recipient, message);
                            return Status.FAILED;

                        case INVALID_MESSAGE:
                            LOG.info(
                                    "sendTextMessage: Invalid message. error={}, recipient={}, message={}, ref={}.",
                                    nexmoMessage.getErrorText(), recipient, message, referenceNumber);
                            return Status.INVALID_PARAMETERS;

                        case NUMBER_BARRED:
                            LOG.warn("sendTextMessage: Number barred. error={}, recipient={}.",
                                    nexmoMessage.getErrorText(), recipient);
                            return Status.INVALID_PARAMETERS;


                        case PARTNER_ACCOUNT_BARRED:
                            LOG.warn("sendTextMessage: Partner account barred. error={}.",
                                    nexmoMessage.getErrorText());
                            return Status.TEMPORARILY_UNAVAILABLE;


                        case PARTNER_QUOTA_EXCEEDED:
                            LOG.warn("sendTextMessage: Partner quota exceeded. error={}.",
                                    nexmoMessage.getErrorText());
                            return Status.TEMPORARILY_UNAVAILABLE;

                        case TOO_MANY_EXISTING_BINDS:
                            LOG.warn("sendTextMessage: Too many connections. error={}.",
                                    nexmoMessage.getErrorText());
                            return Status.THROTTLE;

                        case ACCOUNT_NOT_ENABLED_FOR_REST:
                            LOG.warn("sendTextMessage: Account not enabled for REST. Error={}.",
                                    nexmoMessage.getErrorText());
                            return Status.TEMPORARILY_UNAVAILABLE;

                        case MESSAGE_TOO_LONG:
                            LOG.info("sendTextMessage: Message too long. error={}, recipient={}, message={}, ref={}.",
                                    nexmoMessage.getErrorText(), recipient, message, referenceNumber);
                            return Status.INVALID_PARAMETERS;

                        case INVALID_SENDER_ADDRESS:
                            LOG.error("sendTextMessage: Invalid sender address. error={}, sender={}.",
                                    nexmoMessage.getErrorText(), sender);
                            return Status.FAILED_PERMANENTLY;

                        case INVALID_TTL:
                            LOG.error("sendTextMessage: Invalid TTL. error={}", nexmoMessage.getErrorText());
                            return Status.FAILED_PERMANENTLY;

                        default:
                            LOG.error("sendTextMessage: Unknown status code. error={}", nexmoMessage.getErrorText());
                            assert false;
                            break;
                    }
                }
            }
        } catch (final Throwable t) {
            // Resteasy throws several runtime exceptions, but it is not exactly clear which.
            LOG.error("sendTextMessage: Failed to send message to Nexmo. recipient={}, message={}, ref={}.",
                    recipient, message, referenceNumber, t);
        } finally {
            if (response != null) {
                // Always release the connection.
                response.close();
            }
        }

        return Status.FAILED;
    }

    @Nonnull
    @Override
    public String getProviderName() {
        return ProviderNames.NEXMO.toString();
    }

    @Override
    public int getDefaultRanking() {
        return ProviderRanking.NEXMO;
    }

    @Override
    @Nonnull
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
            @Nonnull final Map<String, String[]> parameterMap) throws SMSDeliveryReportParameterException {
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

    private static int getReferenceNumber(@Nonnull final Map<String, String[]> parameterMap)
            throws SMSDeliveryReportParameterException {
        assert parameterMap != null;

        // Obtain reference from the parameter map.
        final String[] clientRef = parameterMap.get(CLIENT_REF_PARAMETER);
        if ((clientRef != null) && (clientRef.length == 1)) {
            try {
                return Integer.parseInt(clientRef[0]);
            } catch (final NumberFormatException ex) {
                LOG.error("getReferenceNumber: Failed to parse parameter. parameter={}, value={}",
                        CLIENT_REF_PARAMETER, clientRef[0], ex);
                throw new SMSDeliveryReportParameterException(ex);
            }
        } else {
            LOG.error("getReferenceNumber: Incorrect number of values. parameter={}, values={}.",
                    CLIENT_REF_PARAMETER, clientRef);
            throw new SMSDeliveryReportParameterException(
                    "Parameter " + CLIENT_REF_PARAMETER + " contains incorrect number of values. Values: " +
                            Json.toStringJson(clientRef));
        }
    }
}
