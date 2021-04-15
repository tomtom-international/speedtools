/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.mongodb.mappers;

import com.tomtom.speedtools.domain.Money;

import java.util.Currency;

public class MoneyMapper extends EntityMapper<Money> {
    public final EntityType entityType = entityType(Money.class);

    public final Field<Currency> currency = currencyField("currency", "getCurrency", CONSTRUCTOR);
    public final Field<Integer> amount = integerField("amount", "getAmount", CONSTRUCTOR);
}
