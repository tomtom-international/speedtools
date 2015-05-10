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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Map;

/**
 * Implementers of this interface enable sending SMS messages.
 */
public interface SMSProviderConnector {

    /**
     * Status codes to describe the result sending the message.
     */
    public enum Status {

        /**
         * Message successfully sent.
         */
        SENT,

        /**
         * Max capacity reached, Message *NOT* sent. Can be retried after a delay.
         */
        THROTTLE,

        /**
         * Parameters incorrect. This may be message length, invalid phone number etc.
         */
        INVALID_PARAMETERS,

        /**
         * Failed to send message. Can be retried.
         */
        FAILED,

        /**
         * Service temporarily unusable (quota exceeded, etc). Prefer other providers, or even stop.  Can be retried
         * after a considerable delay (hours).
         */
        TEMPORARILY_UNAVAILABLE,

        /**
         * Service failed permanently (e.g., bad configuration). Stop using it until manual intervention.
         */
        FAILED_PERMANENTLY
    }

    /**
     * Send a text message to the given phone number.
     *
     * @param recipient       The telephone number to send to.
     * @param message         The message to send to the number.
     * @param referenceNumber An optional reference number. Used by asynchronous delivery reports.
     * @return Status.
     */
    @Nonnull
    Status sendTextMessage(@Nonnull String recipient, @Nonnull String message, @Nullable Long referenceNumber);

    /**
     * Provider name. Unique across all providers.
     *
     * @return Name.
     */
    @Nonnull
    String getProviderName();

    /**
     * All providers are assigned a number to choose between them if all other criteria (like reliability/cost etc) are
     * equal. This attribute provides the default value for this ranking.
     *
     * @return The default ranking for this provider.
     */
    int getDefaultRanking();

    /**
     * Get a delivery report processor based on the given parameterMap. This parameterMap contains all the query
     * parameters that were provided in the HTTP delivery report received from the SMS Provider. The processor is able
     * to interpret the parameters and extract SMS Delivery Report data.
     *
     * @param parameterMap The parameters from the delivery report request. For each parameter name, it provides an
     *                     array of values.
     * @return A processor that processes the given parameters.
     * @throws SMSDeliveryReportParameterException If error.
     */
    @Nonnull
    SMSDeliveryReportProcessor getProcessor(@Nonnull Map<String, String[]> parameterMap) throws SMSDeliveryReportParameterException;
}
