/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.json;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class may serve as a simple base class to implement toString() and equals() (using the JSON string
 * representation).
 */
public class JsonBased {

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof JsonBased;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof JsonBased)) {
            final JsonBased that = (JsonBased) obj;
            eq = that.canEqual(this);
            eq = eq && toString().equals(that.toString());
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return Json.toStringJson(this);
    }
}
