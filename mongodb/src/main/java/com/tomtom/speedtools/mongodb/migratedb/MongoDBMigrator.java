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

package com.tomtom.speedtools.mongodb.migratedb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tomtom.speedtools.mongodb.MongoDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Class should have an application-specific subclass that registers concrete migrations. Call {@link #migrate(MongoDB,
 * boolean)} to perform the actual migration.
 */
public abstract class MongoDBMigrator {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBMigrator.class);

    public static final String MIGRATOR_COLLECTION_NAME = "migrator";
    public static final String BUSY = "busy";
    public static final String INFO_ID = "info";
    public static final String CURRENT_VERSION = "currentVersion";

    @Nonnull
    private final MongoDBMigration[] migrations;

    protected MongoDBMigrator(@Nonnull final MongoDBMigration... migrations) {
        assert migrations != null;
        assert migrations.length > 0;
        this.migrations = migrations;
    }

    /**
     * Get version of last migrator.
     *
     * @return To-version of last migrator (already trimmed).
     */
    @Nonnull
    public String getLatestToVersion() {
        return migrations[migrations.length - 1].getToVersion();
    }

    /**
     * Migrate the database to the requested toVersion.
     *
     * @param db     Database to migrate.
     * @param dryRun In dry-run mode, no modifications will be made to the database.
     * @return True if the database was modified (other than updating the schema version).
     * @throws MigrationException If an error was encountered during migration.
     */
    public boolean migrate(
            @Nonnull final MongoDB db,
            final boolean dryRun) throws MigrationException {
        assert db != null;
        LOG.info("MigrateDB starting..." + (dryRun ? " (dryRun mode)" : ""));

        final Map<String, MongoDBMigration> migrationMap = new HashMap<>();
        for (final MongoDBMigration migration : migrations) {
            if (migrationMap.put(migration.getFromVersion(), migration) != null) {
                throw new MigrationException("Multiple migrations found with 'from'-version: " +
                        migration.getFromVersion());
            }
        }

        // Read the current version from the database.
        final DBCollection collection = db.getCollection(MIGRATOR_COLLECTION_NAME);
        DBObject info = collection.findOne(new BasicDBObject("_id", INFO_ID));
        if (info == null) {
            info = new BasicDBObject("_id", INFO_ID);
        }
        Object currentVersionObj = info.get(CURRENT_VERSION);
        if (currentVersionObj == null) {
            currentVersionObj = getFirstVersion();
            info.put(CURRENT_VERSION, currentVersionObj);
        }
        final String currentVersion = currentVersionObj.toString().trim();

        // Check whether a previous migration was in progress.
        if (info.get(BUSY) != null) {
            throw new MigrationException("Previous migration was unsuccesful. Please restore database.");
        }

        // Indicate that migration is in progress.
        info.put(BUSY, "true");
        if (!dryRun) {
            info.put(CURRENT_VERSION, getTargetVersion());
            collection.save(info);
        }

        // Create migration path to toVersion.
        final List<MongoDBMigration> migrationPath = new ArrayList<>();
        String version = currentVersion;

        // Create a migration path.
        while (!version.equals(getTargetVersion())) {
            final MongoDBMigration migration = migrationMap.get(version);
            if (migration == null) {
                throw new MigrationException(
                        "No migration possible from version: " + version + " to version " + getTargetVersion());
            }
            migrationPath.add(migration);
            version = migration.getToVersion();
        }

        // Start migrating.
        boolean databaseChanged = false;
        List<MongoDBMigrationProblem> problems = Collections.emptyList();
        for (final MongoDBMigration migration : migrationPath) {
            LOG.info("Migrating database from version " + migration.getFromVersion() +
                    " to version " + migration.getToVersion());
            try {
                migration.setDryRun(dryRun);                                                // Do not change order:
                databaseChanged = migration.migrateChangedDatabase(db) || databaseChanged;  // Always execute migrate!
                problems = migration.flush();
                if (!problems.isEmpty()) {
                    break;
                }
            } catch (final MigrationException e) {
                LOG.error("Migration failed, please restore database from backup: " + e.getMessage());
                throw e;
            } catch (final RuntimeException e) {
                LOG.error("Migration failed, please restore database from backup: " + e.getMessage());
                if (e.getCause() instanceof MigrationException) {
                    throw (MigrationException) e.getCause();
                }
                throw new MigrationException(e);
            }
        }

        // Close migration.
        info.put(CURRENT_VERSION, getTargetVersion());
        info.removeField(BUSY);
        if (!dryRun) {
            collection.save(info);
        }

        // Show problems.
        if (!problems.isEmpty()) {
            final StringBuilder problemString = new StringBuilder();
            problemString.append("Migration problems encountered:");
            for (final MongoDBMigrationProblem problem : problems) {
                problemString.append("\n  ").append(problem.getPath()).append(" - ").append(problem
                        .getProblem());
            }
            final String str = problemString.toString();
            LOG.error(str);
        } else {
            LOG.info("Migration OK");
        }

        // Dry-run info.
        if (dryRun) {
            LOG.info("Migration was run in dry-run mode. No modifications were made to the database.");
            return false;
        }

        // Real mode.
        if (databaseChanged) {
            LOG.info("Database records have been modified (and schema version was updated).");
        } else {
            LOG.info("No database records have been modified (but schema version was updated).");
        }

        // Now, throw an exception is something was wrong.
        if (!problems.isEmpty()) {
            throw new MigrationException("Migration was not successful. Please restore database.");
        }
        return databaseChanged;
    }

    public boolean checkCurrentVersion(@Nonnull final MongoDB db) {
        assert db != null;
        final DBCollection collection = db.getCollection(MIGRATOR_COLLECTION_NAME);
        DBObject info = collection.findOne(new BasicDBObject("_id", INFO_ID));
        if (info == null) {
            info = new BasicDBObject("_id", INFO_ID);
        }
        Object currentVersionObj = info.get(CURRENT_VERSION);
        if (currentVersionObj == null) {
            currentVersionObj = getFirstVersion();
            info.put(CURRENT_VERSION, getFirstVersion());
        }
        final String currentVersion = currentVersionObj.toString().trim();
        LOG.debug("checkCurrentVersion: Database version: {}, expecting: {} {}",
                currentVersion, getTargetVersion(),
                ((currentVersion.equals(getTargetVersion())) ? "(OK)" : "(ERROR)"));
        if (!currentVersion.equals(getTargetVersion())) {
            LOG.error("checkCurrentVersion: Database has wrong version" +
                            (currentVersion.equals(getFirstVersion()) ? " (or may be empty)" : "") +
                            ": {}, expected: {}", currentVersion,
                    getTargetVersion());
            return false;
        }
        return true;
    }

    /**
     * Return the 'first'-version.
     *
     * @return Trimmed version.
     */
    @Nonnull
    public abstract String getFirstVersion();

    /**
     * Return the 'target'-version.
     *
     * @return Trimmed version.
     */
    @Nonnull
    public abstract String getTargetVersion();
}
