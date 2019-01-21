/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Currency;

public class CurrencyMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyMapperTest.class);

    @Test
    public void testCurrencyMapper() throws SchemaException, MapperException {
        LOG.info("testCurrencyMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new CurrencyMapper());

        final CurrencyMapper mapper = registry.getMapper(CurrencyMapper.class);
        final String n = "EUR";
        final Currency m = mapper.fromDb(n);
        Assert.assertNotNull(m);
        Assert.assertEquals("EUR", m.getCurrencyCode());
        Assert.assertEquals(n, mapper.toDb(Currency.getInstance("EUR")));
    }

    @Test(expected = MapperException.class)
    public void testUnknownCurrency() throws SchemaException, MapperException {
        LOG.info("testUnknownCurrency");

        final MapperRegistry registry = new MapperRegistry();
        registry.register(new CurrencyMapper());

        final CurrencyMapper mapper = registry.getMapper(CurrencyMapper.class);
        final String n = "NON_EXISTENT_CURRENCY";
        mapper.fromDb(n);
    }
}
