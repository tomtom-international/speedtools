/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.apivalidation;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class provides a JAX-B data transfer object (also called 'binder') which is a top-level
 * list, rather than a (composite) object. This allows the caller to use top-level JSON lists,
 * for example.
 *
 * The method {@link #validateOne(Object)} needs to be implemented to validate a single element.
 * The method {@link #validate()} is already implemented to check all elements.
 *
 * All methods which set, add or remove elements to the list automatically call {@link #beforeSet()}.
 * All methods which get elements from the list automatically call {@link #beforeGet()}.
 *
 * The user of this class should make sure the JAX-B classes look something like this:
 *
 * <pre>
 *    public class MyListDTO extends ApiListDTO&lt;String&gt; {
 *
 *       public void validateOne(final SomeClass elm) {
 *           validator.checkString(true, "name", elm, minLen, maxLen);
 *       }
 *    }
 * </pre>
 *
 * @param <T> Type of list elements.
 */
public abstract class ApiListDTO<T> extends ApiDTO implements List<T> {

    private final ArrayList<T> list;

    protected ApiListDTO(final boolean immutable, final int initialCapacity) {
        super(immutable);
        list = new ArrayList<>(initialCapacity);
    }

    protected ApiListDTO(final boolean immutable) {
        this(immutable, 0);
    }

    protected ApiListDTO() {
        this(true);
    }

    protected ApiListDTO(final boolean immutable, final Collection<? extends T> c) {
        super(immutable);
        list = new ArrayList<>(c);
    }

    protected ApiListDTO(final Collection<? extends T> c) {
        this(true, c);
    }

    /**
     * This method needs to be implemented be the derived class. It validates one element of the
     * list.
     *
     * @param elm One list element.
     */
    public abstract void validateOne(@Nonnull final T elm);

    /**
     * The default implementation is to validate all elements. No need to specify start() and stop() anymore.
     */
    @Override
    public void validate() {
        validator().start();
        for (final T elm : list) {
            validateOne(elm);
        }
        validator().done();
    }

    // Default list methods.

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return list.contains(o);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        beforeGet();
        return list.iterator();
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        beforeGet();
        return list.toArray();
    }

    @Nonnull
    @Override
    public <T1> T1[] toArray(@Nonnull final T1[] a) {
        beforeGet();
        //noinspection SuspiciousToArrayCall
        return list.toArray(a);
    }

    @Override
    public boolean add(final T e) {
        beforeSet();
        return list.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        beforeSet();
        return list.remove(o);
    }

    @Override
    public boolean containsAll(@Nonnull final Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(@Nonnull final Collection<? extends T> c) {
        beforeSet();
        return list.addAll(c);
    }

    @Override
    public boolean addAll(final int index, @Nonnull final Collection<? extends T> c) {
        beforeSet();
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(@Nonnull final Collection<?> c) {
        beforeSet();
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(@Nonnull final Collection<?> c) {
        beforeSet();
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        beforeSet();
        list.clear();
    }

    @Override
    public T get(final int index) {
        beforeGet();
        return list.get(index);
    }

    @Override
    public T set(final int index, final T element) {
        beforeSet();
        return list.set(index, element);
    }

    @Override
    public void add(final int index, final T element) {
        beforeSet();
        list.add(index, element);
    }

    @Override
    public T remove(final int index) {
        beforeSet();
        return list.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return list.lastIndexOf(o);
    }

    @Nonnull
    @Override
    public ListIterator<T> listIterator() {
        beforeGet();
        return list.listIterator();
    }

    @Nonnull
    @Override
    public ListIterator<T> listIterator(final int index) {
        beforeGet();
        return list.listIterator(index);
    }

    @Nonnull
    @Override
    public List<T> subList(final int fromIndex, final int toIndex) {
        beforeGet();
        return list.subList(fromIndex, toIndex);
    }
}
