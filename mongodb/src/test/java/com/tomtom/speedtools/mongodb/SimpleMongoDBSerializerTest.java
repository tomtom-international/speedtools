/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

import com.mongodb.DBObject;
import com.tomtom.speedtools.json.SimpleJsonSerializer;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SimpleMongoDBSerializerTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleMongoDBSerializerTest.class);

    @Test
    public void testPerson() {
        LOG.info("testPerson");

        final DateTime now = UTCTime.now();

        final Woman jane = new Woman("jane", now, null, Gender.FEMALE, Collections.<Person>emptyList(), true,
                Collections.<Set<Person>>emptyList(), new Object[0]);
        final Person joe = new Person("joe", now, jane, Gender.MALE, Collections.<Person>emptyList(), new Object[0]);
        final Person jim = new Person("jim", now, jane, Gender.MALE, Collections.<Person>emptyList(), new Object[0]);
        final Person mary = new Woman("mary", now, jane, Gender.MALE, Immutables.listOf(joe, jim), false,
                Collections.singletonList(Immutables.setOf(jane, joe)), new Object[]{"A String", joe});

        final Object dbValue = SimpleMongoDBSerializer.getInstance().serialize(mary);
        LOG.info("Serialized: " + SimpleJsonSerializer.getInstance().toString(mary));
        Assert.assertTrue(dbValue instanceof DBObject);

        final Object person = SimpleMongoDBSerializer.getInstance().deserialize(dbValue);
        LOG.info("Deserialized: " + SimpleJsonSerializer.getInstance().toString(person));

        Assert.assertTrue(person instanceof Woman);
        final Woman woman = (Woman) person;
        Assert.assertNotNull(woman.getFavorites());
        Assert.assertEquals(1, woman.getFavorites().size());
        Assert.assertEquals(2, woman.getFavorites().get(0).size());
        Assert.assertSame(Woman.class, woman.getFavorites().get(0).iterator().next().getClass());
        Assert.assertNotNull(woman.getThings());
        Assert.assertEquals(2, woman.getThings().length);
        Assert.assertEquals("A String", woman.getThings()[0]);
        Assert.assertSame(Person.class, woman.getThings()[1].getClass());
    }

    public static class Person {
        @Nonnull
        private final String name;
        @Nonnull
        private final DateTime birthDate;
        @Nullable
        private final Woman mother;
        @Nonnull
        private final Gender gender;
        @Nonnull
        private final List<Person> friends;
        @Nonnull
        private final Object[] things;

        public Person(
                @Nonnull final String name,
                @Nonnull final DateTime birthDate,
                @Nullable final Woman mother,
                @Nonnull final Gender gender,
                @Nonnull final List<Person> friends,
                @Nonnull final Object[] things) {
            this.name = name;
            this.birthDate = birthDate;
            this.mother = mother;
            this.gender = gender;
            this.friends = friends;
            this.things = things;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public DateTime getBirthDate() {
            return birthDate;
        }

        @Nullable
        public Woman getMother() {
            return mother;
        }

        @Nonnull
        public Gender getGender() {
            return gender;
        }

        @Nonnull
        public List<Person> getFriends() {
            return friends;
        }

        @Nonnull
        public Object[] getThings() {
            return things;
        }
    }

    public static class Woman extends Person {
        private final boolean prefersDresses;
        private final List<Set<Person>> favorites;

        public Woman(
                @Nonnull final String name,
                @Nonnull final DateTime birthDate,
                @Nullable final Woman mother,
                @Nonnull final Gender gender,
                @Nonnull final List<Person> friends,
                final boolean prefersDresses,
                @Nonnull final List<Set<Person>> favorites,
                @Nonnull final Object[] things) {
            super(name, birthDate, mother, gender, friends, things);
            this.prefersDresses = prefersDresses;
            this.favorites = favorites;
        }

        public boolean getPrefersDresses() {
            return prefersDresses;
        }

        @Nonnull
        public List<Set<Person>> getFavorites() {
            return favorites;
        }
    }

    public static enum Gender {MALE, FEMALE}
}
