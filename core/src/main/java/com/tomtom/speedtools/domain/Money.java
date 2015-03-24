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

package com.tomtom.speedtools.domain;

import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Currency;

import com.tomtom.speedtools.objects.Objects;


/**
 * Money, or monetary amount. (Immutable)
 * <p/>
 * Money consist of a currency and a positive (or 0) value called 'amount'. The decimal point which would normally be
 * placed in a monetary value amount is shifted completely to the right. For example, EURO 2.35 would be specified as
 * Currency:EURO Amount:235 and YEN 10234 as Currency:YEN Amount: 10234.
 * <p/>
 * Constructor: {@link #Money(java.util.Currency, Integer)}
 */
@Immutable
public final class Money {
    @Nonnull
    private final Currency currency;
    @Nonnull
    private final Integer amount;

    /**
     * Create an address record.
     *
     * @param currency Currency (according to ISO 4217, see http://en.wikipedia.org/wiki/ISO_4217).
     * @param amount   Amount, multiplied by 10^Currency.getDefaultFractionDigits(). Must be &gt;= 0.
     */
    public Money(
            @Nonnull final Currency currency,
            @Nonnull final Integer amount) {
        super();
        assert currency != null;
        assert amount != null;
        assert amount >= 0;
        this.currency = currency;
        this.amount = amount;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings({"ConstantConditions", "UnusedDeclaration"})
    @Deprecated
    private Money() {
        super();
        currency = null;
        amount = null;
    }

    /**
     * Currency.
     *
     * @return Currency.
     */
    @Nonnull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Amount, multiplied by 10^Currency.getDefaultFractionDigits().
     *
     * @return Amount, always &gt;= 0.
     */
    @Nonnull
    public Integer getAmount() {
        return amount;
    }

    /**
     * Setter for {@link #getCurrency()}.
     *
     * @param currency Currency.
     * @return New immutable object.
     */
    @Nonnull
    public Money withCurrency(@Nonnull final Currency currency) {
        return new Money(currency, amount);
    }

    /**
     * Setter for {@link #getAmount()}.
     *
     * @param amount Amount, multiplied by Currency.getDefaultFractionDigits(). Must be &gt;= 0.
     * @return New immutable object.
     */
    @Nonnull
    public Money withAmount(@Nonnull final Integer amount) {
        return new Money(currency, amount);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof Money;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Money)) {
            final Money that = (Money) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && currency.equals(that.currency);
            eq = eq && amount.equals(that.amount);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(currency, amount);
    }

    @Override
    public String toString() {
        return '[' + currency.getCurrencyCode() + ' ' + amount + ']';
    }
}
