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

package com.tomtom.speedtools.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;

/**
 * Utility class to deal with (house) addresses, country values, street names etc.
 */
public final class AddressUtils {

    private AddressUtils() {
        // Prevent instantiation.
    }

    /**
     * Utility function to check if a country code is a valid ISO-3166 code.
     *
     * @param countryCode Country code.
     * @return True if this is a valid ISO-3166 country code.
     */
    public static boolean isValidCountryISO2(@Nullable final String countryCode) {
        if ((countryCode == null) || (countryCode.length() != 2)) {
            return false;
        }
        final int index = Arrays.binarySearch(Locale.getISOCountries(), countryCode, (String o1, final String o2) -> o1.compareTo(o2));
        return index >= 0;
    }

    /**
     * Returns the human readable representation of a ISO-3166 country code.
     *
     * @param countryISO2 ISO-3166 code.
     * @return Readable country name, or empty if the code was invalid.
     */
    @Nonnull
    public static String getDisplayCountry(@Nonnull final String countryISO2) {
        assert countryISO2 != null;
        return getDisplayCountry(countryISO2, Locale.ENGLISH);
    }

    /**
     * Returns the human readable representation of a ISO-3166 country code in a specific language.
     *
     * @param countryISO2 ISO-3166 code.
     * @param language    Language code.
     * @return Readable country name, or empty if the code was invalid.
     */
    @Nonnull
    public static String getDisplayCountry(@Nonnull final String countryISO2, @Nonnull final Locale language) {
        assert countryISO2 != null;
        if (!isValidCountryISO2(countryISO2)) {
            return "";
        }
        final Locale locale = new Locale(language.getLanguage(), countryISO2);
        return locale.getDisplayCountry(language);
    }

    @Nonnull
    public static String formatStreetWithHouseNumber(
            @Nonnull final Locale locale,
            @Nonnull final String street,
            @Nullable final Integer houseNumber,
            @Nullable final String houseNumberPostfix) {
        assert street != null;

        // Add the house number.
        final String fullNumber = ((houseNumber == null) ? "" : houseNumber) +
                (((houseNumberPostfix == null) || houseNumberPostfix.isEmpty()) ? "" :
                        (((houseNumber == null) ? "" : '-') + houseNumberPostfix));

        final String result;
        if (fullNumber.isEmpty()) {
            result = street;
        } else {

            if (isEnglish(locale)) {
                result = fullNumber + ' ' + street;
            } else {
                result = street + ' ' + fullNumber;
            }
        }
        return result;
    }

    @Nonnull
    public static String formatPostcalCodeAndCity(
            @Nonnull final Locale locale,
            @Nonnull final String postalCode,
            @Nonnull final String city) {
        assert postalCode != null;
        assert city != null;

        final String result;
        if (city.isEmpty()) {
            result = postalCode;
        } else {
            if (postalCode.isEmpty()) {
                result = city;
            } else {
                if (isEnglish(locale)) {
                    result = city + ", " + postalCode;
                } else {
                    result = postalCode + "  " + city;
                }
            }
        }
        return result;
    }

    private static boolean isEnglish(@Nonnull final Locale locale) {
        final boolean english;
        final String language = locale.getLanguage();
        final String country = locale.getCountry();
        if ((language != null) && !language.isEmpty()) {

            // Check the locale language.
            english = language.equals(Locale.ENGLISH.getLanguage());
        } else {

            if ((country != null) && !country.isEmpty()) {

                // No language set, check the country instead.
                english =
                        country.equals(Locale.US.getCountry()) ||
                                country.equals(Locale.UK.getCountry()) ||
                                country.equals(Locale.CANADA.getCountry());
            } else {
                english = false;
            }
        }
        return english;
    }
}
