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

package com.tomtom.speedtools.apivalidation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.tomtom.speedtools.apivalidation.errors.*;
import com.tomtom.speedtools.apivalidation.exceptions.ApiBadRequestException;
import com.tomtom.speedtools.domain.ColorConverter;
import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.locale.LocaleUtil;
import com.tomtom.speedtools.utils.MathUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * The class WebParameterValidator implements a simple way to validate monitoring parameters and throw a BAD_REQUEST
 * (HTTP status code 400) is a parameter is not ok.
 */
public final class ApiValidator {
    private static final Logger LOG = LoggerFactory.getLogger(ApiValidator.class);

    @Nonnull
    private final static String FORMAT_DATE_ISO = "yyyy-MM-dd";

    private enum Status {
        NOT_STARTED, IN_PROGRESS, FINISHED
    }

    @Nonnull
    private final List<ApiValidationError> errors = new ArrayList<>();
    private Status status = Status.NOT_STARTED;

    /**
     * Indicate start of validation.
     */
    public void start() {
        status = Status.IN_PROGRESS;
    }

    /**
     * Indicate end of validation.
     */
    public void done() throws ApiBadRequestException {
        status = Status.FINISHED;
        throwIfExceptions();
    }

    /**
     * Reset the validator. This should only be used if you must use a setter on an object
     * after the validation has run, e.g. to modify your DTO element after reading it.
     * You should manually called validate() after setting the attributes.
     */
    public void reset() {
        errors.clear();
        status = Status.NOT_STARTED;
    }

    public void checkInteger(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Integer value,
            final int min,
            final int max) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value, min, max)) {
                errors.add(new ApiIntegerOutOfRangeError(name, value, min, max));
            }
        }
    }

    public void checkLong(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Long value,
            final long min,
            final long max) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value, min, max)) {
                errors.add(new ApiLongOutOfRangeError(name, value, min, max));
            }
        }
    }

    /**
     * Check a double value.
     *
     * @param required                Specifies whether value may be null or not.
     * @param name                    Name of value.
     * @param value                   Actual value.
     * @param min                     Minimum value.
     * @param max                     Maximum value.
     * @param positiveInfinityAllowed The value of positiveInfinityAllowed indicates whether or not {@link
     *                                Double#POSITIVE_INFINITY} is allowed as a value or not. If positiveInfinityAllowed
     *                                is false, the value must be in range [min, max] (inclusive). If
     *                                positiveInfinityAllowed is true, the value must either be in range [min, max] or
     *                                may be {@link Double#POSITIVE_INFINITY}. Note that {@link
     *                                Double#NEGATIVE_INFINITY} is never allowed.
     */
    public void checkDouble(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Double value,
            final double min,
            final double max,
            final boolean positiveInfinityAllowed) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);

        if (value != null) {
            if (!MathUtils.isBetween(value, min, max)) {
                if (!positiveInfinityAllowed || !Double.isInfinite(value)) {
                    errors.add(new ApiDoubleOutOfRangeError(name, value, min, max));
                }
            }
        }
    }

    public void checkTrue(
            @Nonnull final String name,
            final boolean value) {
        assert name != null;
        assertNotFinished();
        if (!value) {
            errors.add(new ApiParameterSyntaxError(name, Boolean.FALSE.toString(), Boolean.TRUE.toString()));
        }
    }

    public void checkFalse(
            @Nonnull final String name,
            final boolean value) {
        assert name != null;
        assertNotFinished();
        if (value) {
            errors.add(new ApiParameterSyntaxError(name, Boolean.TRUE.toString(), Boolean.FALSE.toString()));
        }
    }

    public void checkNull(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Object value) {
        assert name != null;
        assertNotFinished();
        if ((value != null) && required) {
            errors.add(new ApiParameterSyntaxError(name, value.toString(), "null"));
        }
    }

    public void checkNotNull(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Object value) {
        assert name != null;
        assertNotFinished();
        if ((value == null) && required) {
            errors.add(new ApiParameterMissingError(name));
        }
    }

    public void checkNotNullAndValidate(
            final boolean required,
            @Nonnull final String name,
            @Nullable final ApiDTO value) {
        assert name != null;
        checkNotNull(required, name, value);
        if (value != null) {
            value.validate();
        }
    }

    public void checkNotNullAndValidateAll(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Collection<? extends ApiDTO> value) {
        assert name != null;
        checkNotNull(required, name, value);
        if (value != null) {
            for (final ApiDTO binder : value) {
                checkNotNull(true, name, binder);
                binder.validate();
            }
        }
    }

    public <T> void checkNotNullAndValidateEnum(
            final boolean required,
            @Nonnull final String name,
            @Nullable final T value) {
        assert name != null;
        assert (value == null) || value.getClass().isEnum();
        checkNotNull(required, name, value);
    }

    public void checkAllowedValues(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value,
            @Nonnull final String... allowedValues) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            boolean found = false;
            for (final String allowedValue : allowedValues) {
                if (allowedValue.equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                final StringBuilder sb = new StringBuilder();
                for (final String allowedValue : allowedValues) {
                    sb.append('|');
                    sb.append(allowedValue);
                }
                errors.add(new ApiParameterSyntaxError(name, value, sb.toString()));
            }
        }
    }

    public void checkString(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value,
            final int minLength,
            final int maxLength) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value.length(), minLength, maxLength)) {
                errors.add(new ApiStringLengthError(name, value.length(), minLength, maxLength));
            }
        }
    }

    public void checkCountryISO2(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value.length(), ApiValidationConstants.API_COUNTRY_ISO2_MIN_LENGTH,
                    ApiValidationConstants.API_COUNTRY_ISO2_MAX_LENGTH)) {
                errors.add(new ApiStringLengthError(name, value.length(),
                        ApiValidationConstants.API_COUNTRY_ISO2_MIN_LENGTH,
                        ApiValidationConstants.API_COUNTRY_ISO2_MAX_LENGTH));
            }

            // Check if the code is known.
            boolean found = false;
            for (final String country : Locale.getISOCountries()) {
                if (country.equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                final StringBuilder sb = new StringBuilder();
                for (final String country : Locale.getISOCountries()) {
                    sb.append('|');
                    sb.append(country);
                }
                errors.add(new ApiParameterSyntaxError(name, value, sb.toString()));
            }
        }
    }

    public void checkCurrencyISO3(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value.length(), ApiValidationConstants.API_CURRENCY_ISO3_MIN_LENGTH,
                    ApiValidationConstants.API_CURRENCY_ISO3_MAX_LENGTH)) {
                errors.add(new ApiStringLengthError(name, value.length(),
                        ApiValidationConstants.API_CURRENCY_ISO3_MIN_LENGTH,
                        ApiValidationConstants.API_CURRENCY_ISO3_MAX_LENGTH));
            }

            // Get all currency codes.
            final Set<Currency> currencies = new HashSet<>();
            for (final Locale locale : Locale.getAvailableLocales()) {
                try {
                    final Currency currency = Currency.getInstance(locale);
                    currencies.add(currency);
                    String iso3Country = "___";
                    //noinspection NestedTryStatement
                    try {
                        iso3Country = locale.getISO3Country();
                    } catch (final MissingResourceException ignored) {
                        // Ignore.
                    }
                    String iso3Language = "___";
                    //noinspection NestedTryStatement
                    try {
                        iso3Language = locale.getISO3Language();
                    } catch (final MissingResourceException ignored) {
                        // Ignore.
                    }
                    LOG.trace("checkCurrencyISO3: {}-{}: {}", iso3Country, iso3Language, currency.getCurrencyCode());
                } catch (final IllegalArgumentException ignored) {
                    // Locale not found, OK to ignore.
                }
            }

            // Check if the code is known.
            boolean found = false;
            for (final Currency currency : currencies) {
                if (currency.getCurrencyCode().equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                final StringBuilder sb = new StringBuilder();
                for (final Currency currency : currencies) {
                    sb.append('|');
                    sb.append(currency.getCurrencyCode());
                }
                errors.add(new ApiParameterSyntaxError(name, value, sb.toString()));
            }
        }
    }

    public void checkEmailAddress(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value.length(), ApiValidationConstants.API_EMAIL_ADDRESS_MIN_LENGTH,
                    ApiValidationConstants.API_EMAIL_ADDRESS_MAX_LENGTH)) {
                errors.add(new ApiStringLengthError(name, value.length(),
                        ApiValidationConstants.API_EMAIL_ADDRESS_MIN_LENGTH,
                        ApiValidationConstants.API_EMAIL_ADDRESS_MAX_LENGTH));
            }
            try {
                final InternetAddress emailAddress = new InternetAddress(value);
                emailAddress.validate();
            } catch (final AddressException e) {
                errors.add(
                        new ApiParameterSyntaxError(name, value, "name@domain.tld (RFC2822 compliant)", e.getMessage()));
            }
        }
    }

    public void checkTelephoneNumber(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value.length(), ApiValidationConstants.API_TELEPHONE_NUMBER_MIN_LENGTH,
                    ApiValidationConstants.API_TELEPHONE_NUMBER_MAX_LENGTH)) {
                errors.add(new ApiStringLengthError(name, value.length(),
                        ApiValidationConstants.API_TELEPHONE_NUMBER_MIN_LENGTH,
                        ApiValidationConstants.API_TELEPHONE_NUMBER_MAX_LENGTH));
            }
            try {
                PhoneNumberUtil.getInstance().parse(value, null);
            } catch (final NumberParseException e) {
                errors.add(new ApiParameterSyntaxError(name, value, "+312012345678", e.getMessage()));
            }
        }
    }

    public void checkTimeZone(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!MathUtils.isBetween(value.length(), ApiValidationConstants.API_TIME_ZONE_MIN_LENGTH,
                    ApiValidationConstants.API_TIME_ZONE_MAX_LENGTH)) {
                errors.add(new ApiStringLengthError(name, value.length(),
                        ApiValidationConstants.API_TIME_ZONE_MIN_LENGTH,
                        ApiValidationConstants.API_TIME_ZONE_MAX_LENGTH));
            }
        }
    }

    public void checkLocale(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            try {
                LocaleUtil.parse(value);
            } catch (final IllegalArgumentException e) {
                errors.add(
                        new ApiParameterSyntaxError(name, value, "ll[_CC] | ll_[CC]_var | _CC[_var]", e.getMessage()));
            }
        }
    }

    public void checkUrl(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            boolean ok = false;
            try {
                final URL url = new URL(value);
                assert url != null;
                ok = true;
            } catch (final MalformedURLException ignored) {

            }
            if (!ok) {
                errors.add(new ApiParameterSyntaxError(name, name, "url"));
            }
        }
    }

    public void checkDate(
            final boolean required,
            @Nonnull final String name,
            @Nullable final DateTime value,
            @Nonnull final DateTime minDate,
            @Nonnull final DateTime maxDate) {
        assert name != null;
        assert minDate != null;
        assert maxDate != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (value.isBefore(minDate) || value.isAfter(maxDate)) {
                errors.add(new ApiDateOutOfRangeError(name, value, minDate, maxDate));
            }
        }
    }

    public void checkDate(
            final boolean required,
            @Nonnull final String name,
            @Nullable final Date value,
            @Nonnull final DateTime minDate,
            @Nonnull final DateTime maxDate) {
        checkDate(required, name, new DateTime(value), minDate, maxDate);
    }

    public void checkLocalDate(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value,
            @Nonnull final LocalDate minDate,
            @Nonnull final LocalDate maxDate) {
        checkNotNull(required, name, value);
        if (value != null) {
            try {
                final LocalDate localDate = LocalDate.parse(value);
                // Check range.
                if (localDate.isBefore(minDate) || localDate.isAfter(maxDate)) {
                    errors.add(new ApiLocalDateOutOfRangeError(name, localDate, minDate, maxDate));
                }
            } catch (final IllegalArgumentException ignored) {
                errors.add(new ApiLocalDateFormatError(name, value, FORMAT_DATE_ISO));
            }
        }
    }

    public void checkUid(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            if (!Uid.isValid(value)) {
                errors.add(new ApiUidSyntaxError(name, value));
            }
        }
    }

    public void checkColor(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);
        if (value != null) {
            try {
                ColorConverter.toColorRGB(value);
            } catch (final IllegalArgumentException ignored) {
                errors.add(new ApiColorFormatError(name, value));
            }
        }
    }

    public void checkIBAN(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);

        if (value != null) {
            if ((value.length() < ApiValidationConstants.API_IBAN_MIN_LENGTH) ||
                    (value.length() > ApiValidationConstants.API_IBAN_MAX_LENGTH)) {
                errors.add(new ApiIBANFormatError(name, value));
            }
        }
    }

    public void checkBIC(
            final boolean required,
            @Nonnull final String name,
            @Nullable final String value) {
        assert name != null;
        assertNotFinished();
        checkNotNull(required, name, value);

        if (value != null) {
            if ((value.length() != ApiValidationConstants.API_BIC_MIN_LENGTH) &&
                    (value.length() != ApiValidationConstants.API_BIC_MAX_LENGTH)) {
                errors.add(new ApiBICFormatError(name, value));
            }
        }
    }

    void assertFinished() {
        if (status != Status.FINISHED) {
            LOG.error("assertFinished: Validation framework error. Validation was not finished properly.");
            assert false;
        }
    }

    void assertNotStarted() {
        if (status != Status.NOT_STARTED) {
            LOG.error("assertNotStarted: Validation framework error. Validation was called before setter.");
            assert false;
        }
    }

    void assertNotFinished() {
        if (status == Status.FINISHED) {
            LOG.error("assertNotFinished: Validation framework error. Validation was unexpectedly finished.");
            assert false;
        }
    }

    private void throwIfExceptions() throws ApiBadRequestException {
        if (!errors.isEmpty()) {
            LOG.debug("throwIfExceptions: API validation error: {}", errors);
            throw new ApiBadRequestException(errors);
        }
    }
}
