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

package com.tomtom.speedtools.utils;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * Utility class to handle double comparisons properly.
 */
public final class DoubleUtils {

    /**
     * Comparator that uses the Double's natural ordering.
     */
    public static final Comparator<Double> DOUBLE_COMPARATOR =
            (@Nonnull final Double o1, @Nonnull final Double o2) -> o1.compareTo(o2);

    private DoubleUtils() {
        // Prevent instantiation.
    }
}
