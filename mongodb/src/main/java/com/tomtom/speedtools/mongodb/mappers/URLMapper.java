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

package com.tomtom.speedtools.mongodb.mappers;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

public class URLMapper extends Mapper<URL> {

    @Nullable
    @Override
    public URL fromDb(@Nullable final Object dbValue) throws MapperException {
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof String) {
            try {
                return new URL((String) dbValue);
            } catch (final MalformedURLException ignored) {
                throw new MapperException("Malformatted URL value, got a value of: " + dbValue);
            }
        }
        throw new MapperException("String value expected, " +
                "got a value of type: " + dbValue.getClass().getCanonicalName());
    }

    @Nullable
    @Override
    public String toDb(@Nullable final URL value) throws MapperException {
        if (value == null) {
            return null;
        }
        return value.toExternalForm();
    }
}
