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

package com.tomtom.speedtools.utils;

/**
 * Utility class which provides convenience methods for hash set related operations.
 */
@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
public final class SetUtils {

    public static final double LOAD_FACTOR = 0.75;

    // Utility class only has private constructor.
    private SetUtils() {
        assert false;
    }

    /**
     * Return a good value for creation of a hash set (to not resize) given a number of elements you wish to insert.
     *
     * @param nrOfElements Number of elements the set should be able to hold without resizing. Must be &gt;= 0.
     * @return Value to use for construction of the set.
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static int hashSetSize(final int nrOfElements) {
        assert nrOfElements >= 0;
        return (int) Math.ceil(((double) nrOfElements) / LOAD_FACTOR);
    }
}
