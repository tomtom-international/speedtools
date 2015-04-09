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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods to work with immutable data structures. These methods rely on Google immutable libraries and
 * provide a convenience layer on top of them.
 */
public final class Immutables {

    /**
     * Private ctor. Prevent construction.
     */
    private Immutables() {
        super();
        assert false;
    }

    /**
     * Returns the immutable version of given collection. When given collection was already immutable, this collection
     * is simply returned.
     *
     * @param elts Elements for the list.
     * @return Immutable list.
     */
    @Nonnull
    public static <T> Collection<T> copyOf(@Nonnull final Collection<? extends T> elts) {
        assert elts != null;
        if (elts instanceof Set) {
            return ImmutableSet.copyOf(elts);
        } else {
            return ImmutableList.copyOf(elts);
        }
    }

    /**
     * Returns a collection containing elements from elts1, concatenated with elts2. When given collection was already
     * an immutable list, this collection is simply returned.
     *
     * @param elts1 Initial elements for the list.
     * @param elts2 Elements to be concatenated.
     * @return Concatenated collection.
     */
    @SafeVarargs
    @Nonnull
    public static <T> Collection<T> copyOf(@Nonnull final Collection<? extends T> elts1, @Nonnull final T... elts2) {
        assert elts1 != null;
        assert elts2 != null;
        if (elts1.isEmpty()) {
            return listOf(elts2);
        } else if (elts2.length == 0) {
            return copyOf(elts1);
        } else {
            final Builder<T> builder = ImmutableList.builder();
            builder.addAll(elts1);
            builder.add(elts2);
            return builder.build();
        }
    }

    /**
     * Return the empty immutable list.
     *
     * @return Empty list.
     */
    @Nonnull
    public static <T> List<T> emptyList() {
        return ImmutableList.of();
    }

    /**
     * Creates an immutable list containing a single element.
     *
     * @param elt Element in the list.
     * @return Immutable list.
     */
    @Nonnull
    public static <T> List<T> listOfSingle(@Nonnull final T elt) {
        assert elt != null;
        return ImmutableList.of(elt);
    }

    /**
     * Creates an immutable list containing given elements.
     *
     * @param elts Elements for the list.
     * @return Immutable list.
     */
    @SafeVarargs
    @Nonnull
    public static <T> List<T> listOf(@Nonnull final T... elts) {
        assert elts != null;
        return ImmutableList.copyOf(elts);
    }

    /**
     * Returns an immutable list containing elements from given collection. When given collection was already an
     * immutable list, this list is simply returned.
     *
     * @param values Elements for the list.
     * @return Immutable list.
     */
    @Nonnull
    public static <T> List<T> listOf(@Nonnull final Collection<? extends T> values) {
        assert values != null;
        return ImmutableList.copyOf(values);
    }

    /**
     * Returns an immutable list containing elements from elts1, concatenated with elts2. When given collection was
     * already an immutable list, this list is simply returned.
     *
     * @param elts1 Initial elements for the list.
     * @param elts2 Elements to be concatenated.
     * @return Immutable list.
     */
    @SafeVarargs
    @Nonnull
    public static <T> List<T> listOf(@Nonnull final Collection<? extends T> elts1, @Nonnull final T... elts2) {
        assert elts1 != null;
        assert elts2 != null;
        if (elts1.isEmpty()) {
            return listOf(elts2);
        } else if (elts2.length == 0) {
            return listOf(elts1);
        } else {
            final Builder<T> builder = ImmutableList.builder();
            builder.addAll(elts1);
            builder.add(elts2);
            return builder.build();
        }
    }

    /**
     * Return the empty immutable map.
     *
     * @return Empty map.
     */
    @Nonnull
    public static <K, V> Map<K, V> emptyMap() {
        return ImmutableMap.of();
    }

    /**
     * Returns an immutable map containing elements from given map. When given map was already an
     * immutable map, this map is simply returned.
     *
     * @param values Elements for the map.
     * @return Immutable map.
     */
    @Nonnull
    public static <K, V> Map<K, V> mapOf(@Nonnull final Map<? extends K, ? extends V> values) {
        assert values != null;
        return ImmutableMap.copyOf(values);
    }

    /**
     * Return the empty immutable set.
     *
     * @return Empty set.
     */
    @Nonnull
    public static <T> Set<T> emptySet() {
        return ImmutableSet.of();
    }

    /**
     * Creates an immutable set containing given elements. The order of the elements will be preserved (by means of a
     * {@link LinkedHashSet}.
     *
     * @param elts Elements for the set.
     * @return Immutable set.
     */
    @SafeVarargs
    @Nonnull
    public static <T> Set<T> setOf(@Nonnull final T... elts) {
        assert elts != null;
        return ImmutableSet.copyOf(elts);
    }

    /**
     * Returns an immutable set containing elements from given collection. The order of the elements will be preserved
     * (by means of a {@link LinkedHashSet}. When given collection was already an immutable set, this set is simply
     * returned.
     *
     * @param elts Elements for the set.
     * @return Immutable set.
     */
    @Nonnull
    public static <T> Set<T> setOf(@Nonnull final Collection<? extends T> elts) {
        assert elts != null;
        return ImmutableSet.copyOf(elts);
    }

    /**
     * Returns an immutable set containing elements from elts1, concatenated with elts2. The order of the elements will
     * be preserved (by means of a {@link LinkedHashSet}. When given collection was already an immutable set, this set
     * is simply returned.
     *
     * @param elts1 Initial elements for the set.
     * @param elts2 Elements to be concatenated.
     * @return Immutable set.
     */
    @SafeVarargs
    @Nonnull
    public static <T> Set<T> setOf(@Nonnull final Collection<? extends T> elts1, @Nonnull final T... elts2) {
        assert elts1 != null;
        assert elts2 != null;
        if (elts1.isEmpty()) {
            return setOf(elts2);
        }
        if (elts2.length == 0) {
            return setOf(elts1);
        }
        final ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        builder.addAll(elts1);
        builder.add(elts2);
        return builder.build();
    }

    /**
     * Replace an element in a list with another. If the element did not exist in the list, no change is made and the
     * original list is returned.
     *
     * @param elts   Elements to replace element in.
     * @param oldElt Element to replace.
     * @param newElt Element to replace it with
     * @param <T>    Type of the elements.
     * @return New List with oldElt replaced.
     */
    public static <T> List<T> replace(final List<T> elts, final T oldElt, final T newElt) {
        final Builder<T> builder = ImmutableList.builder();
        for (final T elt : elts) {
            if (elt.equals(oldElt)) {
                builder.add(newElt);
            } else {
                builder.add(elt);
            }
        }
        return builder.build();
    }

    /**
     * Replace an element in a list with another, or adds the element if the element did not exist in the list.
     *
     * @param elts   Elements to replace element in.
     * @param oldElt Element to replace.
     * @param newElt Element to replace it with
     * @param <T>    Type of the elements.
     * @return New List with oldElt replaced.
     */
    public static <T> List<T> replaceOrAdd(final List<T> elts, final T oldElt, final T newElt) {
        final Builder<T> builder = ImmutableList.builder();
        boolean shouldAdd = true;
        for (final T elt : elts) {
            if (elt.equals(oldElt)) {
                builder.add(newElt);
                shouldAdd = false;
                break;
            } else {
                builder.add(elt);
            }
        }
        if (shouldAdd) {
            builder.add(newElt);
        }
        return builder.build();
    }
}
