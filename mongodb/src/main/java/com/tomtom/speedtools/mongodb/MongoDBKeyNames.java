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

import javax.annotation.Nonnull;

/**
 * Constants used for MongoDB related stuff.
 */
public final class MongoDBKeyNames {

    /**
     * Record ID.
     */
    @Nonnull
    public static final String ID_KEY = "_id";

    /**
     * Record version number.
     */
    @Nonnull
    public static final String VERSION_KEY = "_ver";

    /**
     * Type discrimination field.
     */
    @Nonnull
    public static final String DISCRIMINATOR_KEY = "_type";

    /**
     * Last modification date/time stamp.
     */
    @Nonnull
    public static final String LAST_MODIFIED_KEY = "_modified";

    private MongoDBKeyNames() {
        // Prevent instantiation.
    }
}
