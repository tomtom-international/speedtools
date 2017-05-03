/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.apivalidation;

import com.tomtom.speedtools.domain.ColorConverter;

/**
 * Utility class with constants for interface time-outs.
 */
public final class ApiValidationConstants {

    // Prevent instantiation.
    private ApiValidationConstants() {
        super();
        assert false;
    }

    public static final int API_BIC_MAX_LENGTH = 11;
    public static final int API_BIC_MIN_LENGTH = 8;
    public static final int API_COLOR_MIN_LENGTH = ColorConverter.COLOR_MIN_LENGTH;
    public static final int API_COLOR_MAX_LENGTH = ColorConverter.COLOR_MAX_LENGTH;
    public static final int API_CURRENCY_ISO3_MAX_LENGTH = 3;
    public static final int API_CURRENCY_ISO3_MIN_LENGTH = 3;
    public static final int API_COUNTRY_ISO2_MAX_LENGTH = 2;
    public static final int API_COUNTRY_ISO2_MIN_LENGTH = 2;
    public static final int API_EMAIL_ADDRESS_MAX_LENGTH = 50;
    public static final int API_EMAIL_ADDRESS_MIN_LENGTH = 5;
    public static final int API_IBAN_MAX_LENGTH = 34;
    public static final int API_IBAN_MIN_LENGTH = 5;
    public static final int API_TELEPHONE_NUMBER_MAX_LENGTH = 20;
    public static final int API_TELEPHONE_NUMBER_MIN_LENGTH = 9;
    public static final int API_TIME_ZONE_MAX_LENGTH = 32;
    public static final int API_TIME_ZONE_MIN_LENGTH = 0;
}
