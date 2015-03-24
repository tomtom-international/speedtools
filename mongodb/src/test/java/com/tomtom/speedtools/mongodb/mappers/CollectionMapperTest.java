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

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(CollectionMapperTest.class);

    private CollectionMapper<String> mapper = null;
    private MapperRegistry registry;

    @Before
    public void setup() throws SchemaException {
        registry = new MapperRegistry();
        registry.register(CollectionMapper.create(StringMapper.class));
        //noinspection unchecked
        mapper = registry.getMapper(CollectionMapper.class);
        mapper.initialize(registry);
    }

    @Test
    public void testFromDb() throws SchemaException, MapperException {
        LOG.info("testFromDb");
        Collection<String> db = null;
        Collection<?> mem;

        mem = mapper.fromDb(db);
        Assert.assertTrue(mem != null);
        Assert.assertTrue(mem.isEmpty());

        db = new ArrayList<String>();
        mem = mapper.fromDb(db);
        Assert.assertTrue(mem != null);
        Assert.assertTrue(mem.isEmpty());

        db.add("red");
        mem = mapper.fromDb(db);
        Assert.assertTrue(mem != null);
        Assert.assertTrue(mem.size() == 1);
        Assert.assertEquals("red", mem.toArray()[0]);

        db.add("blue");
        mem = mapper.fromDb(db);
        Assert.assertTrue(mem != null);
        Assert.assertTrue(mem.size() == 2);
        Assert.assertEquals("red", mem.toArray()[0]);
        Assert.assertEquals("blue", mem.toArray()[1]);
    }

    @Test
    public void testToDb() throws SchemaException, MapperException {
        LOG.info("testToDb");
        List<?> db;
        Collection<String> mem = null;

        db = mapper.toDb(mem);
        Assert.assertTrue(db != null);
        Assert.assertTrue(db.isEmpty());

        mem = new ArrayList<String>();
        db = mapper.toDb(mem);
        Assert.assertTrue(db != null);
        Assert.assertTrue(db.isEmpty());

        mem.add("silver");
        db = mapper.toDb(mem);
        Assert.assertTrue(db != null);
        Assert.assertTrue(db.size() == 1);
        Assert.assertEquals("silver", db.get(0));

        mem.add("gold");
        db = mapper.toDb(mem);
        Assert.assertTrue(db != null);
        Assert.assertTrue(db.size() == 2);
        Assert.assertEquals("gold", db.get(1));
    }

    @Test
    public void testInitializeElementMapper() throws SchemaException {
        // Register a new collection mapper
        registry.register(new DummyContainerMapper());

        // Find the element mapper.
        final DummyMapper dummyMapper = registry.findMapper(DummyMapper.class);

        // See if it is initialized.
        Assert.assertNotNull(dummyMapper);
        Assert.assertTrue(dummyMapper.isInitialized());
    }

    private static class Dummy {
        // Empty.
    }

    private static class DummyContainer {

        @Nonnull
        public Collection<Dummy> getDummies() {
            return new ArrayList<Dummy>();
        }
    }

    public static class DummyMapper extends EntityMapper<Dummy> {
        public final EntityType entityType = entityType(Dummy.class);
    }

    private static class DummyContainerMapper extends EntityMapper<DummyContainer> {
        public final EntityType entityType = entityType(DummyContainer.class);

        public final Field<Collection<Dummy>> dummies =
                collectionField("dummies", DummyMapper.class, "getDummies", NO_SETTER);
    }
}
