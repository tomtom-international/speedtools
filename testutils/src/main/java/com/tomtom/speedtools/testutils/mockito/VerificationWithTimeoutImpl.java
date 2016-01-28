/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.testutils.mockito;

import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

public class VerificationWithTimeoutImpl {

    private final VerificationMode delegate;
    private final int timeout;
    private final int treshhold;

    public VerificationWithTimeoutImpl(final int treshhold, final int millis, final VerificationMode delegate) {
        this.treshhold = treshhold;
        this.timeout = millis;
        this.delegate = delegate;
    }

    public void verify(final VerificationData data) {
        int soFar = 0;
        AssertionError error = null;
        while (soFar <= timeout) {
            //noinspection ErrorNotRethrown
            try {
                delegate.verify(data);
                return;
            } catch (final AssertionError e) {
                error = e;
                soFar += treshhold;
                sleep(treshhold);
            }
        }
        if (error != null) {
            throw error;
        }
    }

    void sleep(final int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (final InterruptedException ignored) {
            // Oops, not much luck.
        }
    }

    public VerificationMode getDelegate() {
        return delegate;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getTreshhold() {
        return treshhold;
    }
}
