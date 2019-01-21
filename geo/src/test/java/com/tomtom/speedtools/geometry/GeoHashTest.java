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

package com.tomtom.speedtools.geometry;

import com.tomtom.speedtools.utils.MathUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoHashTest {
    private static final Logger LOG = LoggerFactory.getLogger(GeoHashTest.class);

    public static final GeoPoint POS_LONDON = new GeoPoint(51.506, -0.75);   // London.
    public static final GeoPoint POS_PARIS = new GeoPoint(48.861, 2.335);   // Paris.
    public static final GeoPoint POS_AMSTERDAM = new GeoPoint(52.3765, 4.908);  // TomTom HQ.

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testHash() {
        LOG.info("testHash");

        final String posAmsterdam = "u173zwvghxq0";
        final String posLondon = "gcpmnbmu5wr3";
        final String posParis = "u09tvnu7cqw4";

        // Should never throw exceptions.
        Assert.assertEquals("s00000000000", new GeoHash(new GeoPoint(0.0, 0.0)).getHash());
        Assert.assertEquals("s00j8n012j80", new GeoHash(new GeoPoint(1.0, 0.0)).getHash());
        Assert.assertEquals("s008nb00j8n0", new GeoHash(new GeoPoint(0.0, 1.0)).getHash());
        Assert.assertEquals(posAmsterdam, new GeoHash(POS_AMSTERDAM).getHash());
        Assert.assertEquals(posLondon, new GeoHash(POS_LONDON).getHash());
        Assert.assertEquals(posParis, new GeoHash(POS_PARIS).getHash());

        final GeoPoint x = new GeoHash(posAmsterdam).getPoint();
        Assert.assertTrue(MathUtils.isAlmostEqual(POS_AMSTERDAM.getLat(), x.getLat()));
        Assert.assertTrue(MathUtils.isAlmostEqual(POS_AMSTERDAM.getLon(), x.getLon()));

        final GeoPoint y = new GeoHash(posLondon).getPoint();
        Assert.assertTrue(MathUtils.isAlmostEqual(POS_LONDON.getLat(), y.getLat()));
        Assert.assertTrue(MathUtils.isAlmostEqual(POS_LONDON.getLon(), y.getLon()));

        final GeoPoint z = new GeoHash(posParis).getPoint();
        Assert.assertTrue(MathUtils.isAlmostEqual(POS_PARIS.getLat(), z.getLat()));
        Assert.assertTrue(MathUtils.isAlmostEqual(POS_PARIS.getLon(), z.getLon()));

        final GeoHash hash = new GeoHash(POS_PARIS);
        Assert.assertTrue(hash.decreaseResolution().contains(new GeoHash(POS_PARIS)));
        Assert.assertFalse(new GeoHash(POS_PARIS).contains(hash.decreaseResolution()));
        Assert.assertTrue(hash.decreaseResolution(hash.length() - 1).contains(new GeoHash(POS_AMSTERDAM)));

        // Should always throw an IllegalArgumentException.
        try {
            //noinspection ResultOfObjectAllocationIgnored
            new GeoHash("frikandel"); // Has several invalid characters.
            Assert.fail("Invalid value should not be accepted.");
        } catch (final IllegalArgumentException ok) {
            // OK.
        }

        // Should always throw an IllegalArgumentException.
        try {
            //noinspection ResultOfObjectAllocationIgnored
            new GeoHash(""); // Has several invalid characters.
            Assert.fail("Empty value should not be accepted.");
        } catch (final IllegalArgumentException ok) {
            // OK.
        }
    }
}
