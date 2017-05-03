/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

public class BooleanMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(BooleanMapperTest.class);

    @Test
    public void testBooleanMapper() throws SchemaException, MapperException {
        LOG.info("testBooleanMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new BooleanMapper());

        final BooleanMapper mapper = registry.getMapper(BooleanMapper.class);
        final Boolean b1 = false;
        final Boolean b2 = mapper.fromDb(b1);
        Assert.assertTrue(b2 != null);
        Assert.assertEquals(false, b2);
        Assert.assertEquals(b2, mapper.toDb(false));

        final Boolean c1 = true;
        final Boolean c2 = mapper.fromDb(c1);
        Assert.assertTrue(c2 != null);
        Assert.assertEquals(true, c2);
        Assert.assertEquals(c2, mapper.toDb(true));
    }
}
