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

package com.tomtom.speedtools.services.sms.implementation;

import javax.annotation.Nonnull;

/**
 * Defines a name for each SMS provider. The enum.toString() is used as the value.
 */
public enum ProviderNames {

    /**
     * www.nexmo.com
     */
    NEXMO("NEXMO"),

    /**
     * www.messagebird.com
     */
    MESSAGE_BIRD("MESSAGE_BIRD");

    @Nonnull
    private final String name;

    private ProviderNames(@Nonnull final String name) {
        assert name != null;

        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
