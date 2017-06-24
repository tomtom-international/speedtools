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


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumerationMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(EnumerationMapperTest.class);

    static enum Color {
        RED, BLUE, YELLOW
    }

    static class ColorMapper extends EnumerationMapper<Color> {
        private final Value red = value(Color.RED, "red");
        private final Value blue = value(Color.BLUE, "blue");
        private final Value yellow = value(Color.YELLOW, "yell");
    }

    static class IncompleteColorMapper extends EnumerationMapper<Color> {
        private final Value red = value(Color.RED, "red");
        private final Value blue = value(Color.BLUE, "blue");
    }

    static class NonUniqueColorMapper extends EnumerationMapper<Color> {
        private final Value red = value(Color.RED, "red");
        private final Value blue = value(Color.BLUE, "blue");
        private final Value yellow = value(Color.YELLOW, "blue");
    }

    @Test
    public void testEnumerationMapper() throws SchemaException, MapperException {
        LOG.info("testEnumerationMapper");
        final MapperRegistry registry = new MapperRegistry();
        registry.register(new ColorMapper());

        final ColorMapper mapper = registry.getMapper(ColorMapper.class);
        Assert.assertEquals("red", mapper.red.getDbValue());
        Assert.assertEquals(Color.RED, mapper.fromDb("red"));
        Assert.assertEquals("red", mapper.toDb(Color.RED));
        Assert.assertEquals("blue", mapper.blue.getDbValue());
        Assert.assertEquals(Color.BLUE, mapper.fromDb("blue"));
        Assert.assertEquals("blue", mapper.toDb(Color.BLUE));
        assert mapper.yellow != null;
    }

    @Test(expected = SchemaException.class)
    public void testIncompleteEnumerationMapper() throws SchemaException {
        LOG.info("testIncompleteEnumerationMapper");
        final MapperRegistry registry = new MapperRegistry();
        final IncompleteColorMapper incompleteColorMapper = new IncompleteColorMapper();
        registry.register(incompleteColorMapper);
        assert incompleteColorMapper.red != null;
        assert incompleteColorMapper.blue != null;
    }

    @Test(expected = SchemaException.class)
    public void testNonUniqueEnumerationMapper() throws SchemaException {
        LOG.info("testNonUniqueEnumerationMapper");
        final MapperRegistry registry = new MapperRegistry();
        final NonUniqueColorMapper nonUniqueColorMapper = new NonUniqueColorMapper();
        registry.register(nonUniqueColorMapper);
        assert nonUniqueColorMapper.red != null;
        assert nonUniqueColorMapper.blue != null;
        assert nonUniqueColorMapper.yellow != null;
    }
}
