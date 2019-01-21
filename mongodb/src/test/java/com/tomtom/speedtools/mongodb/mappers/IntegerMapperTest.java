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

public class IntegerMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(IntegerMapperTest.class);

    @Test
    public void testIntegerMapper() throws SchemaException, MapperException {
        LOG.info("testIntegerMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new IntegerMapper());

        final IntegerMapper mapper = registry.getMapper(IntegerMapper.class);
        Number n = Integer.MAX_VALUE;
        Number m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertEquals(Integer.MAX_VALUE, m.intValue());
        Assert.assertEquals(n, mapper.toDb(Integer.MAX_VALUE));

        n = Integer.MIN_VALUE;
        m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertEquals(Integer.MIN_VALUE, m.intValue());
        Assert.assertEquals(n, mapper.toDb(Integer.MIN_VALUE));

        n = 0;
        m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertEquals(0, m.intValue());
        Assert.assertEquals(n, mapper.toDb(0));
    }
}
