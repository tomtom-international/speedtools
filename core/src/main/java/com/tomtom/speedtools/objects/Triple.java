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

package com.tomtom.speedtools.objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Triples class to represent triples (a, b, c).
 *
 * @param <T1> Type 1.
 * @param <T2> Type 2.
 * @param <T3> Type 3.
 */
public final class Triple<T1, T2, T3> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final T1 value1;
    @Nonnull
    private final T2 value2;
    @Nonnull
    private final T3 value3;

    /**
     * Create a new triple (a, b, c).
     *
     * @param value1 Value of a.
     * @param value2 Value of b.
     * @param value3 Value of c.
     * @param <T1>   Type of a.
     * @param <T2>   Type of b.
     * @param <T3>   Type of c.
     * @return New triple.
     */
    @Nonnull
    public static <T1, T2, T3> Triple<T1, T2, T3> create(@Nonnull final T1 value1,
                                                         @Nonnull final T2 value2, @Nonnull final T3 value3) {
        return new Triple<>(value1, value2, value3);
    }

    /**
     * Public constructor for a triple (a, b, c).
     *
     * @param value1 Value of a.
     * @param value2 Value of b.
     * @param value3 Value of c.
     */
    public Triple(@Nonnull final T1 value1, @Nonnull final T2 value2, @Nonnull final T3 value3) {
        assert value1 != null;
        assert value2 != null;
        assert value3 != null;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    private Triple() {
        super();
        value1 = null;
        value2 = null;
        value3 = null;
    }

    @Nonnull
    public T1 getValue1() {
        return value1;
    }

    @Nonnull
    public T2 getValue2() {
        return value2;
    }

    @Nonnull
    public T3 getValue3() {
        return value3;
    }

    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof Triple;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Triple)) {
            @SuppressWarnings("unchecked")
            final Triple<T1, T2, T3> that = (Triple<T1, T2, T3>) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && Objects.equal(value1, that.value1);
            eq = eq && Objects.equal(value2, that.value2);
            eq = eq && Objects.equal(value3, that.value3);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value1, value2, value3);
    }

    @Nonnull
    @Override
    public String toString() {
        return "[" + value1 + ", " + value2 + ", " + value3 + ']';
    }
}
