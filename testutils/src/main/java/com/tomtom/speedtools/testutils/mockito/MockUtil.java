/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.testutils.mockito;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Utilities to work around generics warnings when mocking using Mockito.
 */
public final class MockUtil {

    private MockUtil() {
        // Prevent instantiation.
    }

    /**
     * Get an argument captor for class <b>c</b>
     *
     * @param <T> Class.
     * @param c   The class to make a captor for.
     * @return The captor
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> ArgumentCaptor<T> captorOf(@Nonnull final Class<? super T> c) {
        return ArgumentCaptor.forClass((Class<T>) c);
    }

    /**
     * Get a mock of class <b>c</b>
     *
     * @param <T> Class.
     * @param c   The class to make a mock for.
     * @return The mock.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> T mockOf(@Nonnull final Class<? super T> c) {
        return Mockito.mock((Class<T>) c);
    }

    /**
     * Argument matcher that uses reflection to invoke the getId() method.
     *
     * Important: this method is used ONLY in Mockito verify() methods and is defined to return null. The method is
     * however not annotated Nullable to avoid all sorts of nullability warnings which are not relevant for those
     * verify() methods.
     *
     * @param <T>   Class.
     * @param other The object to match the argument with.
     * @return Always null.
     */
    public static <T> T sameId(@Nonnull final T other) {        // Not annotated Nullable, see comment above!
        return Mockito.argThat(new ArgumentMatcher<T>() {       // Returns null be definition.

            @Override
            public boolean matches(@Nullable final Object o) {
                try {
                    // Get Id method.
                    if (o == null) {
                        return false;
                    }

                    final Method getIdMethod = o.getClass().getMethod("getId");

                    // Get ids of both argument and other.
                    final Object argumentId = getIdMethod.invoke(o);
                    final Object otherId = getIdMethod.invoke(other);

                    // Compare ids.
                    return argumentId.equals(otherId);
                } catch (final Exception e) {
                    // This is a test util, throw RuntimeException.  This will end up as an error in the test.
                    //noinspection ProhibitedExceptionThrown
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
