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

import com.tomtom.speedtools.locale.LocaleUtil;

import javax.annotation.Nullable;
import java.util.Locale;

public class LocaleMapper extends Mapper<Locale> {
    @Nullable
    @Override
    public Locale fromDb(@Nullable final Object dbValue) throws MapperException {
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof String) {
            try {
                return LocaleUtil.parse((String) dbValue);
            } catch (final IllegalArgumentException e) {
                throw new MapperException("Invalid Locale value '" + dbValue + "'. " + e.getMessage());
            }
        }
        throw new MapperException("String value expected, " +
                "got a value of type: " + dbValue.getClass().getCanonicalName());
    }

    @Nullable
    @Override
    public Object toDb(@Nullable final Locale value) throws MapperException {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
