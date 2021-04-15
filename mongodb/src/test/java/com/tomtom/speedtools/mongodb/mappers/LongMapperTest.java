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

package com.tomtom.speedtools.mongodb.mappers;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(LongMapperTest.class);

    @Test
    public void testLongMapper() throws SchemaException, MapperException {
        LOG.info("testLongMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new LongMapper());

        final LongMapper mapper = registry.getMapper(LongMapper.class);
        Number n = Long.MAX_VALUE;
        Number m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertEquals(Long.MAX_VALUE, m.longValue());
        Assert.assertEquals(n, mapper.toDb(Long.MAX_VALUE));

        n = Long.MIN_VALUE;
        m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertEquals(Long.MIN_VALUE, m.longValue());
        Assert.assertEquals(n, mapper.toDb(Long.MIN_VALUE));

        n = 0L;
        m = mapper.fromDb(n);
        Assert.assertTrue(m != null);
        Assert.assertEquals(0L, m.longValue());
        Assert.assertEquals(n, mapper.toDb(0L));
    }
}
