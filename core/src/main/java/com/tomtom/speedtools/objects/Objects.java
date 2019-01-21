/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Generic Java helper methods to implement proper equals and hashCode methods.
 */
@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
public final class Objects {

    /**
     * Private ctor. Prevent construction.
     */
    private Objects() {
        super();
        assert false;
    }

    /**
     * Tests if the 2 given objects are equal. Can be safely used with null references. Two null references are defined
     * to be equal.
     *
     * @param obj1 First object to compare.
     * @param obj2 Second object to compare.
     * @return True if and only if the two objects are equal.
     */
    public static boolean equal(@Nullable final Object obj1, @Nullable final Object obj2) {
        //noinspection ObjectEquality
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 != null) {
            if (obj2 != null) {
                final boolean eq = obj1.equals(obj2);
                assert obj2.equals(obj1) == eq;
                return eq;
            } else {
                // Obj1 == null, Obj2 != null.
                return false;
            }
        } else {
            // Obj1 != null, Obj2 == null.
            assert obj2 != null;
            return false;
        }
    }

    /**
     * Compound hash-code calculation.
     *
     * @param values Values to calculate hash-code for.
     * @return Compound hash-code.
     */
    public static int hashCode(@Nullable final Double... values) {
        return Arrays.hashCode(values);
    }

    /**
     * Compound hash-code calculation.
     *
     * @param values Values to calculate hash-code for.
     * @return Compound hash-code.
     */
    public static int hashCode(@Nullable final String... values) {
        return Arrays.hashCode(values);
    }

    /**
     * Compound hash-code calculation. Note that the array must be hash-coded based on contents, not identity, hence the
     * call to deepHashCode.
     *
     * @param values Values to calculate hash-code for.
     * @return Compound hash-code.
     */
    public static int hashCode(@Nullable final Object... values) {
        return Arrays.deepHashCode(values);
    }

    /**
     * Returns value when it's not null, and defaultValue otherwise.
     *
     * @param <T>          Type.
     * @param value        Value to check.
     * @param defaultValue Value to return when value equals null.
     * @return Value or defaultValue.
     */
    @Nonnull
    public static <T> T notNullOr(@Nullable final T value, @Nonnull final T defaultValue) {
        assert defaultValue != null;
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}
