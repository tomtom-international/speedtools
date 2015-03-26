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

package com.tomtom.speedtools.mongodb;

import com.mongodb.BasicDBObject;
import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper;
import com.tomtom.speedtools.mongodb.mappers.MapperException;
import com.tomtom.speedtools.mongodb.mappers.MapperRegistry;
import com.tomtom.speedtools.mongodb.mappers.SchemaException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

// TODO: Implement these tests.
public class MongoDBUpdateTest {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBUpdateTest.class);

    @Test
    public void testSetAllExcept() throws MapperException, SchemaException {
        LOG.info("testSetAllExcept");
        final TestMapperRegistry testMapperRegistry = new TestMapperRegistry();
        final TestEntityMapper testEntityMapper = testMapperRegistry.getMapper(TestEntityMapper.class);
        final TestEntity testEntity = new TestEntity(new Uid<TestEntity>(), "field0", "field1", "field2", "field3",
                "field4");

        // One exclude.
        final MongoDBUpdate mongoDBUpdate0 = new MongoDBUpdate().setAllExcept(testEntityMapper, testEntity,
                testEntityMapper.f4);
        final BasicDBObject value0 = (BasicDBObject) mongoDBUpdate0.toDBObject().get(MongoDBUpdate.OPERATOR_SET);
        Assert.assertNotNull(value0);
        Assert.assertEquals(4, value0.size());

        // Two excludes.
        final MongoDBUpdate mongoDBUpdate1 = new MongoDBUpdate().setAllExcept(testEntityMapper, testEntity,
                testEntityMapper.f3, testEntityMapper.f4);
        final BasicDBObject value1 = (BasicDBObject) mongoDBUpdate1.toDBObject().get(MongoDBUpdate.OPERATOR_SET);
        Assert.assertNotNull(value1);
        Assert.assertEquals(3, value1.size());

        // No excludes.
        final MongoDBUpdate mongoDBUpdate2 = new MongoDBUpdate().setAllExcept(testEntityMapper, testEntity);
        final BasicDBObject value2 = (BasicDBObject) mongoDBUpdate2.toDBObject().get(MongoDBUpdate.OPERATOR_SET);
        Assert.assertNotNull(value2);
        Assert.assertEquals(5, value2.size());

        // All excludes.
        final MongoDBUpdate mongoDBUpdate3 = new MongoDBUpdate().setAllExcept(testEntityMapper, testEntity,
                testEntityMapper.f0, testEntityMapper.f1, testEntityMapper.f2, testEntityMapper.f3, testEntityMapper.f4);
        final BasicDBObject value3 = (BasicDBObject) mongoDBUpdate3.toDBObject();
        Assert.assertNotNull(value3);
        Assert.assertEquals(0, value3.size());
    }

    public static class TestEntity {
        @SuppressWarnings("InstanceVariableNamingConvention")
        @Nonnull
        private final Uid<TestEntity> id;
        @Nonnull
        private final String f0;
        @Nonnull
        private final String f1;
        @Nonnull
        private final String f2;
        @Nonnull
        private final String f3;
        @Nonnull
        private final String f4;

        public TestEntity(
                @Nonnull final Uid<TestEntity> id,
                @Nonnull final String f0,
                @Nonnull final String f1,
                @Nonnull final String f2,
                @Nonnull final String f3,
                @Nonnull final String f4) {
            assert id != null;
            assert f0 != null;
            assert f1 != null;
            assert f2 != null;
            assert f3 != null;
            assert f4 != null;

            this.id = id;
            this.f0 = f0;
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
            this.f4 = f4;
        }

        @Nonnull
        public Uid<TestEntity> getId() {
            return id;
        }

        @Nonnull
        public String getF0() {
            return f0;
        }

        @Nonnull
        public String getF1() {
            return f1;
        }

        @Nonnull
        public String getF2() {
            return f2;
        }

        @Nonnull
        public String getF3() {
            return f3;
        }

        @Nonnull
        public String getF4() {
            return f4;
        }
    }

    private static class TestEntityMapper extends EntityMapper<TestEntity> {
        public final EntityType entityType = entityType(TestEntity.class);
        public final Field<Uid<TestEntity>> id = idField("getId", CONSTRUCTOR);
        public final Field<String> f0 = stringField("f0", "getF0", CONSTRUCTOR);
        public final Field<String> f1 = stringField("f1", "getF1", CONSTRUCTOR);
        public final Field<String> f2 = stringField("f2", "getF2", CONSTRUCTOR);
        public final Field<String> f3 = stringField("f3", "getF3", CONSTRUCTOR);
        public final Field<String> f4 = stringField("f4", "getF4", CONSTRUCTOR);

    }

    private static class TestMapperRegistry extends MapperRegistry {

        private TestMapperRegistry() throws SchemaException {
            super();
            register(new TestEntityMapper());
        }
    }
}
