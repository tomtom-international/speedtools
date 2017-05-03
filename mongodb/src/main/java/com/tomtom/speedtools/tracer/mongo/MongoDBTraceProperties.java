/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import com.tomtom.speedtools.guice.HasProperties;
import com.tomtom.speedtools.guice.InvalidPropertyValueException;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.mongodb.MongoConnectionCache;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.UnknownHostException;

public class MongoDBTraceProperties implements HasProperties {

    @Nonnull
    private final String servers;
    @Nonnull
    private final String database;
    @Nonnull
    private final String userName;
    @Nonnull
    private final String password;
    private final int maxDatabaseSizeMB;
    private final int connectionTimeoutMsecs;
    private final boolean readEnabled;
    private final boolean writeEnabled;

    /**
     * MongoDB traces properties.
     *
     * @param servers                Host names and port numbers of MongoDB engine for traces, formatted as
     *                               "&lt;hostname&gt;:&lt;port&gt;[,&lt;hostname&gt;:&lt;port&gt;]*".
     *                               This can be DNS name or IP number.
     *                               The port number must be in range: [1, 65535]. Cannot be empty.
     * @param database               Database name for traces. Cannot be empty.
     * @param userName               Database userName for traces. Can be empty.
     * @param password               Database password for traces. Can be empty.
     * @param maxDatabaseSizeMB      Maximum size of capped collection, in megabytes. Must be &gt; 0.
     * @param connectionTimeoutMsecs Connection timeout in msecs. Must be &gt;= 0.
     * @param readEnabled            Specifies whether the MongoDB trace is read enabled or not.
     * @param writeEnabled           Specifies whether the MongoDB trace is write enabled or not.
     */
    @Inject
    public MongoDBTraceProperties(
            @Named("MongoDBTrace.servers") @Nonnull final String servers,
            @Named("MongoDBTrace.database") @Nonnull final String database,
            @Named("MongoDBTrace.userName") @Nonnull final String userName,
            @Named("MongoDBTrace.password") @Nonnull final String password,
            @Named("MongoDBTrace.maxDatabaseSizeMB") final int maxDatabaseSizeMB,
            @Named("MongoDBTrace.connectionTimeoutMsecs") final int connectionTimeoutMsecs,
            @Named("MongoDBTrace.readEnabled") final boolean readEnabled,
            @Named("MongoDBTrace.writeEnabled") final boolean writeEnabled)
            throws InvalidPropertyValueException {
        assert servers != null;
        assert database != null;
        assert userName != null;
        assert password != null;
        assert maxDatabaseSizeMB > 0;
        assert connectionTimeoutMsecs > 0;

        // Check property values.
        if (servers.isEmpty()) {
            throw new InvalidPropertyValueException("MongoDBTrace.servers cannot be empty.");
        }
        try {
            MongoConnectionCache.getMongoDBServerAddresses(servers);
        } catch (final UnknownHostException ignored) {
            throw new InvalidPropertyValueException("MongoDBTrace.servers has an invalid value (unknown host)");
        }

        if (database.isEmpty()) {
            throw new InvalidPropertyValueException("MongoDBTrace.database cannot be empty.");
        }
        if (maxDatabaseSizeMB <= 0) {
            throw new InvalidPropertyValueException("MongoDBTrace.maxDatabaseSizeMB must be > 0.");
        }
        if (connectionTimeoutMsecs < 0) {
            throw new InvalidPropertyValueException("MongoDBTrace.connectionTimeoutMsecs must be >= 0.");
        }
        this.servers = servers;
        this.database = database;
        this.userName = userName;
        this.password = password;
        this.maxDatabaseSizeMB = maxDatabaseSizeMB;
        this.connectionTimeoutMsecs = connectionTimeoutMsecs;
        this.readEnabled = readEnabled;
        this.writeEnabled = writeEnabled;
    }

    @Nonnull
    public String getServers() {
        return servers;
    }

    @Nonnull
    public String getDatabase() {
        return database;
    }

    @Nonnull
    public String getUserName() {
        return userName;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    public int getMaxDatabaseSizeMB() {
        return maxDatabaseSizeMB;
    }

    public int getConnectionTimeoutMsecs() {
        return connectionTimeoutMsecs;
    }

    public boolean getReadEnabled() {
        return readEnabled;
    }

    public boolean getWriteEnabled() {
        return writeEnabled;
    }

    @Nonnull
    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
