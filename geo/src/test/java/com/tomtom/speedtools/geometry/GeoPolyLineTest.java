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

import com.google.common.collect.Lists;
import com.tomtom.speedtools.testutils.ValidationFailException;
import com.tomtom.speedtools.testutils.constructorchecker.ConstructorChecker;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GeoPolyLineTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoPolyLineTest.class);

    @Test
    public void testEqualsVerifier() {
        LOG.info("testEqualsVerifier");
        EqualsVerifier.forClass(GeoPolyLine.class).
                withRedefinedSuperclass().
                verify();
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testNew() throws ValidationFailException {
        LOG.info("testNew");
        ConstructorChecker.validateConstructor(GeoPolyLine.class, new int[]{},
                Lists.asList(new GeoPoint(-3.0, -4.0), new GeoPoint[]{new GeoPoint(5.0, 6.0)}));
    }

    @Test
    public void testGetLengthMeters() {
        LOG.info("testGetLengthMeters");
        final GeoPoint o = new GeoPoint(-0.5, 10.0); // Note that the values are chosen symmetrically around lat=0!
        final GeoPoint x1 = new GeoPoint(0.5, 10.0);
        final GeoPoint x2 = new GeoPoint(1.5, 10.0);
        final GeoPolyLine poly = new GeoPolyLine(Lists.asList(o, x1, new GeoPoint[]{x2}));

        final double len1 = Geo.METERS_PER_DEGREE_LAT * 2.0;
        final double len2 = poly.getLengthMeters();
        Assert.assertEquals(0, Double.compare(len1, len2));
    }

    @Test
    public void testAsLines() {
        LOG.info("testAsLines");
        final GeoPoint o = new GeoPoint(-0.5, 10.0); // Note that the values are chosen symmetrically around lat=0!
        final GeoPoint x1 = new GeoPoint(0.5, 10.0);
        final GeoPoint x2 = new GeoPoint(1.5, 10.0);
        final GeoPolyLine poly = new GeoPolyLine(Lists.asList(o, x1, new GeoPoint[]{x2}));

        final List<GeoLine> lines = poly.asLines();
        Assert.assertEquals(2, lines.size());
        Assert.assertEquals(lines.get(0), new GeoLine(o, x1));
        Assert.assertEquals(lines.get(1), new GeoLine(x1, x2));
    }

    @Test
    public void testGetCenter1() {
        LOG.info("testGetCenter1");
        final GeoPoint x1 = new GeoPoint(0.0, 0.0);
        final GeoPoint x2 = new GeoPoint(10.0, 0.0);
        final GeoPoint x3 = new GeoPoint(10.0, 10.0);
        final GeoPoint x4 = new GeoPoint(0.0, 10.0);
        final List<GeoPoint> p1 = Lists.asList(x1, new GeoPoint[]{x2, x3, x4});
        final GeoPolyLine poly1 = new GeoPolyLine(p1);
        Assert.assertEquals(new GeoPoint(5.0, 5.0), poly1.getCenter());
    }

    @Test
    public void testGetCenter2() {
        LOG.info("testGetCenter2");
        final GeoPoint x1 = new GeoPoint(0.0, 0.0);
        final GeoPoint x2 = new GeoPoint(0.0, 10.0);
        final GeoPoint x3 = new GeoPoint(2.0, 20.0);
        final List<GeoPoint> p1 = Lists.asList(x1, new GeoPoint[]{x2, x3});
        final GeoPolyLine poly1 = new GeoPolyLine(p1);
        Assert.assertEquals(new GeoPoint(1.0, 10.0), poly1.getCenter());
    }

    @Test
    public void testTranslate() {
        LOG.info("testTranslate");
        final GeoPoint x1 = new GeoPoint(0.0, 179.0);
        final GeoPoint x2 = new GeoPoint(0.0, -179.0);
        final GeoPoint x3 = new GeoPoint(0.0, 0.0);
        final List<GeoPoint> p1 = Lists.asList(x1, new GeoPoint[]{x2, x3});
        final GeoPolyLine poly = new GeoPolyLine(p1);
        final GeoPolyLine poly1 = poly.translate(new GeoVector(2.0, 1.0));
        final GeoPolyLine poly2 = poly.translate(new GeoVector(-2.0, -1.0));
        Assert.assertEquals(new GeoPoint(2.0, -180.0), poly1.get(0));
        Assert.assertEquals(new GeoPoint(2.0, -178.0), poly1.get(1));
        Assert.assertEquals(new GeoPoint(2.0, 1.0), poly1.get(2));

        Assert.assertEquals(new GeoPoint(-2.0, 178.0), poly2.get(0));
        Assert.assertEquals(new GeoPoint(-2.0, -180.0), poly2.get(1));
        Assert.assertEquals(new GeoPoint(-2.0, -1.0), poly2.get(2));
    }

    @Test
    public void testMoveTo() {
        LOG.info("testMoveTo");
        final GeoPoint x1 = new GeoPoint(0.0, 179.0);
        final GeoPoint x2 = new GeoPoint(0.0, -179.0);
        final GeoPoint x3 = new GeoPoint(0.0, 0.0);
        final List<GeoPoint> p1 = Lists.asList(x1, new GeoPoint[]{x2, x3});
        final GeoPolyLine poly = new GeoPolyLine(p1);
        final GeoPolyLine poly1 = poly.moveTo(new GeoPoint(2.0, -180.0));
        final GeoPolyLine poly2 = poly.moveTo(new GeoPoint(-2.0, 178.0));
        Assert.assertEquals(new GeoPoint(2.0, -180.0), poly1.get(0));
        Assert.assertEquals(new GeoPoint(2.0, -178.0), poly1.get(1));
        Assert.assertEquals(new GeoPoint(2.0, 1.0), poly1.get(2));

        Assert.assertEquals(new GeoPoint(-2.0, 178.0), poly2.get(0));
        Assert.assertEquals(new GeoPoint(-2.0, -180.0), poly2.get(1));
        Assert.assertEquals(new GeoPoint(-2.0, -1.0), poly2.get(2));
    }
}
