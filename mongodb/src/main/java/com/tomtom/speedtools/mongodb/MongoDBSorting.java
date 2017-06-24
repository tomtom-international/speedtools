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

package com.tomtom.speedtools.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper;

import javax.annotation.Nonnull;

public class MongoDBSorting {
    public static final int ASC = 1;
    public static final int DESC = -1;

    @Nonnull
    public static final MongoDBSorting DEFAULT_INSTANCE = new MongoDBSorting();

    @Nonnull
    private final BasicDBObject sortDBObject;

    public MongoDBSorting() {
        sortDBObject = new BasicDBObject();
    }

    /**
     * Creates an ascending sort object for given field.
     *
     * @param fields Field(s) to be sorted on.
     */
    public MongoDBSorting(@Nonnull final EntityMapper<?>.Field<?>... fields) {
        assert fields != null;
        sortDBObject = new BasicDBObject(MongoDBUtils.mongoPath(fields), ASC);
    }

    /**
     * Private constructor, used by chaining methods.
     *
     * @param sortDBObject Initialized {@link BasicDBObject} containing sorting.
     */
    private MongoDBSorting(@Nonnull final BasicDBObject sortDBObject) {
        assert sortDBObject != null;
        this.sortDBObject = sortDBObject;
    }

    /**
     * Method constructs a new instance of a MongoDBSorting object and adds ascending sorting to initial instance.
     *
     * @param fields Field(s) to be sorted on.
     * @return A new MongoDBSorting instance with given ascending sort added.
     */
    @Nonnull
    public MongoDBSorting ascending(@Nonnull final EntityMapper<?>.Field<?>... fields) {
        assert fields != null;

        return new MongoDBSorting(sortDBObject.append(MongoDBUtils.mongoPath(fields), ASC));
    }

    /**
     * Method constructs a new instance of a MongoDBSorting object and adds descending sorting to initial instance.
     *
     * @param fields Field(s) to be sorted on.
     * @return A new MongoDBSorting instance with given descending sort added.
     */
    @Nonnull
    public MongoDBSorting descending(@Nonnull final EntityMapper<?>.Field<?>... fields) {
        assert fields != null;

        return new MongoDBSorting(sortDBObject.append(MongoDBUtils.mongoPath(fields), DESC));
    }

    /**
     * Method applies the sorting if any to given {@link DBCursor}.
     *
     * @param dbCursor {@link DBCursor} to apply sorting to.
     * @return {@link DBCursor} with sorting.
     */
    @Nonnull
    public DBCursor apply(@Nonnull final DBCursor dbCursor) {
        assert dbCursor != null;

        // Check if sorting should be applied.
        if (!sortDBObject.isEmpty()) {
            return dbCursor.sort(sortDBObject);
        }
        return dbCursor;
    }

    @Nonnull
    public DBObject toDBObject() {
        return sortDBObject;
    }
}
