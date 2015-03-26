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

package com.tomtom.speedtools.mongodb;

import com.mongodb.*;
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

    /**
     * Authenticate to the MongoDB if necessary.
     *
     * @param mongo        MongoDB database instance (required for initial authentication).
     * @param databaseName Database name to authenticate (user are setup per database).
     * @param userName     User name.
     * @param password     Password.
     * @throws MongoDBConnectionException If something went wrong during authentication.
     * @throws MongoException             If something went wrong, other than authentication.
     */
    public static void authenticateIfRequired(
            @Nonnull final Mongo mongo,
            @Nonnull final String databaseName,
            @Nonnull final String userName,
            @Nonnull final String password) {
        assert mongo != null;
        assert databaseName != null;
        assert userName != null;
        assert password != null;

        // Get the target database.
        final DB db = mongo.getDB(databaseName);
        if (db.isAuthenticated()) {
            LOG.debug("authenticateIfRequired: Already authenticated, db={}", databaseName);
            return;
        }

        /**
         * Try to fetch the global database names list. If this fails, the database is run with --auth and requires
         * authentication. If this succeeds, the database is run with --noauth and the individual databases do not require authentication.
         */
        try {
            mongo.getDatabaseNames();
            LOG.debug("authenticateIfRequired: No authentication required to retrieve database names, connection={}",
                    mongo.getConnectPoint());
            return;
        } catch (final MongoException ignored) {
            LOG.debug("authenticateIfRequired: Could not get database names, connection={}", mongo.getConnectPoint());
        }

        /**
         * Get the correct database.
         * If this succeeds, the database does not require authentication (in fact, it will actively refuse it,
         * so we should not even try).
         */
        try {
            db.getCollectionNames();
            LOG.debug("authenticateIfRequired: No authentication required to retrieve collection names, db={}",
                    db.getName());
        } catch (final MongoException ignored) {
            LOG.debug("authenticateIfRequired: Could not get collection names, db={}", db.getName());
        }

        // This is OK. The database requires us to authenticate before we continue.
        try {
            final boolean authenticated = db.authenticate(userName, password.toCharArray());
            if (!authenticated) {
                final String message = "Cannot authenticate, db=" + db.getName() + ", user=" + userName;
                LOG.error("authenticateIfRequired: " + message);
                throw new MongoDBConnectionException(message);
            }
        } catch (final MongoException e) {
            final String message = "Technical authentication failure, db=" + db.getName() + ", user=" + userName;
            LOG.error("authenticateIfRequired: " + message, db.getName(), userName);
            throw new MongoDBConnectionException(message, e);
        }
        LOG.debug("authenticateIfRequired: Succesfully authenticated, db={}, user={}", db.getName(), userName);
    }

}
