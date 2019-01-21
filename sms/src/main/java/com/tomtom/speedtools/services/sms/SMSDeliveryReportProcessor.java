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

package com.tomtom.speedtools.services.sms;

import com.tomtom.speedtools.services.sms.SMSDeliveryReportListener.DeliveryStatus;

import javax.annotation.Nonnull;

/**
 * This processor is a strategy for processing delivery reports. An implementation is provided by every
 * SMSProviderConnector, allowing for SMSProvider specific processing.
 */
public interface SMSDeliveryReportProcessor {

    /**
     * Indicates whether or not to continue with this particular report. For instance, an SMSProvider might deliver
     * intermediate reports that do not lead to an action on the back-end side. If skip is true, the other methods on
     * this processor must not be called.
     *
     * @return True to skip this report.
     */
    boolean skip();

    /**
     * Get the reference number for the delivery report. The reference number was passed to the SMSProvider when sending
     * the message. The back-end ensures that sms messages are uniquely numbered (&gt; 0).
     *
     * @return The reference number.
     * @throws SMSDeliveryReportParameterException If error.
     */
    @SuppressWarnings("RedundantThrows")
    long getReferenceNumber() throws SMSDeliveryReportParameterException;

    /**
     * Get the delivery status for the delivery report.
     *
     * @return The delivery status.
     * @throws SMSDeliveryReportParameterException If error.
     */
    @SuppressWarnings("RedundantThrows")
    @Nonnull
    DeliveryStatus getDeliveryStatus() throws SMSDeliveryReportParameterException;
}
