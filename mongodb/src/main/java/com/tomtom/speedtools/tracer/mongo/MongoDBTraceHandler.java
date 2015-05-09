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

package com.tomtom.speedtools.tracer.mongo;

import com.mongodb.*;
import com.tomtom.speedtools.mongodb.MongoConnectionCache;
import com.tomtom.speedtools.mongodb.SimpleMongoDBSerializer;
import com.tomtom.speedtools.tracer.GenericTraceHandler;
import com.tomtom.speedtools.tracer.TracerFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("ThisEscapedInObjectConstruction")
public class MongoDBTraceHandler implements GenericTraceHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBTraceHandler.class);

    private static final int MEGABYTE = 1024 * 1024;
    @Nonnull
    private static final AtomicLong serialNrCounter = new AtomicLong(0);
    @Nullable
    private final DBCollection collection;

    @Inject
    public MongoDBTraceHandler(@Nonnull final MongoDBTraceProperties properties) {
        assert properties != null;

        DBCollection traceCollection = null;
        if (properties.getWriteEnabled()) {
            LOG.debug("MongoDBTraceHandler: writing traces enabled, opening collection and adding trace handler");
            try {
                traceCollection = getDBCollection(
                        properties.getServers(), properties.getDatabase(),
                        properties.getUserName(), properties.getPassword(),
                        properties.getMaxDatabaseSizeMB(), properties.getConnectionTimeoutMsecs());
            } catch (final UnknownHostException | MongoException e) {
                LOG.error("MongoDBTraceHandler: MongoDB exception, disabled traces: properties={}, {}", properties, e);
            } catch (final Exception e) {
                LOG.error("MongoDBTraceHandler: Non-MongoDB exception, disabled traces: properties={}, {}", properties,
                        e);
            }

            // Only register with factory if the collection exists.
            //noinspection VariableNotUsedInsideIf
            if (traceCollection != null) {
                TracerFactory.addTraceHandler(this);
            }
        } else {
            LOG.debug("MongoDBTraceHandler: writing traces disabled, no trace handler added");
        }
        this.collection = traceCollection;
    }

    @Override
    public void handle(
            @Nonnull final DateTime time,
            @Nonnull final String clazz,
            @Nonnull final String tracer,
            @Nonnull final String method,
            @Nonnull final Object[] args) {
        assert time != null;
        assert clazz != null;
        assert tracer != null;
        assert method != null;
        assert args != null;

        // Bail out if the trace collection does not exist. Don't output the args - too chatty.
        if (collection == null) {
            LOG.warn("handle: Cannot write trace - trace collection does not exist, " +
                    "{}.{}.{}", clazz, tracer, method);
            return;
        }
        assert collection != null;

        final long serialNr = serialNrCounter.getAndIncrement();
        @Nonnull final MongoDBTrace trace =
                new MongoDBTrace(time, clazz, tracer, method, args, serialNr);

        // Catch exceptions from MongoDB here.
        try {
            final Object dbTrace = SimpleMongoDBSerializer.getInstance().serialize(trace);
            if (dbTrace instanceof DBObject) {
                collection.insert((DBObject) dbTrace);
            }
        } catch (final Exception e) {
            LOG.error("handle: Cannot insert trace, trace=" + trace, e);

            /**
             *  Continue execution, because errors during tracing should NOT disturb execution.
             *  Do log this as en error, because we're not expecting this to happen.
             */
        }
    }

    @Nonnull
    static DBCollection getDBCollection(
            @Nonnull final String servers,
            @Nonnull final String database,
            @Nonnull final String userName,
            @Nonnull final String password,
            final int sizeMB,
            final int connectTimeoutMsecs) throws UnknownHostException {
        assert servers != null;
        assert database != null;
        assert userName != null;
        assert password != null;
        assert connectTimeoutMsecs >= 0;

        LOG.info("getDBCollection: Creating MongoDB connection for traces: {}", servers);
        final Mongo mongo = MongoConnectionCache.getMongoDB(servers, connectTimeoutMsecs,
                userName, database, password);

        // If this is a replica set, set read preference to secondary for traces.
        final List<ServerAddress> serverAddressList = mongo.getServerAddressList();
        if (serverAddressList.size() > 1) {
            mongo.setReadPreference(ReadPreference.secondary());
        }

        // Should writes fail, then don't throw exceptions, just ignore.
        // We care more about not disturbing the primary system then our own (traces) integrity.
        mongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);

        // The connection point may actually be null... Not an error.
        final String connectPoint = mongo.getConnectPoint();
        LOG.info("getDBCollection: MongoDB connection for traces established: '{}' at {}",
                database, connectPoint);

        final DB db = mongo.getDB(database);
        final DBObject options = new BasicDBObject();
        options.put("capped", true);
        options.put("size", sizeMB * MEGABYTE);

        final DBCollection collection;
        if (!db.getCollectionNames().contains(Constants.COLLECTION_NAME)) {
            collection = db.createCollection(Constants.COLLECTION_NAME, options);
        } else {
            collection = db.getCollection(Constants.COLLECTION_NAME);
        }

        return collection;
    }
}
