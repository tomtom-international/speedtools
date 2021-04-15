/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.services.sms.implementation.nexmo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tomtom.speedtools.apivalidation.ApiDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;

import static com.tomtom.speedtools.utils.StringUtils.trim;

// Ignore unexpected properties because interface specification is not under our control.
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "trip")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class NexmoMessage extends ApiDTO {

    @XmlEnum
    public enum Status {

        /**
         * The message was successfully accepted for delivery by Nexmo.
         */
        @XmlEnumValue("0")
        SUCCESS,

        /**
         * You have exceeded the submission capacity allowed on this account, please back-off and retry.
         */
        @XmlEnumValue("1")
        THROTTLED,

        /**
         * Your request is incomplete and missing some mandatory parameters.
         */
        @XmlEnumValue("2")
        MISSING_PARAMS,

        /**
         * The value of one or more parameters is invalid.
         */
        @XmlEnumValue("3")
        INVALID_PARAMS,

        /**
         * The username / password you supplied is either invalid or disabled.
         */
        @XmlEnumValue("4")
        INVALID_CREDENTIALS,

        /**
         * An error has occurred in the nexmo platform whilst processing this message.
         */
        @XmlEnumValue("5")
        INTERNAL_ERROR,

        /**
         * The Nexmo platform was unable to process this message, for example, an un-recognized number prefix.
         */
        @XmlEnumValue("6")
        INVALID_MESSAGE,

        /**
         * The number you are trying to submit to is blacklisted and may not receive messages.
         */
        @XmlEnumValue("7")
        NUMBER_BARRED,

        /**
         * The username you supplied is for an account that has been barred from submitting messages.
         */
        @XmlEnumValue("8")
        PARTNER_ACCOUNT_BARRED,

        /**
         * Your pre-pay account does not have sufficient credit to process this message.
         */
        @XmlEnumValue("9")
        PARTNER_QUOTA_EXCEEDED,

        /**
         * The number of simultaneous connections to the platform exceeds the capabilities of your account.
         */
        @XmlEnumValue("10")
        TOO_MANY_EXISTING_BINDS,

        /**
         * This account is not provisioned for REST submission, you should use SMPP instead.
         */
        @XmlEnumValue("11")
        ACCOUNT_NOT_ENABLED_FOR_REST,

        /**
         * Applies to Binary submissions, where the length of the UDH and the message body combined exceed 140 octets.
         */
        @XmlEnumValue("12")
        MESSAGE_TOO_LONG,

        /**
         * The sender address (from parameter) was not allowed for this message. Restrictions may apply depending on the
         * destination see our FAQs
         */
        @XmlEnumValue("15")
        INVALID_SENDER_ADDRESS,

        /**
         * The ttl parameter values is invalid.
         */
        @XmlEnumValue("16")
        INVALID_TTL
    }

    @Nullable
    private Status status;
    @Nullable
    private String messageId;
    @Nullable
    private String to;
    @Nullable
    private String clientRef;
    @Nullable
    private String remainingBalance;
    @Nullable
    private String messagePrice;
    @Nullable
    private String network;
    @Nullable
    private String errorText;

    @Override
    public void validate() {
        validator().start();
        validator().checkNotNull(true, "status", status);
        validator().checkNotNull(false, "message-id", messageId);
        validator().checkNotNull(false, "to", to);
        validator().checkNotNull(false, "client-ref", clientRef);
        validator().checkNotNull(false, "remaining-balance", remainingBalance);
        validator().checkNotNull(false, "message-price", messagePrice);
        validator().checkNotNull(false, "network", network);
        validator().checkNotNull(false, "error-text", errorText);
        validator().done();
    }

    @XmlElement(name = "status", required = true)
    @Nonnull
    public Status getStatus() {
        beforeGet();
        assert status != null;
        return status;
    }

    public void setStatus(@Nullable final Status status) {
        beforeSet();
        this.status = status;
    }

    @XmlElement(name = "message-id", required = false)
    @Nullable
    public String getMessageId() {
        beforeGet();
        return messageId;
    }

    public void setMessageId(@Nullable final String messageId) {
        beforeSet();
        this.messageId = trim(messageId);
    }

    @XmlElement(name = "to", required = false)
    @Nullable
    public String getTo() {
        beforeGet();
        return to;
    }

    public void setTo(@Nullable final String to) {
        beforeSet();
        this.to = trim(to);
    }

    @XmlElement(name = "client-ref", required = false)
    @Nullable
    public String getClientRef() {
        beforeGet();
        return clientRef;
    }

    public void setClientRef(@Nullable final String clientRef) {
        beforeSet();
        this.clientRef = trim(clientRef);
    }

    @XmlElement(name = "remaining-balance", required = false)
    @Nullable
    public String getRemainingBalance() {
        beforeGet();
        return remainingBalance;
    }

    public void setRemainingBalance(@Nullable final String remainingBalance) {
        beforeSet();
        this.remainingBalance = trim(remainingBalance);
    }

    @XmlElement(name = "message-price", required = false)
    @Nullable
    public String getMessagePrice() {
        beforeGet();
        return messagePrice;
    }

    public void setMessagePrice(@Nullable final String messagePrice) {
        beforeSet();
        this.messagePrice = trim(messagePrice);
    }

    @XmlElement(name = "network", required = false)
    @Nullable
    public String getNetwork() {
        beforeGet();
        return network;
    }

    public void setNetwork(@Nullable final String network) {
        beforeSet();
        this.network = trim(network);
    }

    @XmlElement(name = "error-text", required = false)
    @Nullable
    public String getErrorText() {
        beforeGet();
        return errorText;
    }

    public void setErrorText(@Nullable final String errorText) {
        beforeSet();
        this.errorText = trim(errorText);
    }
}
