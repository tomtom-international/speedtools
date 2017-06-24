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

import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;


public class ReflectiveFieldTest {
    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveFieldTest.class);

    private static PersonMapper mapper = null;
    private static final MapperRegistry registry = new MapperRegistry();

    @BeforeClass
    public static void setUp() throws SchemaException {
        registry.register(new PersonMapper());
//        registry.register(new MapperTestUtils.ContainerMapper());
        mapper = registry.getMapper(PersonMapper.class);

    }

    @Test
    public void testNewInstance() throws SchemaException, MapperException {
        LOG.info("testNewInstance");

        // Our sample person.
        final Person p = new Person(12).withName("Alfred");

        // Convert to dbObject.
        final DBObject dbObject = mapper.toDb(p);
        assert dbObject != null;
        Assert.assertEquals(p.getId(), dbObject.get("id"));
        Assert.assertEquals(p.getName(), dbObject.get("name"));

        // And convert back to a person.
        final Person p2 = mapper.fromDb(dbObject);
        assert p2 != null;
        Assert.assertEquals(p.getId(), p2.getId());
        Assert.assertEquals(p.getName(), p2.getName());
    }

    static class Person {
        private final int id;
        private final String name;

        public Person(final int id) {
            this.id = id;
            this.name = "";
        }

        public Person(final int id, @Nonnull final String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Person withName(@Nonnull final String name) {
            return new Person(id, name);
        }
    }

    static class PersonMapper extends EntityMapper<Person> {
        public final EntityType type = entityType(Person.class);
        public final Field<Integer> id = field("id", IntegerMapper.class, "getId", CONSTRUCTOR);
        public final Field<String> name = stringField("name", "getName", "withName");
    }
}
