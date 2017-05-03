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

package com.tomtom.speedtools.mongodb.mappers;

import org.bson.types.Binary;

import javax.annotation.Nullable;

public class BinaryMapper extends Mapper<byte[]> {

    @Nullable
    @Override
    public byte[] fromDb(@Nullable final Object dbValue) throws MapperException {
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof Binary) {
            final Binary binary = (Binary) dbValue;
            return binary.getData();
        }
        if (dbValue instanceof byte[]) {
            final byte[] bytes = (byte[]) dbValue;
            return bytes;
        }
        throw new MapperException("Bytes expected, " +
                "got a value of type: " + dbValue.getClass().getCanonicalName());
    }

    @Nullable
    @Override
    public Binary toDb(@Nullable final byte[] value) {
        if (value == null) {
            return null;
        }
        return new Binary(value);
    }
}
