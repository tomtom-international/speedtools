/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper around MongoDB, to enable the concept of sub-databases, implemented as collection prefixes.
 */
public class MongoDB {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(MongoDB.class);

    @Nonnull
    private final DB db;
    @Nonnull
    private final String collectionPrefix;

    /**
     * Constructor.
     *
     * @param db              Database to wrap.
     * @param subDatabaseName Sub-database name, or "".
     */
    public MongoDB(@Nonnull final DB db, @Nonnull final String subDatabaseName) {
        assert db != null;
        assert subDatabaseName != null;
        this.db = db;
        if (subDatabaseName.trim().isEmpty()) {
            this.collectionPrefix = "";
        } else {
            this.collectionPrefix = subDatabaseName + '_'; // Hyphen causes problems in MongoDB shell.
        }
    }

    @Nonnull
    public DB getDB() {
        return db;
    }

    @Nonnull
    public Iterable<String> getCollectionNames() {
        final List<String> collectionNames = new ArrayList<>();
        for (final String name : db.getCollectionNames()) {
            if (name.startsWith(collectionPrefix)) {
                collectionNames.add(name.substring(collectionPrefix.length()));
            }
        }
        return collectionNames;
    }

    @Nonnull
    public DBCollection getCollection(@Nonnull final String name) {
        assert name != null;
        return db.getCollection(collectionPrefix + name);
    }

    /**
     * Returns server version.
     *
     * @return Server version.
     */
    @Nonnull
    public String getServerVersion() {
        final CommandResult result = db.doEval("db.version()");
        return result.getString("retval");
    }

    /**
     * Method checks if the server version is sufficient.
     *
     * @param minimalServerVersion The minimal server version in X.X.X format
     * @return True in case server version is sufficient, false otherwise.
     */
    public boolean isServerVersionAtLeast(final @Nonnull String minimalServerVersion) {
        assert minimalServerVersion != null;

        boolean sufficient = false;
        final String serverVersion = getServerVersion();

        LOG.debug("isServerVersionAtLeast: required={}, actual={}", minimalServerVersion, serverVersion);

        // Check input format.
        final String[] versionBits = minimalServerVersion.split("\\.");
        for (final String versionBit : versionBits) {
            try {
                Integer.valueOf(versionBit);
            } catch (final NumberFormatException e) {
                LOG.error("isServerVersionAtLeast: minimal server version has wrong syntax, version={}",
                        minimalServerVersion, e);
                return false;
            }
        }

        final String[] serverVersionBits = serverVersion.split("\\.");

        final int length = Math.min(versionBits.length, serverVersionBits.length);

        boolean check = true;
        int i = 0;
        while (check) {
            int versionBit = 0;
            if (versionBits.length > i) {
                versionBit = Integer.valueOf(versionBits[i]);
            }

            int serverVersionBit = 0;
            if (serverVersionBits.length > i) {
                serverVersionBit = Integer.valueOf(serverVersionBits[i]);
            }

            if (serverVersionBit > versionBit) {
                sufficient = true;
                check = false;
            } else if (serverVersionBit < versionBit) {
                sufficient = false;
                check = false;
            } else {
                i++;
                if (i > length) {
                    // Last loop. Versions are identical.
                    sufficient = true;
                    check = false;
                }
            }
        }

        return sufficient;
    }
}
