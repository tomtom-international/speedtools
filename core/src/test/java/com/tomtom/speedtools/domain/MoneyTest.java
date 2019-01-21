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

package com.tomtom.speedtools.domain;

import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Currency;

/**
 * <strong>Important:</strong> See {@link com.tomtom.speedtools.testutils.TestUtils} for an explanation of the unit test code
 * structure.
 */
@SuppressWarnings("FieldMayBeStatic")
public class MoneyTest {
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTest.class);

    // x is the simplest, empty instance.
    private final Currency x1 = Currency.getInstance("USD");
    private final int x2 = 0;
    private final Money x = new Money(x1, x2);

    // y and z are a filled instances.
    private Currency y1 = null;
    private int y2 = 0;
    private Money y = null;

    @Before
    public void setUp() {
        y1 = Currency.getInstance("EUR");
        y2 = 1234;
        y = new Money(y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(Money.class).
                withPrefabValues(Currency.class, Currency.getInstance("EUR"), Currency.getInstance("USD")).
                verify();
    }

    @Test
    public void testEquals() {
        LOG.info("testEquals");
        final Money s = y.withCurrency(x1).withAmount(x2);
        final Money t = x.withCurrency(y1).withAmount(y2);
        Assert.assertTrue(x.equals(s));
        Assert.assertTrue(y.equals(t));
        Assert.assertFalse(x.equals(t));
        Assert.assertFalse(y.equals(s));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(Money.class, new int[]{}, y1, y2);
    }

    @Test
    public void testWithCurrency() {
        LOG.info("testWithCurrency");
        Assert.assertTrue(y1.equals(x.withCurrency(y1).getCurrency()));
        Assert.assertNotNull(y.withCurrency(x1).getCurrency());
    }

    @Test
    public void testWithAmount() {
        LOG.info("testWithAmount");
        Assert.assertTrue(y2 == x.withAmount(y2).getAmount());
        Assert.assertTrue(x2 == y.withAmount(x2).getAmount());
    }
}
