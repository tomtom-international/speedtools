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

package com.tomtom.speedtools.services.push.implementation.apns;


import com.tomtom.speedtools.services.push.PushConnectionException;

import javax.annotation.Nonnull;

public class APNSGatewayConnectionException extends PushConnectionException {

    public APNSGatewayConnectionException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }

    public APNSGatewayConnectionException(@Nonnull final Exception exception) {
        super(exception);
        assert exception != null;
    }

    public APNSGatewayConnectionException(@Nonnull final String message, @Nonnull final Exception exception) {
        super(message, exception);
        assert message != null;
        assert exception != null;
    }
}
