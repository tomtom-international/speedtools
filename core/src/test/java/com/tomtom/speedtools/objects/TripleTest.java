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

package com.tomtom.speedtools.objects;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;


@SuppressWarnings("FieldMayBeStatic")
public class TripleTest {
    private static final Logger LOG = LoggerFactory.getLogger(TripleTest.class);

    private final Double x1 = 1.0;
    private final String x2 = "";
    private final Boolean x3 = false;
    private final Triple<Double, String, Boolean> x = new Triple<>(x1, x2, x3);

    private Double y1 = null;
    private String y2 = null;
    private Boolean y3 = null;
    private Triple<Double, String, Boolean> y = null;

    @Before
    public void setUp() {
        y1 = 2.3;
        y2 = "a";
        y3 = true;
        //noinspection ConstantConditions
        y = new Triple<>(y1, y2, y3);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(Triple.class).
                withRedefinedSuperclass().
                verify();
        // Do not add: allFieldsShouldBeUsed()
    }

    @Test
    public void testEquals() {
        LOG.info("testEquals");
        final Triple<Double, String, Boolean> s = Triple.create(x1, x2, x3);
        final Triple<Double, String, Boolean> t = Triple.create(y1, y2, y3);
        Assert.assertEquals(s, x);
        Assert.assertEquals(t, y);
        Assert.assertFalse(x.equals(t));
        Assert.assertFalse(y.equals(s));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(Triple.class, new int[]{}, y1, y2, y3);
    }

    @Test
    public void testGet() {
        LOG.info("testget");
        final Triple<String, Integer, Boolean> x = Triple.create("a", 1, true);
        Assert.assertEquals("a", x.getValue1());
        Assert.assertEquals(Integer.valueOf(1), x.getValue2());
        Assert.assertEquals(true, x.getValue3());
    }
}
