/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DateTimeMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeMapperTest.class);

    @Test
    public void testDateTimeMapper() throws SchemaException, MapperException {
        LOG.info("testDateTimeMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new DateTimeMapper());

        final DateTimeMapper mapper = registry.getMapper(DateTimeMapper.class);
        final DateTime dt = UTCTime.from(new DateTime(2000, 1, 2, 3, 4, 5, 6));
        final Date d = dt.toDate();
        Assert.assertEquals(dt, mapper.fromDb(d));
        Assert.assertEquals(d, mapper.toDb(dt));
    }
}
