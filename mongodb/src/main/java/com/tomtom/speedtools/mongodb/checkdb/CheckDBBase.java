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

package com.tomtom.speedtools.mongodb.checkdb;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.geometry.Geo;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.geometry.GeoRectangle;
import com.tomtom.speedtools.mongodb.MongoDB;
import com.tomtom.speedtools.mongodb.mappers.*;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper.Field;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper.HasFieldName;
import com.tomtom.speedtools.mongodb.migratedb.MongoDBMigrator;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.objects.Tuple;
import com.tomtom.speedtools.thread.WorkQueue;
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.utils.AddressUtils;
import com.tomtom.speedtools.utils.MathUtils;
import com.tomtom.speedtools.utils.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import static com.tomtom.speedtools.mongodb.MongoDBUtils.mongoPath;


/**
 * This class contains generic helpers for checking database consistency. It is defined as an abstract base class from
 * which you can derive your own "CheckDB" class. That class will have access to all of these methods by inheritance.
 * Not quite what inheritance is meant for, but it works out quite OK this way, as it keeps the domain model specific
 * database checking code relatively clean and not cluttered with the framework sub-classes that are found here.
 *
 * To create a database checker, create a class CheckDB which inherits from CheckDBBase.
 *
 * The class CheckDB contains a method checkAllCollections() to check the consistency of all collections in the FWS
 * database.
 *
 * The way it works is like this: for every collection, a simple query statement is issued to query every record from
 * that collection. Every record is then run through a standard database entity mapper, to obtain a type-safe variant of
 * the record.
 *
 * Any failures from that conversion are accumulated and stored. Once the type-safe variant is available, every field of
 * the record is checked individually for correct range and referential integrity. Again, any errors found during these
 * checks, are accumulated and returned at the end of the check.
 *
 * The implementation has been optimized to check database records of a SINGLE collection in parallel using threads. It
 * uses a {@link WorkQueue} of RecordChecker work load items.
 *
 * Note that the implementation is only thread-safe for checking multiple records within a SINGLE collection at once.
 * There is shared data about the collection itself and the 'current record pointer', so these shared values should
 * never be modified inside the threads.
 *
 * Final remark: Checking database consistency without producing ton of lines of code is hard. We've tried several ways
 * to crack this problem and came up with this one. However, the way the resulting code looks might not seem to be very
 * (or remotely) readable at first sight. Try and invest time and effort in mastering the techniques used here. Once you
 * get used to it, it is by far the most compact way of testing database integrity we've tried so far. And it is
 * relatively straightforward to keep it in line with the other source.
 */
@SuppressWarnings(
        {"NestedTryStatement", "ConstantConditions", "NumericCastThatLosesPrecision"})
abstract public class CheckDBBase {
    private static final Logger LOG = LoggerFactory.getLogger(CheckDBBase.class);

    @Nonnull
    protected final MongoDB db;
    @Nonnull
    protected final MongoDBMigrator migrateDB;
    @Nonnull
    protected final MapperRegistry mapperRegistry;

    @Nonnull
    protected final List<String> internalErrors = Collections.synchronizedList(new ArrayList<>());
    @Nonnull
    protected final List<Error> errors = Collections.synchronizedList(new ArrayList<>());
    @Nonnull
    protected final List<Error> warnings = Collections.synchronizedList(new ArrayList<>());
    @Nonnull
    protected final AtomicLong nrTotalChecks = new AtomicLong(0);

    protected long nrTotalRecords = 0;
    protected long nrTotalCollections = 0;

    /**
     * Some limits.
     */
    @Nonnull
    public static final DateTime DB_DATE_MAX = new DateTime(2200, 1, 1, 0, 0);
    @Nonnull
    public static final DateTime DB_DATE_MIN = new DateTime(1900, 1, 1, 0, 0);
    public static final double DB_LAT_MAX = 90.0;
    public static final double DB_LAT_MIN = -90.0;
    public static final double DB_LON_MAX = Geo.LON180;
    public static final double DB_LON_MIN = -180.0;

    // For progress updates only.
    @Nonnull
    protected static final DateTime firstUpdate = UTCTime.now();

    @SuppressWarnings("StaticNonFinalField")
    @Nonnull
    protected static DateTime lastUpdate = UTCTime.now();

    // Max. number work packages in work queue.
    protected static final int MAX_QUEUE_SIZE = 10000;

    // The thread pool to execute record checker in.
    @Nullable
    protected WorkQueue workQueue;

    // Dummy ID.
    protected static final Uid<Object> DUMMY_RECORD_ID = Uid.fromString("0-0-0-0-0");

    // Used for short-hand writing of x = a || b.
    @SuppressWarnings("UnusedDeclaration")
    protected boolean x;

    @Inject
    protected CheckDBBase(
            @Nonnull final MongoDB db,
            @Nonnull final MongoDBMigrator migrateDB,
            @Nonnull final MapperRegistry mapperRegistry) {
        assert db != null;
        assert mapperRegistry != null;

        this.db = db;
        this.migrateDB = migrateDB;
        this.mapperRegistry = mapperRegistry;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Check collections methods
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Execute all checks.
     *
     * @param checkAllCollections Used to check collection.
     * @return Problem report.
     */
    @Nonnull
    public Report checkAllCollectionsInQueue(final Runnable checkAllCollections) {
        if (!migrateDB.checkCurrentVersion(db)) {
            LOG.error("");
            LOG.error("The database does not have the right version (or may be empty)!");

            final DBCollection collection = db.getCollection(MongoDBMigrator.MIGRATOR_COLLECTION_NAME);
            errors.add(new Error(new Uid("0-0-0-0-0"), collection.getFullName(),
                    "Incorrect version or empty database", null));
        }

        LOG.info("Checking all collections; all errors are collected and shown at end of run");
        workQueue = new WorkQueue(MAX_QUEUE_SIZE);
        try {
            checkAllCollections.run();
            assert workQueue.isEmptyAndFinished();
        } catch (final AssertionError e) {
            LOG.error("Unexpected assertion error encountered.", e);
            throw e;
        } catch (final Exception e) {
            LOG.error("Unexpected exception encountered.", e);
            internalErrors.add("Unexpected exception encountered: " + e.getMessage());
        } finally {
            workQueue.scheduleShutdown();
            for (final Exception e : workQueue.getExceptions()) {
                internalErrors.add("Exceptions found: " + e.getMessage());
            }
        }

        if (!internalErrors.isEmpty()) {
            LOG.error("");
            LOG.error("The following fields were never checked in CheckDB:");
            for (final String line : internalErrors) {
                LOG.error("   {}", line);
            }
            LOG.error("");
            LOG.error("CheckDB has not been updated after a domain model change!");
            LOG.error("");
        }
        return new Report(
                nrTotalChecks.get(),
                nrTotalCollections,
                nrTotalRecords,
                errors,
                warnings,
                !internalErrors.isEmpty());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Utility methods.
    // -----------------------------------------------------------------------------------------------------------------

    public void showProgressCollectionStart(
            @Nonnull final DBCollection collection, final int count) {
        assert collection != null;
        LOG.debug("");
        LOG.debug("{}:", collection.getFullName());
        LOG.info("  |   0% -- processed 0 of {} records ({} errors and {} warnings found so far) - {}", count,
                errors.size(), warnings.size(), errors.isEmpty() ? "OK" : "not OK");
    }

    public void showProgressCheckRecord(
            @Nonnull final String id,
            final long nr,
            final int count) {
        assert id != null;
        assert nr >= 0;
        assert count >= 0;
        LOG.debug("  | _id={}", id);
        final DateTime now = UTCTime.now();
        if (lastUpdate.plusSeconds(3).isBefore(now)) {
            lastUpdate = now;
            final long msecs = lastUpdate.getMillis() - firstUpdate.getMillis();
            final float pct = Math.max(1.0f, Math.round(((float) nr / count) * 100.0f));
            final long eta = ((long) ((float) msecs / (pct / 100)) - msecs) / 1000;
            final String pctStr = String.format("%3d", (int) pct);
            LOG.info(
                    "  | {}% -- processed {} of {} records (+/- {}s to go, {} errors and {} warnings found so far) - {}",
                    pctStr, nr, count, eta, errors.size(), warnings.size(), errors.isEmpty() ? "OK" : "not OK");
        }
    }

    public void showProgressCollectionEnd(
            @Nonnull final DBCollection collection, final int count) {
        assert collection != null;
        LOG.debug("  |");
        LOG.debug("  | {}:", collection.getName());
        LOG.debug("  | {} errors were found in {} records", errors.size(), nrTotalRecords);
        LOG.info("  | 100% -- processed {} of {} records ({} errors and {} warnings found so far) - {}", count, count,
                errors.size(), warnings.size(), errors.isEmpty() ? "OK" : "not OK");
    }

    public static class Report {
        public final long nrChecks;
        public final long nrCollections;
        public final long nrRecords;
        public final List<Error> databaseErrors;
        public final List<Error> databaseWarnings;
        public final boolean internalError;

        public Report(
                final long nrChecks,
                final long nrCollections,
                final long nrRecords,
                @Nonnull final List<Error> databaseErrors,
                @Nonnull final List<Error> databaseWarnings,
                final boolean internalError) {
            assert nrChecks >= 0;
            assert nrCollections >= 0;
            assert nrRecords >= 0;
            assert databaseErrors != null;
            this.nrChecks = nrChecks;
            this.nrCollections = nrCollections;
            this.nrRecords = nrRecords;
            this.databaseErrors = databaseErrors;
            this.databaseWarnings = databaseWarnings;
            this.internalError = internalError;
        }
    }

    /**
     * Container class for errors.
     */
    public static class Error {
        @Nullable
        protected final String recordId;
        @Nonnull
        protected final String collectionName;
        @Nullable
        protected final String message;
        @Nullable
        protected final Exception exception;

        public Error(
                @Nullable final String recordId,
                @Nonnull final String collectionName,
                @Nullable final String message,
                @Nullable final Exception exception) {
            assert collectionName != null;
            this.collectionName = collectionName;
            this.recordId = recordId;
            this.message = message;
            this.exception = exception;
        }

        public Error(
                @Nonnull final Uid<?> recordId,
                @Nonnull final String collectionName,
                @Nullable final String message,
                @Nullable final Exception exception) {
            this(recordId.toString(), collectionName, message, exception);
        }

        @Nullable
        public String getRecordId() {
            return recordId;
        }

        @Nonnull
        public String getCollectionName() {
            return collectionName;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        @Nullable
        public Exception getException() {
            return exception;
        }

        @Override
        @Nonnull
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("collection=");
            sb.append(collectionName);
            if (recordId != null) {
                sb.append(", _id=");
                sb.append(recordId);
            }
            if (message != null) {
                sb.append(", \"");
                sb.append(message);
                sb.append('"');
            }
            if (exception != null) {
                sb.append(", exception=");
                sb.append(exception);
            }
            return sb.toString();
        }
    }

    /**
     * Helper class to use do-while-loops instead of while-loops.
     */
    public static class Iter<T> {
        protected final Iterator<T> it;
        protected final boolean empty;
        protected boolean started = false;
        protected boolean finished = false;

        public Iter(@Nullable final Collection<T> collection) {
            it = (collection == null) ? Immutables.<T>emptyList().iterator() : collection.iterator();
            empty = !it.hasNext();
        }

        public T next() {
            assert started || (!started && !finished);
            started = true;
            if (empty) {
                return null;
            }
            return it.next();
        }

        public boolean hasNext() {
            finished = it.hasNext();
            return finished;
        }
    }

    /**
     * Helper class to check a collection.
     */
    @SuppressWarnings("rawtypes")
    public class CollectionChecker {
        @Nonnull
        protected final DBCollection collection;
        @Nullable
        protected final DBCursor cursor;
        @Nonnull
        protected final EntityMapper mapper;

        /**
         * The set allFields will grow to contain all fields, based on mappers, which are removed one by one by the
         * 'field' method. When record checking is done, the set is checked for non-checked fields.
         *
         * This field is directly referenced in threads, so must be thread safe.
         */
        @Nonnull
        protected final Set<Field> allFields =
                Collections.synchronizedSet(new HashSet<>());

        protected long nrRecordsInCollection = 0;
        protected int count = 0;
        protected final boolean isEmpty;
        protected final String collectionName;
        protected final List<UniquenessChecker<?>> uniquenessCheckers = new LinkedList<>();

        public CollectionChecker(
                @Nonnull final String collectionName,
                @Nonnull final EntityMapper<?> mapper) {
            super();
            assert collectionName != null;
            assert mapper != null;

            this.collectionName = collectionName;
            collection = db.getCollection(collectionName);
            cursor = collection.find();
            count = cursor.count();
            this.mapper = mapper;
            for (final Field field : mapper.getFields()) {
                allFields.add(field);
            }
            this.isEmpty = !cursor.hasNext();
            showProgressCollectionStart(collection, count);
            ++nrTotalCollections;

            // First update should take a bit longer.
            lastUpdate = UTCTime.now().plusSeconds(5);
            assert workQueue.isEmptyAndFinished();
        }

        public boolean hasNext() {
            return cursor.hasNext();
        }

        @Nullable
        public <T> Tuple<Uid<?>, T> next() {
            return next(true);
        }

        @Nullable
        public <T> Tuple<Uid<?>, T> next(final boolean recordIdIsUUID) {

            // Return immediately for empty collection.
            if (isEmpty) {
                return null;
            }

            // Get next record.
            ++nrTotalRecords;
            ++nrRecordsInCollection;
            final DBObject record = cursor.next();
            final String mongoId = record.get("_id").toString();
            final Uid<Object> recordId;
            if (recordIdIsUUID) {
                try {
                    recordId = Uid.fromString(mongoId);
                } catch (final IllegalArgumentException ignored) {
                    error(DUMMY_RECORD_ID, "Incorrectly formatted _id found in record (not a UUID): " + mongoId,
                            null, null
                    );
                    return null;
                }
            } else {
                recordId = DUMMY_RECORD_ID;
            }
            showProgressCheckRecord(mongoId, nrRecordsInCollection, count);

            try {
                @SuppressWarnings("unchecked")
                final T entity = (T) mapper.fromDb(record);
                if (entity != null) {
                    return new Tuple<>(recordId, entity);
                } else {
                    if (!isEmpty) {
                        error(recordId, "Mapped entity is null", null, null);
                    }
                }
            } catch (final ClassCastException e) {
                error(recordId, "Incorrect record type found", null, new Exception(e));
            } catch (final MapperException e) {
                error(recordId, "Could not map entity", null, new Exception(e));
                for (final MapperError mapperError : e.getMapperErrors()) {
                    error(recordId, mapperError.toString(), null, null);
                }
            } catch (final MongoException e) {
                error(recordId, "MongoDB exception", null, new Exception(e));
            }
            return null;
        }

        public void done() {
            // Wait for all record checkers to finish.
            workQueue.waitUntilFinished();
            assert workQueue.isEmptyAndFinished();

            // Only after all record checkers have finished can we run the uniqueness checkers, otherwise not all values
            // are present yet that need to be checked.
            for (final UniquenessChecker<?> uniquenessChecker : uniquenessCheckers) {
                workQueue.startOrWait(uniquenessChecker);
            }

            // Wait for all uniqueness checkers to finish.
            workQueue.waitUntilFinished();
            assert workQueue.isEmptyAndFinished();

            for (final Field field : allFields) {
                internalErrors.add(collection.getName() + '.' + field.getFieldName());
            }
            showProgressCollectionEnd(collection, count);
        }

        public void error(
                @Nonnull final Uid<?> recordId,
                @Nonnull final String message,
                @Nullable final String fieldPath,
                @Nullable final Exception exception) {
            assert recordId != null;
            assert message != null;

            final String messageAndField = message + ", " + StringUtils.mkRevString(".", mapper) +
                    '.' + ((fieldPath == null) ? "<field unknown>" : fieldPath);
            final Error error = new Error(recordId, collectionName, messageAndField, exception);
            errors.add(error);
            LOG.debug("      --> error {}: {}", errors.size(), messageAndField);
            //noinspection VariableNotUsedInsideIf
            if (exception != null) {
                LOG.debug("      --> error {}: Exception: '{}'", errors.size(), error.getException());
            }
        }

        public void warning(
                @Nonnull final Uid<?> recordId,
                @Nonnull final String message,
                @Nullable final String fieldPath) {
            assert recordId != null;
            assert message != null;

            final String messageAndField = message + ", " + StringUtils.mkRevString(".", mapper) +
                    '.' + ((fieldPath == null) ? "<field unknown>" : fieldPath);
            final Error error = new Error(recordId, collectionName, messageAndField, null);
            warnings.add(error);
            LOG.debug("      --> warnings {}: {}", warnings.size(), messageAndField);
        }

        /**
         * Creates a new {@link UniquenessChecker} for this collection. The returned {@code UniquenessChecker} must only
         * be used for data contained within this collection, otherwise uniqueness violations will be reported on the
         * wrong collection.
         *
         * Values to check for uniqueness can then be added to it using {@link UniquenessChecker#add(Uid,
         * Object) add(Uid, Object)} or {@link UniquenessChecker#add(Uid,
         * Collection) add(Uid, Collection)}. The actual check will be performed once all other checks in this
         * {@code CollectionChecker} have been finished, to ensure that all values to check for uniqueness have been
         * added to the {@code UniquenessChecker}.
         *
         * @param logAsErrors True if violations are logged as errors, false if logged as warnings.
         * @param fieldPath   The field path within the collection that needs to be unique over all the elements of the
         *                    collection.
         * @param <T>         The type of the values to check for uniqueness
         * @return Returns a new {@code UniquenessChecker}.
         */
        @Nonnull
        protected <T> UniquenessChecker<T> createUniquenessChecker(final boolean logAsErrors,
                                                                   @Nonnull final EntityMapper<?>.Field<?>... fieldPath) {
            assert fieldPath != null;
            final UniquenessChecker<T> uniquenessChecker = new UniquenessChecker<>(this, logAsErrors, fieldPath);
            uniquenessCheckers.add(uniquenessChecker);
            return uniquenessChecker;
        }
    }

    public class UniquenessChecker<T> implements Runnable {
        @Nonnull
        protected final Collection<Tuple<Uid<?>, T>> valueTuples =
                new ConcurrentLinkedQueue<>();
        @Nonnull
        protected final CollectionChecker collectionChecker;
        @Nonnull
        protected final HasFieldName[] fieldPath;
        protected final boolean logAsErrors;

        /**
         * Creates a new {@code UniquenessChecker}. The {@link CollectionChecker} of the collection that contains the
         * records that contain the values to check must be passed in, so that uniqueness violations are reported on the
         * correction collection.
         *
         * @param collectionChecker The {@link CollectionChecker} for the collection that contains the values that
         *                          should be checked for uniqueness.
         * @param logAsErrors       True if violations are logged as errors, false if logged as warnings.
         * @param fieldPath         The field path within the collection that needs to be unique over all the elements of
         *                          the collection.
         */
        public UniquenessChecker(
                @Nonnull final CollectionChecker collectionChecker,
                final boolean logAsErrors,
                @Nonnull final HasFieldName... fieldPath) {
            assert collectionChecker != null;
            assert fieldPath != null;
            this.fieldPath = fieldPath;
            this.logAsErrors = logAsErrors;
            this.collectionChecker = collectionChecker;
        }

        /**
         * Adds {@code value} from record with {@code recordId} to the list of values to check for uniqueness. The check
         * is not performed at the point this method is invoked, but only later when {@link #run()} is being called.
         *
         * @param recordId The ID of the record that contains {@code value}.
         * @param value    The value to schedule for the uniqueness check.
         * @return Always returns {@code true}.
         */
        public boolean add(
                @Nonnull final Uid<?> recordId,
                @Nullable final T value) {
            assert recordId != null;

            valueTuples.add(new Tuple<>(recordId, value));
            return true;
        }

        /**
         * Adds all values in {@code valueCollection} from record with {@code recordId} to the list of values to check
         * for uniqueness. The check is not performed at the point this method is invoked, but only later when {@link
         * #run()} is being called.
         *
         * @param recordId        The ID of the record that contains {@code valueCollection}.
         * @param valueCollection The values to schedule for the uniqueness check.
         * @return Always returns {@code true}.
         */
        public boolean add(
                @Nonnull final Uid<?> recordId,
                @Nonnull final Collection<T> valueCollection) {
            assert recordId != null;
            assert valueCollection != null;

            for (final T value : valueCollection) {
                valueTuples.add(new Tuple<>(recordId, value));
            }
            return true;
        }

        /**
         * Performs the actual check for duplicates within the previously {@link #add}ed values.
         */
        @Override
        public void run() {
            final Set<T> set = new HashSet<>();
            for (final Tuple<Uid<?>, T> valueTuple : valueTuples) {
                final T value = valueTuple.getValue2();
                if (!set.add(value)) {
                    final Uid<?> recordId = valueTuple.getValue1();
                    final String message = "Duplicate entities found, value=" + value;
                    if (logAsErrors) {
                        collectionChecker.error(recordId, message, mongoPath(fieldPath), null);
                    } else {
                        collectionChecker.warning(recordId, message, mongoPath(fieldPath));
                    }
                }
            }
            nrTotalChecks.incrementAndGet();
        }
    }

    /**
     * Helper class to check a record within a collection.
     */
    @SuppressWarnings("rawtypes")
    public abstract class RecordCheckerBase implements Runnable {

        /**
         * The queue isEmpty contains flags indicating emptiness for the the current (sub)collections.
         */
        @Nonnull
        protected final BlockingDeque<Boolean> isEmpty = new LinkedBlockingDeque<>();
        @Nonnull
        protected final BlockingDeque<EntityMapper<?>> mappers = new LinkedBlockingDeque<>();

        @Nonnull
        protected final CollectionChecker collectionChecker;
        @Nonnull
        protected Field field = null;
        protected boolean currentRecordInvalid = true;

        protected RecordCheckerBase(
                @Nonnull final CollectionChecker collectionChecker,
                @Nullable final Tuple<Uid<?>, ?> record) {
            super();
            assert collectionChecker != null;
            this.collectionChecker = collectionChecker;
            isEmpty.push(collectionChecker.isEmpty);
            mappers.add(collectionChecker.mapper);
            this.currentRecordInvalid = (record == null);
        }

        @Override
        public abstract void run();

        public <T extends EntityMapper<?>> T sub(
                @Nullable final Object entity,
                @Nonnull final Class<T> clazz) {
            assert clazz != null;

            final T m = mapperRegistry.findMapper(clazz);
            for (final Field field : m.getFields()) {
                collectionChecker.allFields.add(field);
            }
            mappers.push(m);
            isEmpty.push(entity == null);
            assert !(entity instanceof Collection);
            return m;
        }

        public void endsub(@Nonnull final EntityMapper<?> mapper) {
            assert mapper != null;
            assert isEmpty.size() > 1 : "Expected size > 1, got " + isEmpty.size();
            assert mappers.size() > 1 : "Expected size > 1, got " + mappers.size();

            isEmpty.pop();
            final EntityMapper<?> pop = mappers.pop();
            if (!pop.equals(mapper)) {
                error(DUMMY_RECORD_ID,
                        "Mapper expected for: " + mapper + ", " + StringUtils.mkRevString(".", mappers) + '.' + pop);
            }
        }

        public boolean field(@Nonnull final EntityMapper<?>.Field<?> field) {
            assert field != null;

            // Mark field as processed.
            collectionChecker.allFields.remove(field);
            this.field = field;
            final Boolean peek = this.isEmpty.peek();
            assert peek != null;
            final boolean skip = (peek || currentRecordInvalid);
            return !skip;
        }

        public void error(
                @Nonnull final Uid<?> recordId,
                @Nonnull final String message) {
            error(recordId, message, (Exception) null);
        }

        public void error(
                @Nonnull final Uid<?> recordId,
                @Nonnull final Object value,
                @Nonnull final String expected) {
            assert recordId != null;
            assert expected != null;

            String message = "Value '" + value + "' is not valid";
            if (expected != null) {
                message = message + ", expected: " + expected;
            }
            error(recordId, message);
        }

        public void error(
                @Nonnull final Uid<?> recordId,
                @Nonnull final String message,
                @Nullable final Exception exception) {
            final String messageAndField = message + ", " +
                    (StringUtils.mkRevString(".", mappers) + '.' + field);
            final Error error = new Error(recordId, collectionChecker.collectionName, messageAndField, exception);
            errors.add(error);
            LOG.debug("      --> error {}: {}", errors.size(), messageAndField);
            //noinspection VariableNotUsedInsideIf
            if (exception != null) {
                LOG.debug("      --> error {}: Exception: '{}'", errors.size(), error.getException());
            }
        }

        public boolean checkCountryISO2(
                @Nonnull final Uid<?> recordId,
                @Nullable final String countryISO2,
                final boolean required) {
            assert recordId != null;

            if ((countryISO2 != null) && !countryISO2.isEmpty()) {
                if (!AddressUtils.isValidCountryISO2(countryISO2)) {
                    error(recordId, countryISO2, "ISO2 format");
                }
            } else {
                if (required) {
                    error(recordId, "Entity missing but not optional");
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkDate(
                @Nonnull final Uid<?> recordId,
                @Nullable final DateTime dateTime,
                final boolean required) {
            assert recordId != null;

            if (dateTime != null) {
                if (dateTime.isBefore(DB_DATE_MIN) ||
                        dateTime.isAfter(DB_DATE_MAX)) {
                    error(recordId, dateTime, "should be in [" +
                            DB_DATE_MIN + ", " +
                            DB_DATE_MAX + ']');
                }
            } else {
                if (required) {
                    error(recordId, "Entity missing but not optional");
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkRecordId(
                @Nonnull final Uid<?> recordId,
                @Nonnull final Uid<?> id) {
            assert recordId != null;

            if (!id.equals(recordId)) {
                error(recordId, id, "Same value as _id");
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkEqual(
                @Nonnull final Uid<?> recordId,
                @Nonnull final Object o1,
                @Nonnull final Object o2) {
            assert recordId != null;

            if (!o1.equals(o2)) {
                error(recordId, o1, "Same value as: " + o2);
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkDate(
                @Nonnull final Uid<?> recordId,
                @Nullable final LocalDate localDate,
                final boolean required) {
            assert recordId != null;

            return (localDate == null) || checkDate(recordId, UTCTime.from(localDate.toDate()), required);
        }

        public boolean checkString(
                @Nonnull final Uid<?> recordId,
                @Nonnull final String value,
                final int minLen,
                final int maxLen) {
            assert recordId != null;

            if (value != null) {
                if (!MathUtils.isBetween(value.length(), minLen, maxLen)) {
                    error(recordId, "length should be in [" + minLen + ", " + maxLen + "] is " + value.length());
                }
            } else {
                error(recordId, "Entity missing but not optional:" + field);
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkCollectionSize(
                @Nonnull final Uid<?> recordId,
                @Nullable final Collection value,
                final int min,
                final int max,
                final boolean required) {
            assert recordId != null;

            if (value != null) {
                if (!MathUtils.isBetween(value.size(), min, max)) {
                    error(recordId, value.size(), "size should be in [" + min + ", " + max + ']');
                }
            } else {
                if (required) {
                    error(recordId, "Collection missing but not optional:" + field);
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkBetween(
                @Nonnull final Uid<?> recordId,
                @Nullable final Integer value,
                final int min,
                final int max,
                final boolean required) {
            assert recordId != null;

            if (value != null) {
                if (!MathUtils.isBetween(value, min, max)) {
                    error(recordId, value, "should be in [" + min + ", " + max + ']');
                }
            } else {
                if (required) {
                    error(recordId, "Entity missing but not optional:" + field);
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkBetween(
                @Nonnull final Uid<?> recordId,
                @Nullable final Double value,
                final double min,
                final double max,
                final boolean required) {
            assert recordId != null;

            if (value != null) {
                if (!MathUtils.isBetween(value, min, max)) {
                    error(recordId, value, "should be in [" + min + ", " + max + ']');
                }
            } else {
                if (required) {
                    error(recordId, "Entity missing but not optional:" + field);
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkBetweens(
                @Nonnull final Uid<?> recordId,
                @Nullable final Collection<Integer> values,
                final int min,
                final int max,
                final boolean required) {
            assert recordId != null;

            if (values != null) {
                for (final Integer value : values) {
                    checkBetween(recordId, value, min, max, required);
                }
            } else {
                if (required) {
                    error(recordId, "Entity missing but not optional:" + field);
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkBetweens(
                @Nonnull final Uid<?> recordId,
                @Nullable final Collection<Double> values,
                final double min,
                final double max,
                final boolean required) {
            assert recordId != null;

            if (values != null) {
                for (final Double value : values) {
                    checkBetween(recordId, value, min, max, required);
                }
            } else {
                if (required) {
                    error(recordId, "Entity missing but not optional:" + field);
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkPoint(
                @Nonnull final Uid<?> recordId,
                @Nullable final GeoPoint point,
                final boolean required) {
            assert recordId != null;

            if ((point == null) && required) {
                error(recordId, "Entity missing but not optional");
            }
            final GeoPointMapper m = sub(point, GeoPointMapper.class);
            x = field(m.lat) && checkBetween(recordId, point.getLat(),
                    DB_LAT_MIN, DB_LAT_MAX, true);
            x = field(m.lon) && checkBetween(recordId, point.getLat(),
                    DB_LON_MIN, DB_LON_MAX, true);
            x = field(m.elevationMeters);
            endsub(m);
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkRectangle(
                @Nonnull final Uid<?> recordId,
                @Nullable final GeoRectangle rectangle,
                final boolean required) {
            assert recordId != null;

            if ((rectangle == null) && required) {
                error(recordId, "Entity missing but not optional:" + field);
            }
            final GeoRectangleMapper m = sub(rectangle, GeoRectangleMapper.class);
            x = field(m.northEast) && checkPoint(recordId, rectangle.getNorthEast(), true) &&
                    checkBetween(recordId, rectangle.getEasting(), MathUtils.EPSILON, Double.MAX_VALUE, true);
            x = field(m.southWest) && checkPoint(recordId, rectangle.getSouthWest(), true) &&
                    checkBetween(recordId, rectangle.getNorthing(), MathUtils.EPSILON, Double.MAX_VALUE, true);
            endsub(m);
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkRectangles(
                @Nonnull final Uid<?> recordId,
                @Nonnull final Collection<GeoRectangle> rectangles) {
            assert recordId != null;

            int i1 = 1;
            for (final GeoRectangle rect1 : rectangles) {
                checkRectangle(recordId, rect1, true);

                // -- And rectangles should not be fully contained within each other.
                int i2 = 1;
                for (final GeoRectangle rect2 : rectangles) {
                    // This check specifically uses object equality to skip 'itself'.
                    //noinspection ObjectEquality
                    if ((rect1 != rect2) && rect1.contains(rect2)) {
                        error(recordId, String.valueOf(i1), "Contained in rectangle: " + i2);
                    }
                    ++i2;
                }
                ++i1;
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkUnique(
                @Nonnull final Uid<?> recordId,
                @Nullable final Collection<?> objects) {
            assert recordId != null;

            if (objects != null) {
                final Set<Object> set = new HashSet<>();
                for (final Object object : objects) {
                    if (!set.add(object)) {
                        error(recordId, object, "A unique value.");
                    }
                }
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }

        public boolean checkNotNull(
                @Nonnull final Uid<?> recordId,
                @Nullable final Object object,
                final boolean required) {
            assert recordId != null;

            if ((object == null) && required) {
                error(recordId, "Entity missing but not optional");
            }
            nrTotalChecks.incrementAndGet();
            return true;
        }
    }
}
