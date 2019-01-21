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

package com.tomtom.speedtools.mongodb;


import com.mongodb.DBCursor;

import javax.annotation.Nonnull;

public class MongoDBPaging {

    public final static int DEFAULT_OFFSET = 0;
    public final static int DEFAULT_COUNT = 0; // Unlimited.

    public final static int MIN_OFFSET = 0;
    public final static int MIN_COUNT = 0;

    public static final MongoDBPaging DEFAULT_INSTANCE = new MongoDBPaging();

    private final int offset;
    private final int count;

    public MongoDBPaging() {
        this(DEFAULT_OFFSET, DEFAULT_COUNT);
    }

    /**
     * Constructor that sets offset and count. A value of zero for count means unlimited.
     *
     * @param offset The first element to return.
     * @param count  The maximum number of items.
     */
    public MongoDBPaging(final int offset, final int count) {
        super();

        assert offset >= MIN_OFFSET;
        assert count >= MIN_COUNT;

        this.offset = offset;
        this.count = count;
    }

    /**
     * Method applies offset and returns new instance.
     *
     * @param offset The first element to return.
     * @return MongoDBPaging instance with offset.
     */
    @Nonnull
    public MongoDBPaging offset(final int offset) {
        return new MongoDBPaging(offset, count);
    }

    /**
     * Method applies count and returns new instance. A value of zero means unlimited.
     *
     * @param count The maximum number of items to return.
     * @return MongoDBPaging instance with count.
     */
    @Nonnull
    public MongoDBPaging count(final int count) {
        return new MongoDBPaging(offset, count);
    }

    /**
     * Method applies the paging if any to given {@link DBCursor}.
     *
     * @param dbCursor {@link DBCursor} to apply paging to.
     * @return {@link DBCursor} with paging.
     */
    @Nonnull
    public DBCursor apply(@Nonnull final DBCursor dbCursor) {
        assert dbCursor != null;

        // Check if paging should be applied.
        if ((offset != DEFAULT_OFFSET) || (count != DEFAULT_COUNT)) {
            return dbCursor.skip(offset).limit(count);
        }
        return dbCursor;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }
}
