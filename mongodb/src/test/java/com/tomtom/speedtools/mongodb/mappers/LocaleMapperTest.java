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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@SuppressWarnings("StaticVariableMayNotBeInitialized")
public class LocaleMapperTest {
    private static final Logger LOG = LoggerFactory.getLogger(LocaleMapperTest.class);

    private static LocaleMapper mapper;

    @BeforeClass
    public static void initialize() throws SchemaException {
        final MapperRegistry mapperRegistry = new MapperRegistry();
        mapperRegistry.register(new LocaleMapper());
        mapper = mapperRegistry.getMapper(LocaleMapper.class);
    }

    @Test
    public void testFromDb() throws MapperException {
        LOG.info("testFromDb");

        Assert.assertEquals(Locale.ENGLISH, mapper.fromDb("en"));
        Assert.assertEquals(Locale.US, mapper.fromDb("en_US"));
        Assert.assertEquals(new Locale("nl", "NL"), mapper.fromDb("nl_NL"));
        Assert.assertEquals(new Locale("en", "NL"), mapper.fromDb("en_NL"));
        Assert.assertEquals(new Locale("en", "NL"), mapper.fromDb("en_NL"));
        Assert.assertEquals(new Locale("es", "ES", "Traditional_WIN"), mapper.fromDb("es_ES_Traditional_WIN"));
    }

    @Test
    public void testToDb() throws MapperException {
        LOG.info("testToDb");

        Assert.assertEquals("en", mapper.toDb(Locale.ENGLISH));
        Assert.assertEquals("en_US", mapper.toDb(Locale.US));
        Assert.assertEquals("nl_NL", mapper.toDb(new Locale("nl", "NL")));
        Assert.assertEquals("es_ES_Traditional_WIN", mapper.toDb(new Locale("es", "ES", "Traditional_WIN")));
    }
}
