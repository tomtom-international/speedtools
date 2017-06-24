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

import com.mongodb.*;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper;
import com.tomtom.speedtools.mongodb.mappers.MapperError;
import com.tomtom.speedtools.mongodb.mappers.MapperException;
import com.tomtom.speedtools.time.UTCTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides generic methods to operate on MongoDB Collections. Most methods are package private to make sure
 * they are only used by DAO's.
 */
public final class DaoUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DaoUtils.class);
    private static final boolean UPSERT = true;      // Indicates 'upsert' in case of missing record.
    private static final boolean NO_UPSERT = false;     // Indicates 'no upsert' in case of missing record.
    private static final boolean NO_MULTI = false;     // Indicates 'no multi' for queries.

    @SuppressWarnings("StaticNonFinalField")
    private static WriteConcern writeConcern = WriteConcern.SAFE;

    private DaoUtils() {
        // Prevent instantiation.
    }

    /**
     * Finds a single entity that satisfies a specified query filter.
     *
     * @param collection Collection to get the entity from.
     * @param mapper     Mapper to be used to reconstitute the object stored in the collection.
     * @param query      The query to select the entity.
     * @param <T>        The type of the reconstituted object.
     * @return The object in the collection for the given query.
     * @throws EntityNotFoundException Thrown if no object if found with the given id. This will NOT log an error.
     * @throws InternalDaoException    Thrown when an unknown error has occurred. The error will have been logged.
     */
    @Nonnull
    public static <T> T findOne(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<T> mapper,
            @Nonnull final MongoDBQuery query) throws EntityNotFoundException, InternalDaoException {
        assert collection != null;
        assert mapper != null;
        assert query != null;

        DBObject dbObject = null;
        try {
            dbObject = collection.findOne(query.toDBObject());
            if (dbObject != null) {
                final T entity = mapper.fromDb(dbObject);
                // Cannot be null because dbObject was not null.
                assert entity != null;
                return entity;
            } else {
                final String message = "Entity not found: " + query + ", collection=" + collection.getName() + '.';
                LOG.debug("findOne: {}", message);
                throw new EntityNotFoundException(message);
            }
        } catch (final MapperException | MongoException e) {
            final String message =
                    "Entity could not be mapped: " + query + ", collection=" + collection.getName() +
                            ", object=" + Json.toStringJson(dbObject) + '.';
            LOG.error("findOne: " + message, e);
            throw new InternalDaoException(message, e);
        }
    }

    /**
     * Finds entities that satisfy a specified query filter.
     *
     * @param collection Collection to get the entity from.
     * @param mapper     Mapper to be used to reconstitute the object stored in the collection.
     * @param query      The query to select the entity.
     * @param <T>        The type of the reconstituted object.
     * @return The objects in the collection for the given query.
     * @throws InternalDaoException Thrown when an unknown error has occurred. The error will have been logged.
     */
    @Nonnull
    public static <T> List<T> find(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<T> mapper,
            @Nonnull final MongoDBQuery query) throws InternalDaoException {
        return find(collection, mapper, false, query);
    }

    /**
     * Finds entities that satisfy a specified query filter.
     *
     * @param <T>                Element type.
     * @param collection         Collection to get the entity from.
     * @param mapper             Mapper to be used to reconstitute the object stored in the collection.
     * @param ignoreMapperErrors Ignore mapping exception, skip entities with mapping errors.
     * @param query              The query to select the entity.
     * @return The objects in the collection for the given query.
     * @throws InternalDaoException Thrown when an unknown error has occurred. The error will have been logged.
     */
    @Nonnull
    public static <T> List<T> find(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<T> mapper,
            final boolean ignoreMapperErrors,
            @Nonnull final MongoDBQuery query) throws InternalDaoException {
        return find(
                collection,
                mapper,
                ignoreMapperErrors,
                query,
                MongoDBSorting.DEFAULT_INSTANCE,
                MongoDBPaging.DEFAULT_INSTANCE);
    }

    /**
     * Finds entities that satisfy a specified query filter.
     *
     * @param <T>                Element type.
     * @param collection         Collection to get the entity from.
     * @param mapper             Mapper to be used to reconstitute the object stored in the collection.
     * @param ignoreMapperErrors Ignore mapping exception, skip entities with mapping errors.
     * @param query              The query to select the entity.
     * @param sorting            The sorting parameters for this query.
     * @param paging             The paging parameters for this query.
     * @return The objects in the collection for the given query.
     * @throws InternalDaoException Thrown when an unknown error has occurred. The error will have been logged.
     */
    @Nonnull
    public static <T> List<T> find(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<T> mapper,
            final boolean ignoreMapperErrors,
            @Nonnull final MongoDBQuery query,
            @Nonnull final MongoDBSorting sorting,
            @Nonnull final MongoDBPaging paging) throws InternalDaoException {
        assert collection != null;
        assert mapper != null;
        assert query != null;

        try {
            final DBCursor dbCursor = collection.find(query.toDBObject());
            final DBCursor cursor = paging.apply(sorting.apply(dbCursor));

            final List<MapperError> errors = new LinkedList<>();
            final List<T> result = new ArrayList<>();
            while (cursor.hasNext()) {

                final DBObject dbValue = cursor.next();
                final T entity = mapper.fromDb(dbValue, errors);
                result.add(entity);
            }

            if (!errors.isEmpty()) {
                final String message =
                        "Mapper errors found: " + query + ", collection=" + collection.getName() + '.';
                LOG.error("find: {} Errors: '{}'", message, Json.toJson(errors));
                if (!ignoreMapperErrors) {
                    throw new InternalDaoException(message);
                }
            }
            return result;
        } catch (final MapperException | MongoException e) {
            final String message =
                    "Entity could not be mapped: " + query + ", collection=" + collection.getName() + '.';
            LOG.error("find: " + message, e);
            throw new InternalDaoException(message, e);
        }
    }

    /**
     * Store the given object in the given collections.
     *
     * @param <T>        The type of the object to te stored.
     * @param collection Collection in which to store the object.
     * @param mapper     Mapper to be used to transform the object.
     * @param entity     The object to be stored.
     * @throws EntityStoreException Thrown if the object cannot be transformed. The error will have been logged.
     */
    public static <T> void storeEntity(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<T> mapper,
            @Nonnull final T entity) throws EntityStoreException {
        assert collection != null;
        assert mapper != null;
        assert entity != null;

        try {
            // Create MongoDB record.
            final DBObject dbObject = mapper.toDb(entity);

            // Cannot be null because entity was not null.
            assert dbObject != null;

            // Add a "last modified" timestamp to each record.
            dbObject.put(MongoDBKeyNames.LAST_MODIFIED_KEY, UTCTime.now().toDate());

            collection.save(dbObject, writeConcern);
        } catch (final MapperException | MongoException e) {
            final String message = "Map entity failed: type=" +
                    entity.getClass().getSimpleName() + ", collection=" + collection.getName() + '.';
            LOG.error("storeEntity: " + message, e);
            throw new EntityStoreException(message, e);
        }
    }

    /**
     * Removes the given object from the given collection. The entity must fully match the database record to be
     * removed.
     *
     * @param <T>        The type of the object to remove.
     * @param collection Collection from which to remove the object.
     * @param mapper     Mapper to be used to transform the object.
     * @param entity     The object to be removed.
     * @throws EntityRemoveException Thrown if the object cannot be removed. The error will have been logged.
     */
    public static <T> void removeEntity(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<T> mapper,
            @Nonnull final T entity) throws EntityRemoveException {
        assert collection != null;
        assert mapper != null;
        assert entity != null;

        try {
            final DBObject dbObject = mapper.toDb(entity);
            collection.remove(dbObject);
        } catch (final MapperException | MongoException e) {
            final String message = "Map entity failed: type=" +
                    entity.getClass().getSimpleName() + ", collection=" + collection.getName() + '.';
            LOG.error("removeEntity: " + message, e);
            throw new EntityRemoveException(message, e);
        }
    }

    /**
     * Removes the given object from the given collection.
     *
     * @param <T>        Element type.
     * @param collection Collection from which to remove the object.
     * @param field      The field to match.
     * @param value      The value of the field.
     * @throws EntityRemoveException Thrown if the object cannot be removed. The error will have been logged.
     */
    public static <T> void removeEntityByField(
            @Nonnull final DBCollection collection,
            @Nonnull final EntityMapper<?>.Field<T> field,
            @Nonnull final T value) throws EntityRemoveException {
        assert collection != null;
        assert field != null;
        assert value != null;

        try {
            final MongoDBQuery dbQuery = new MongoDBQuery().
                    eq(field, value);
            collection.remove(dbQuery.toDBObject());
        } catch (final MapperException | MongoException e) {
            final String message = "Remove entity failed: field=" +
                    field.getFieldName() + ", collection=" + collection.getName() + '.';
            LOG.error("removeEntityByField: " + message, e);
            throw new EntityRemoveException(message, e);
        }
    }

    /**
     * Update a document in a collection. This will always update the last modified time as well.
     *
     * @param collection Collection that contains the document.
     * @param query      Query to find the document.
     * @param update     Update for document.
     * @throws EntityStoreException    New document cannot be stored. The error will have been logged.
     * @throws EntityNotFoundException The document was not found. The error will have been logged.
     */
    public static void update(
            @Nonnull final DBCollection collection,
            @Nonnull final MongoDBQuery query,
            @Nonnull final MongoDBUpdate update) throws EntityStoreException, EntityNotFoundException {
        update(collection, query, update, true);
    }

    /**
     * Update a document in a collection.
     *
     * @param collection         Collection that contains the document.
     * @param query              Query to find the document.
     * @param update             Update for document.
     * @param updateLastModified True if the last modified time needs to be adjusted as well (to now).
     * @throws EntityStoreException    New document cannot be stored. The error will have been logged.
     * @throws EntityNotFoundException The document was not found. The error will have been logged.
     */
    public static void update(
            @Nonnull final DBCollection collection,
            @Nonnull final MongoDBQuery query,
            @Nonnull final MongoDBUpdate update,
            final boolean updateLastModified) throws EntityStoreException, EntityNotFoundException {
        assert collection != null;
        assert query != null;
        assert update != null;
        try {

            // Update last modified time.
            if (updateLastModified) {
                update.setRaw(MongoDBKeyNames.LAST_MODIFIED_KEY, UTCTime.now().toDate());
            }

            // Make sure upsert is set to false to not create new records on the fly.
            final WriteResult result =
                    collection.update(query.toDBObject(), update.toDBObject(), NO_UPSERT, NO_MULTI, writeConcern);
            if (result.getN() == 0) {
                final String message =
                        "Couldn't find entity to update, query: " + query + ", update: " + update + ", " +
                                "collection: " + collection.getName() + '.';
                LOG.error("update: {}", message);
                throw new EntityNotFoundException(message);
            }
        } catch (final MapperException | MongoException e) {
            final String message =
                    "Couldn't map entity to update, query: " + query + ", update: " + update + ", " +
                            "collection: " + collection.getName() + '.';
            LOG.error("update: " + message, e);
            throw new EntityStoreException(message, e);
        }
    }

    /**
     * Returns number of entities that satisfy a specified query filter. When the collections is large and indexes are
     * missing this call can be a performance killer.
     *
     * @param collection Collection to get the entity from.
     * @param query      The query to select the entity.
     * @return The number of objects in the collection for the given query.
     * @throws InternalDaoException Thrown when an unknown error has occurred. The error will have been logged.
     */
    public static long count(
            @Nonnull final DBCollection collection,
            @Nonnull final MongoDBQuery query) throws InternalDaoException {
        assert collection != null;
        assert query != null;

        try {
            LOG.debug("count: collection={}, query={}", collection.getName(), query);
            return collection.count(query.toDBObject());
        } catch (final MapperException e) {
            final String message =
                    "Entity could not be mapped: " + query + ", collection=" + collection.getName() + '.';
            LOG.error("count: " + message, e);
            throw new InternalDaoException(message, e);
        }
    }

    /**
     * Update or insert a document in a collection. This will always update the last modified time as well.
     *
     * @param collection Collection that contains the document.
     * @param query      Query to find the document.
     * @param value      Update for document.
     * @throws EntityStoreException New document cannot be stored. The error will have been logged.
     */
    public static void upsert(
            @Nonnull final DBCollection collection,
            @Nonnull final MongoDBQuery query,
            @Nonnull final DBObject value) throws EntityStoreException {
        upsert(collection, query, value, true);
    }

    /**
     * Update or insert a document in a collection.
     *
     * @param collection         Collection that contains the document.
     * @param query              Query to find the document.
     * @param value              Update (or initial value) for document.
     * @param updateLastModified True if the last modified time needs to be adjusted as well (to now).
     * @return Number of records updated (0 if inserted, 1 if updated).
     * @throws EntityStoreException New document cannot be stored. The error will have been logged.
     */
    public static int upsert(
            @Nonnull final DBCollection collection,
            @Nonnull final MongoDBQuery query,
            @Nonnull final DBObject value,
            final boolean updateLastModified) throws EntityStoreException {
        assert collection != null;
        assert query != null;
        assert value != null;
        try {

            // Update last modified time.
            if (updateLastModified) {
                value.put(MongoDBKeyNames.LAST_MODIFIED_KEY, UTCTime.now().toDate());
            }

            // Make sure upsert is set to true to create the object if it is not found.
            final WriteResult result =
                    collection.update(query.toDBObject(), value, UPSERT, NO_MULTI, writeConcern);
            final int nr = result.getN();
            if (nr == 0) {
                LOG.debug("upsert: Inserted new object, query={}, collection={}", query, collection.getName());
            }
            return nr;
        } catch (final MapperException | MongoException e) {
            final String message =
                    "Couldn't map entity to update, query: " + query + ", update: " + value + ", " +
                            "collection: " + collection.getName() + '.';
            LOG.error("upsert: " + message, e);
            throw new EntityStoreException(message, e);
        }
    }

    /**
     * This retrieves the global write concern of the DAO utils for save() and update() calls.
     *
     * @return Global write concern.
     */
    @Nonnull
    public static WriteConcern getWriteConcern() {
        return writeConcern;
    }

    /**
     * This sets the global write concern of the DAO utils for save() and update() calls.
     *
     * @param newWriteConcern Write concern.
     */
    public static void setWriteConcern(@Nonnull final WriteConcern newWriteConcern) {
        assert newWriteConcern != null;
        writeConcern = newWriteConcern;
    }
}
