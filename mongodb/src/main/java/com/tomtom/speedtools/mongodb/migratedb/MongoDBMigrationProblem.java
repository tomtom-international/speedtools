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

package com.tomtom.speedtools.mongodb.migratedb;

import javax.annotation.Nonnull;

public class MongoDBMigrationProblem {
    @Nonnull private final String problem;
    @Nonnull private final String path;

    public MongoDBMigrationProblem(@Nonnull final String problem, @Nonnull final String path) {
        assert problem != null;
        assert path != null;
        this.problem = problem;
        this.path = path;
    }

    @Nonnull
    public String getProblem() {
        return problem;
    }

    @Nonnull
    public String getPath() {
        return path;
    }
}
