/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructorMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConstructorMapperTest.class);

    @Test(expected = SchemaException.class)
    public void testContructorFieldsNoContructor() throws SchemaException, MapperException {
        LOG.info("testContructorFieldsNoContructor");

        class NonAbstractEntity {
        }

        //noinspection unchecked
        class IllegalMapper extends EntityMapper<NonAbstractEntity> {
            public final EntityType entityType = entityType(NonAbstractEntity.class);
            public final Field<String> f = stringField("bla", "getBla", CONSTRUCTOR);
        }

        final MapperRegistry registry = new MapperRegistry();
        final IllegalMapper illegalMapper = new IllegalMapper();
        registry.register(illegalMapper);
    }
}