/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.mongodb.mappers;

import javax.annotation.Nullable;

public class BooleanMapper extends Mapper<Boolean> {

    @Nullable
    @Override
    public Boolean fromDb(@Nullable final Object dbValue) throws MapperException {
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof Boolean) {
            return (Boolean) dbValue;
        }
        throw new MapperException("Boolean expected, " +
                "got a value of type: " + dbValue.getClass().getCanonicalName());
    }

    @Nullable
    @Override
    public Boolean toDb(@Nullable final Boolean value) {
        if (value == null) {
            return null;
        }
        return value;
    }
}
