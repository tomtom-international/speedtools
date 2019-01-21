/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exception used to report errors from the SMSDeliveryReportProcessor.
 */
public class SMSDeliveryReportParameterException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an exception with a cause.
     *
     * @param cause The cause of the exception. It can be null.
     */
    public SMSDeliveryReportParameterException(@Nullable final Exception cause) {
        super(cause);
    }

    /**
     * Constructs an exception with a message.
     *
     * @param message The message of the exception.
     */
    public SMSDeliveryReportParameterException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    /**
     * Constructs an exception with a message and a cause.
     *
     * @param message The message of the exception.
     * @param cause   The cause of the exception. It can be null.
     */
    public SMSDeliveryReportParameterException(@Nonnull final String message, @Nullable final Exception cause) {
        super(message, cause);
        assert message != null;
    }
}
