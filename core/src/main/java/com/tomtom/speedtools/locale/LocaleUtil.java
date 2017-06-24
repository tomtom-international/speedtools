/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.locale;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Set;

import static com.tomtom.speedtools.objects.Immutables.setOf;

/**
 * Utility class to parse locale values, like "en", "en_GB", etc.
 */
public final class LocaleUtil {

    private static final Set<String> COUNTRIES = setOf(Locale.getISOCountries());
    private static final Set<String> LANGUAGES = setOf(Locale.getISOLanguages());

    private LocaleUtil() {
        // Class must not be instantiated.
        assert false;
    }

    /**
     * Parses the {@code locale} and converts it into a {@link Locale}. The locale should contain language and/or
     * country code (both lower and upper case country codes are supported) and optionally a variant.
     * Supported formats: "en" | "en_GB" | "en_GB_var" | "en__var" | _GB_var | _GB i.e.,
     * ll[_CC] | ll_[CC]_var | _CC[_var].
     *
     * @param locale The locale to parse.
     * @return The parsed locale.
     * @throws IllegalArgumentException When the locale cannot be parsed.
     */
    @Nonnull
    public static Locale parse(@Nonnull final String locale) {
        assert locale != null;

        if (locale.isEmpty()) {
            throw new IllegalArgumentException("Locale must not be empty");
        }

        final String language;
        final String country;
        final String variant;
        final String[] parts = locale.split("_", 3);
        switch (parts.length) {

            case 1:
                language = parts[0].toLowerCase(Locale.ENGLISH);
                validateLanguage(language);
                return new Locale(language);

            case 2:
                language = parts[0].toLowerCase(Locale.ENGLISH);
                country = parts[1].toUpperCase(Locale.ENGLISH);
                validateLanguage(language);
                validateCountry(country);
                validateLanguageOrCountryNotEmpty(language, country);
                return new Locale(language, country);

            case 3:
                language = parts[0].toLowerCase(Locale.ENGLISH);
                country = parts[1].toUpperCase(Locale.ENGLISH);
                variant = parts[2];
                validateLanguage(language);
                validateCountry(country);
                validateVariant(variant);
                validateLanguageOrCountryNotEmpty(language, country);
                return new Locale(language, country, variant);

            default:
                assert false;
                break;
        }

        assert false;
        return null;
    }

    private static void validateLanguageOrCountryNotEmpty(
            @Nonnull final String language,
            @Nonnull final String country) {
        assert language != null;
        assert country != null;

        if (language.isEmpty() && country.isEmpty()) {
            throw new IllegalArgumentException("Language or country must be set.");
        }
    }

    private static void validateLanguage(@Nonnull final String language) {
        assert language != null;

        if (language.isEmpty()) {
            return;
        }

        if (!LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Language '" + language + "' unknown");
        }
    }

    private static void validateCountry(@Nonnull final String country) {
        assert country != null;

        if (country.isEmpty()) {
            return;
        }

        if (!COUNTRIES.contains(country)) {
            throw new IllegalArgumentException("Country '" + country + "' unknown");
        }
    }

    private static void validateVariant(@Nonnull final String variant) {
        assert variant != null;

        if (variant.isEmpty()) {
            throw new IllegalArgumentException("Variant component '" + variant + "' not valid");
        }
    }
}
