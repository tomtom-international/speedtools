/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.tomtom.speedtools.mongodb.mappers.MapperTestUtils.Box;
import com.tomtom.speedtools.mongodb.mappers.MapperTestUtils.BoxMapper;
import com.tomtom.speedtools.mongodb.mappers.MapperTestUtils.Color;
import com.tomtom.speedtools.mongodb.mappers.MapperTestUtils.Container;
import com.tomtom.speedtools.mongodb.mappers.MapperTestUtils.ContainerMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class EntityMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(EntityMapperTest.class);

    private static final String JSON_VALUE =
        "{ \"color\" : \"blue\" , \"created\" : { \"$date\" : \"2000-10-09T08:07:06.005Z\"} , \"name\" : \"x\" , \"price\" : 5.6 , \"boxes\" : [ { \"color\" : \"red\" , \"created\" : { \"$date\" : \"2001-02-03T04:05:06.007Z\"} , \"name\" : \"a\" , \"price\" : 1.2 , \"_type\" : \"box\"} , { \"color\" : \"green\" , \"created\" : { \"$date\" : \"2011-12-13T14:15:16.017Z\"} , \"name\" : \"b\" , \"price\" : 3.4 , \"_type\" : \"box\"}] , \"shade\" : \"yellow\" , \"shipped\" : { \"$date\" : \"1990-03-04T05:20:21.022Z\"} , \"line\" : \"y\" , \"weight\" : 7.8 , \"_type\" : \"container\"}";

    private static       ContainerMapper mapper     = null;
    private static       Container       container1 = null;
    private static final MapperRegistry                  registry   = new MapperRegistry();

    @BeforeClass
    public static void setup() throws SchemaException {
        registry.register(new BoxMapper());
        registry.register(new ContainerMapper());
        mapper = registry.getMapper(ContainerMapper.class);

        // TODO: UTCTime parsing errors
        final Box box1 =
            new Box(Color.RED, new DateTime(2001, 2, 3, 4, 5, 6, 7, DateTimeZone.UTC),
                "a", 1.2);
        final Box
            box2 =
            new Box(Color.GREEN,
                new DateTime(2011, 12, 13, 14, 15, 16, 17, DateTimeZone.UTC), "b", 3.4);
        final Collection<Box> boxes = new ArrayList<>();
        boxes.add(box1);
        boxes.add(box2);
        container1 = new Container(
            Color.BLUE, new DateTime(2000, 10, 9, 8, 7, 6, 5, DateTimeZone.UTC), "x", 5.6, boxes,
            Color.YELLOW, new DateTime(1990, 3, 4, 5, 20, 21, 22, DateTimeZone.UTC), "y", 7.8);
    }

    @Test
    public void testGetVersion() throws SchemaException, MapperException {
        LOG.info("testGetVersion");
        final int version = mapper.getCurrentVersion();
        Assert.assertTrue(version >= 0);
    }

    @Test
    public void testToDb() throws SchemaException, MapperException {
        LOG.info("testToDb");
        final DBObject db1 = mapper.toDb(container1);
        Assert.assertTrue(db1 != null);
        Assert.assertEquals(JSON_VALUE, db1.toString());
    }

    @Test
    public void testFromDb() throws SchemaException, MapperException {
        LOG.info("testFromDb");
        final DBObject db2 = (DBObject) JSON.parse(JSON_VALUE);
        final Container container2 = mapper.fromDb(db2);
        Assert.assertEquals(container1, container2);
    }
}
