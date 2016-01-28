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

package com.tomtom.speedtools.mongodb.mappers;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(DoubleMapperTest.class);

    @Test
    public void testDoubleMapper() throws SchemaException, MapperException {
        LOG.info("testDoubleMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new DoubleMapper());

        final DoubleMapper mapper = registry.getMapper(DoubleMapper.class);
        Number n = Double.MAX_VALUE;
        Number m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertTrue(Double.compare(Double.MAX_VALUE, m.doubleValue()) == 0);
        Assert.assertEquals(n, mapper.toDb(Double.MAX_VALUE));

        n = Double.MIN_VALUE;
        m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertTrue(Double.compare(Double.MIN_VALUE, m.doubleValue()) == 0);
        Assert.assertEquals(n, mapper.toDb(Double.MIN_VALUE));

        n = 0.0;
        m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertTrue(Double.compare(0.0, m.doubleValue()) == 0);
        Assert.assertEquals(n, mapper.toDb(0.0));
    }
}
