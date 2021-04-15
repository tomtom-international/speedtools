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

import com.tomtom.speedtools.geometry.GeoArea;
import com.tomtom.speedtools.geometry.GeoObject;
import org.junit.Assert;
import org.junit.Test;

public class InheritanceTest {

    @Test(expected = SchemaException.class)
    public void testCyclicInheritance() throws SchemaException, MapperException {
        class GeoObjectMapper extends EntityMapper<GeoObject> {
            public final EntityType entityType = entityType(GeoObject.class);
            public final SuperEntity superEntity = superEntity(GeoObjectMapper.class);
        }

        final MapperRegistry registry = new MapperRegistry();
        final GeoObjectMapper geoAreaMapper = new GeoObjectMapper();
        registry.register(geoAreaMapper);
        final EntityMapper<GeoArea> subGeoAreaMapper = geoAreaMapper.getSubMapper(GeoArea.class);
        Assert.assertSame(geoAreaMapper, subGeoAreaMapper);
    }

    @Test(expected = SchemaException.class)
    public void testSpuriousInheritance() throws SchemaException, MapperException {
        //noinspection unchecked
        class GeoObjectMapper extends EntityMapper<GeoObject> {
            public final EntityType entityType = entityType(GeoObject.class);
            public final SuperEntity superEntity = superEntity(
                    (Class<? extends EntityMapper<? super GeoObject>>) (Class) GeoAreaMapper.class);
        }
        class GeoAreaMapper extends EntityMapper<GeoArea> {
            public final EntityType entityType = entityType(GeoArea.class);
            public final SuperEntity superEntity = superEntity(GeoObjectMapper.class);
        }

        final MapperRegistry registry = new MapperRegistry();
        final GeoObjectMapper geoAreaMapper = new GeoObjectMapper();
        registry.register(geoAreaMapper);
        final EntityMapper<GeoArea> subGeoAreaMapper = geoAreaMapper.getSubMapper(GeoArea.class);
        Assert.assertSame(geoAreaMapper, subGeoAreaMapper);
    }
}
