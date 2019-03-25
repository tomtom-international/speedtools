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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class provides an interface to fetch a Mongo instance, given a series of server names and their port numbers.
 */
public final class MongoConnectionCache {
    private static final Logger LOG = LoggerFactory.getLogger(MongoConnectionCache.class);

    private static final String HOSTNAME_SEPARATOR = "[;, ]";
    private static final char PORTNAME_SEPARATOR = ':';

    /**
     * Local storage for MongoDB instances. This cache prevents the system from creating new MongoDB instances for every
     * unit and integration test during test runs. During a production run, the map would contain just one entry and
     * would essentially never be used.
     */
    private static final Cache<String, Mongo> mongoDBInstances =
            CacheBuilder.newBuilder().weakValues().build(
                    new CacheLoader<String, Mongo>() {

                        @Override
                        @Nonnull
                        public Mongo load(@Nonnull final String key) throws UnknownHostException {
                            assert key != null;
                            return new Mongo(key);
                        }
                    });

    private MongoConnectionCache() {
        // Prevent instantiation.
    }

    /**
     * Get a MongoDB instance, given its server address(es). The server specification should be formatted as follows:
     * <pre>
     *      hostname:port[,hostname:port]*
     * </pre>
     *
     * @param servers             Server addresses, format see above.
     * @param connectTimeoutMsecs Timeout in seconds.
     * @param userName            User name.
     * @param database            Database.
     * @param password            Password.
     * @return MongoDB instance.
     * @throws UnknownHostException If the MongoDB server cannot be found.
     */
    @Nonnull
    public static Mongo getMongoDB(@Nonnull final String servers, final int connectTimeoutMsecs,
                                   @Nonnull final String userName, @Nonnull final String database,
                                   @Nonnull final String password)
            throws UnknownHostException {
        assert servers != null;
        assert connectTimeoutMsecs >= 0;
        assert userName != null;
        assert database != null;
        assert password != null;
        try {
            return mongoDBInstances.get(servers, () -> {
                LOG.info("getMongoDB: MongoDB servers: " + servers);

                final List<ServerAddress> replicaSetSeeds = getMongoDBServerAddresses(servers);
                final List<MongoCredential> credentials = new ArrayList<>();
                if (!userName.isEmpty()) {
                    LOG.debug("getMongoDB: credentials provided (username/password)");
                    final MongoCredential credential = MongoCredential.createCredential(userName, database, password.toCharArray());
                    credentials.add(credential);
                }
                return new MongoClient(replicaSetSeeds, credentials,
                        MongoClientOptions.builder().
                                connectTimeout(connectTimeoutMsecs).
                                build());
            });
        } catch (final ExecutionException e) {
            throw new UnknownHostException("Couldn't connect to MongoDB at: " + servers + ", cause: " + e.getCause());
        }
    }

    /**
     * From a string list of hostname:port tuples, create a list of MongoDB server addresses.
     *
     * @param servers String of host names and port numbers.
     * @return List of MongoDB server addresses.
     * @throws UnknownHostException     If the MongoDB server cannot be found.
     * @throws IllegalArgumentException If a value is incorrect.
     */
    @Nonnull
    public static List<ServerAddress> getMongoDBServerAddresses(
            @Nonnull final String servers) throws UnknownHostException {
        final String[] serversArray = servers.split(HOSTNAME_SEPARATOR);
        final List<ServerAddress> replicaSetSeeds = new ArrayList<>(serversArray.length);
        for (final String server : serversArray) {
            final int sep = server.indexOf(PORTNAME_SEPARATOR);
            if ((sep <= 0) || (sep >= (server.length() - 1))) {
                throw new IllegalArgumentException(
                        "Expected format <hostname>" + PORTNAME_SEPARATOR + "<port>, but was " + server);
            }
            final String host = server.substring(0, sep).trim();
            final int port = Integer.valueOf(server.substring(sep + 1));
            if ((port < 1) || (port > 65535)) {
                throw new IllegalArgumentException("Port number must be in [1, 65535], but was " + port);
            }
            final ServerAddress address = new ServerAddress(host, port);
            replicaSetSeeds.add(address);
        }
        return replicaSetSeeds;
    }
}
