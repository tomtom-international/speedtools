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

package com.tomtom.speedtools.domain;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorConverterTest {
    private static final Logger LOG = LoggerFactory.getLogger(ColorConverterTest.class);

    @Test
    public void testToColorRGB() {
        LOG.info("testToColorRGB");
        final ColorRGB8 c1 = new ColorRGB8(1, 2, 3);
        Assert.assertEquals(c1, ColorConverter.toColorRGB("#010203"));
    }

    @Test
    public void testToColorCSS() {
        LOG.info("testToColorCSS");
        final ColorRGB8 c1 = new ColorRGB8(1, 2, 3);
        Assert.assertEquals("#010203", ColorConverter.toColorCSS(c1));
    }
}
