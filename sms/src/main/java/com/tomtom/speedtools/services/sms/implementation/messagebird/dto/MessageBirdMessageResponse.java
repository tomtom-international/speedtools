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

package com.tomtom.speedtools.services.sms.implementation.messagebird.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tomtom.speedtools.apivalidation.ApiDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;

import static com.tomtom.speedtools.utils.StringUtils.trim;

/**
 * Response from the MessageBird REST API.
 */
// Ignore unexpected properties because interface specification is not under our control.
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "response")
public class MessageBirdMessageResponse extends ApiDTO {

    @Nullable
    private Item item;

    // Normally unused (required for mock and JAX-B).
    public MessageBirdMessageResponse() {
        super();
    }

    @Override
    public void validate() {
        validator().start();
        validator().checkNotNullAndValidate(true, "item", item);
        validator().done();
    }

    @XmlElement(name = "item", required = true)
    @Nonnull
    public Item getItem() {
        beforeGet();
        assert item != null;
        return item;
    }

    public void setItem(@Nullable final Item item) {
        beforeSet();
        this.item = item;
    }

    @XmlEnum
    public enum ResponseCode {

        // Request successfully processed.
        @XmlEnumValue("01")
        REQUEST_SUCCESSFUL,

        // The message cannot be scheduled at this date (and time).
        @XmlEnumValue("69")
        DATE_NOT_ALLOWED,

        // Error in timestamp syntax.
        @XmlEnumValue("70")
        INVALID_TIMESTAMP,

        // The timestamp is in the past.
        @XmlEnumValue("71")
        TIMESTAMP_IN_PAST,

        // The message is too long.
        @XmlEnumValue("72")
        MESSAGE_TOO_LONG,

        // Invalid sender.
        @XmlEnumValue("89")
        INVALID_SENDER,

        // One or more destinations are invalid.
        @XmlEnumValue("93")
        INVALID_DESTINATION,

        // No, or empty, body.
        @XmlEnumValue("95")
        EMPTY_BODY,

        // You do not have enough credits.
        @XmlEnumValue("96")
        INSUFFICIENT_CREDITS,

        // Invalid username or password.
        @XmlEnumValue("97")
        INVALID_USERNAME_PASSWORD,

        // Your ip address is not allowed with this account.
        @XmlEnumValue("98")
        IP_NOT_ALLOWED,

        // Cannot connect to server. (Internal error at MessageBird.)
        @XmlEnumValue("99")
        CANNOT_CONNECT,
    }

    @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
    public static class Item extends ApiDTO {

        @Nullable
        private ResponseCode responseCode;
        @Nullable
        private String responseMessage;
        @Nullable
        private String credits;

        // Normally unused (required for mock and JAX-B).
        public Item() {
            super();
        }

        @Override
        public void validate() {
            validator().start();
            validator().checkNotNull(true, "responseCode", responseCode);
            validator().checkNotNull(true, "responseMessage", responseMessage);
            validator().done();
        }

        @XmlElement(name = "responseCode", required = true)
        @Nonnull
        public ResponseCode getResponseCode() {
            beforeGet();
            assert responseCode != null;
            return responseCode;
        }

        public void setResponseCode(@Nullable final ResponseCode responseCode) {
            beforeSet();
            this.responseCode = responseCode;
        }

        @XmlElement(name = "responseMessage", required = true)
        @Nonnull
        public String getResponseMessage() {
            beforeGet();
            assert responseMessage != null;
            return responseMessage;
        }

        public void setResponseMessage(@Nullable final String responseMessage) {
            beforeSet();
            this.responseMessage = trim(responseMessage);
        }

        @XmlElement(name = "credits", required = false)
        @Nullable
        public String getCredits() {
            beforeGet();
            return credits;
        }

        public void setCredits(@Nullable final String credits) {
            beforeSet();
            this.credits = trim(credits);
        }
    }
}
