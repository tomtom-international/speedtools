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

/**
 * Called when a delivery report is received from the SMS provider.
 */
public interface SMSDeliveryReportListener {

    public enum DeliveryStatus {

        /**
         * The message was successfully delivered to the recipient.
         */
        DELIVERED,

        /**
         * The message was held up by a provider, and has not (yet) been delivered.
         */
        BUFFERED,

        /**
         * The message has not (and will not) be delivered.
         */
        FAILED
    }

    /**
     * Report on the delivery of a message.
     *
     * @param referenceNumber The reference number of the message, provided when the message was sent (&gt; 0).
     * @param newStatus       The received status.
     */
    void messageDeliveryReport(long referenceNumber, @Nonnull DeliveryStatus newStatus);
}
