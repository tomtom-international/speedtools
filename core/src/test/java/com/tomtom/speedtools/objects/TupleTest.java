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

package com.tomtom.speedtools.objects;

import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("FieldMayBeStatic")
public class TupleTest {
    private static final Logger LOG = LoggerFactory.getLogger(TupleTest.class);

    private final Double x1 = 1.0;
    private final String x2 = "";
    private final Tuple<Double, String> x = new Tuple<>(x1, x2);

    private Double y1 = 0.0;
    private final String y2 = "1";
    private Tuple<Double, String> y = null;

    @Before
    public void setUp() {
        y1 = 3.0;
        y = new Tuple<>(y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(Tuple.class).
                withRedefinedSuperclass().
                verify();
    }

    @Test
    public void testEquals() {
        LOG.info("testEquals");
        final Tuple<Double, String> s = Tuple.create(x1, x2);
        final Tuple<Double, String> t = Tuple.create(y1, y2);
        Assert.assertTrue(x.equals(s));
        Assert.assertTrue(y.equals(t));
        Assert.assertFalse(x.equals(t));
        Assert.assertFalse(y.equals(s));
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(Tuple.class, new int[]{}, y1, y2);
    }

    @Test
    public void testGet() {
        LOG.info("testget");
        final Tuple<String, Integer> x = Tuple.create("a", 1);
        Assert.assertEquals("a", x.getValue1());
        Assert.assertEquals(Integer.valueOf(1), x.getValue2());
    }
}
