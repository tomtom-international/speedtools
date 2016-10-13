/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("FieldMayBeStatic")
public class GeoPointTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoPointTest.class);

    private final double x1 = 0;
    private final double x2 = 0;
    private final GeoPoint x = new GeoPoint(x1, x2);

    private double y1 = 0;
    private double y2 = 0;
    private GeoPoint y = null;

    @Before
    public void setUp() {
        y1 = 1;
        y2 = 2;
        y = new GeoPoint(y1, y2);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(GeoPoint.class, new int[]{}, y1, y2);
    }

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GeoPoint.class).
                withRedefinedSuperclass().
                verify();
    }

    @Test
    public void testWithLat() {
        LOG.info("testWithLat");
        Assert.assertEquals(0, Double.compare(y1, x.withLat(y1).getLat()));
        Assert.assertEquals(0, Double.compare(x1, y.withLat(x1).getLat()));
    }

    @Test
    public void testWithLon() {
        LOG.info("testWithLon");
        Assert.assertEquals(0, Double.compare(y2, x.withLon(y2).getLon()));
        Assert.assertEquals(0, Double.compare(x2, y.withLon(x2).getLon()));
    }
}
