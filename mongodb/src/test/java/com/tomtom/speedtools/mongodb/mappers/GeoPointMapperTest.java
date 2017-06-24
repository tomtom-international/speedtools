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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tomtom.speedtools.geometry.GeoPoint;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

@SuppressWarnings("StaticVariableMayNotBeInitialized")
public class GeoPointMapperTest {

    private static final Logger LOG = LoggerFactory.getLogger(GeoPointMapperTest.class);

    private static GeoPointMapper mapper;

    @BeforeClass
    public static void initialize() throws SchemaException {
        final MapperRegistry mapperRegistry = new MapperRegistry();
        mapperRegistry.register(new GeoPointMapper());
        mapper = mapperRegistry.getMapper(GeoPointMapper.class);
    }

    @Test
    public void testFromDb() throws MapperException {
        LOG.info("testFromDb");

        final DBObject dbObject = new BasicDBObject();
        dbObject.put("lat", 1.5);
        dbObject.put("lon", 2.0);

        final GeoPoint entity = mapper.fromDb(dbObject);
        Assert.assertNotNull(entity);
        Assert.assertEquals(1.5d, entity.getLat(), 0.001);
        Assert.assertEquals(2.0d, entity.getLon(), 0.001);
    }

    @Test
    public void testToDb() throws MapperException {
        LOG.info("testToDb");

        final GeoPoint point = new GeoPoint(1.5, 2.0);
        final DBObject dbObject = mapper.toDb(point);

        Assert.assertNotNull(dbObject);
        final Object lat = dbObject.get("lat");
        Assert.assertNotNull(lat);
        Assert.assertTrue(lat instanceof Double);
        Assert.assertEquals(1.5, (Double) lat, 0.001);
        final Object lon = dbObject.get("lon");
        Assert.assertNotNull(lon);
        Assert.assertTrue(lon instanceof Double);
        Assert.assertEquals(1.5, (Double) lat, 0.001);
    }

    @Test
    public void testGeoIndexCompatibility() throws MapperException {
        LOG.info("testGeoIndexCompatibility");

        final GeoPoint point = new GeoPoint(1.5, 2.0);
        final DBObject dbObject = mapper.toDb(point);

        Assert.assertNotNull(dbObject);
        Assert.assertEquals(3, dbObject.keySet().size());
        final Iterator<String> iterator = dbObject.keySet().iterator();
        Assert.assertEquals("lat", iterator.next());
        Assert.assertEquals("lon", iterator.next());
    }
}
