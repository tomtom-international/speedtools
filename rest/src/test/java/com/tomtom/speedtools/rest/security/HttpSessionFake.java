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

package com.tomtom.speedtools.rest.security;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpSessionFake implements HttpSession {
    @Nonnull
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public long getCreationTime() {
        assert false;
        return 0;
    }

    @Nullable
    @Override
    public String getId() {
        assert false;
        return null;
    }

    @Override
    public long getLastAccessedTime() {
        assert false;
        return 0;
    }

    @Nullable
    @Override
    public ServletContext getServletContext() {
        assert false;
        return null;
    }

    @Override
    public void setMaxInactiveInterval(final int interval) {
        assert false;
        assert false;
    }

    @Override
    public int getMaxInactiveInterval() {
        assert false;
        return 0;
    }

    @SuppressWarnings({"deprecation", "UnnecessaryFullyQualifiedName"})
    @Nullable
    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        assert false;
        return null;
    }

    @Nullable
    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Nullable
    @Override
    public Object getValue(final String name) {
        assert false;
        return null;
    }

    @Nullable
    @Override
    public Enumeration<String> getAttributeNames() {
        assert false;
        return null;
    }

    @Nullable
    @Override
    public String[] getValueNames() {
        assert false;
        return null;
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        attributes.put(name, value);
    }

    @Override
    public void putValue(final String name, final Object value) {
        assert false;
    }

    @Override
    public void removeAttribute(final String name) {
        attributes.remove(name);
    }

    @Override
    public void removeValue(final String name) {
        assert false;
    }

    @Override
    public void invalidate() {
        assert false;
    }

    @Override
    public boolean isNew() {
        assert false;
        return false;
    }
}
