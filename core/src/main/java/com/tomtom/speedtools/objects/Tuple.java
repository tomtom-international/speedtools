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

package com.tomtom.speedtools.objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Tuples class to represent tuples (a, b).
 *
 * @param <T1> Type 1.
 * @param <T2> Type 2.
 */
public final class Tuple<T1, T2> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final T1 value1;
    @Nonnull
    private final T2 value2;

    /**
     * Create a new tuple (a, b).
     *
     * @param value1 Value of a.
     * @param value2 Value of b.
     * @param <T1>   Type of a.
     * @param <T2>   Type of b.
     * @return New tuple.
     */
    @Nonnull
    public static <T1, T2> Tuple<T1, T2> create(@Nonnull final T1 value1, @Nonnull final T2 value2) {
        return new Tuple<>(value1, value2);
    }

    /**
     * Public constructor for a tuple (a, b).
     *
     * @param value1 Value of a.
     * @param value2 Value of b.
     */
    public Tuple(@Nonnull final T1 value1, @Nonnull final T2 value2) {
        assert value1 != null;
        assert value2 != null;
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Default constructor, used for deserialization only.
     */
    @SuppressWarnings("ConstantConditions")
    @Deprecated
    private Tuple() {
        super();
        value1 = null;
        value2 = null;
    }

    @Nonnull
    public T1 getValue1() {
        return value1;
    }

    @Nonnull
    public T2 getValue2() {
        return value2;
    }

    public boolean canEqual(@Nonnull final Object obj) {
        return obj instanceof Tuple;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof Tuple)) {
            @SuppressWarnings("unchecked")
            final Tuple<T1, T2> that = (Tuple<T1, T2>) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && Objects.equal(value1, that.value1);
            eq = eq && Objects.equal(value2, that.value2);
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value1, value2);
    }

    @Nonnull
    @Override
    public String toString() {
        return "[" + value1 + ", " + value2 + ']';
    }
}
