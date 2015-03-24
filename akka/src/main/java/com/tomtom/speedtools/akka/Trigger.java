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

package com.tomtom.speedtools.akka;

import akka.actor.UntypedActor;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triggers are actor messages that contain no state. Trigger instances are singletons, only one trigger instance with
 * the same (qualified) name should be defined.
 */
@SuppressWarnings("ThisEscapedInObjectConstruction")
public final class Trigger implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private static final Map<String, Trigger> usedNames = new ConcurrentHashMap<String, Trigger>();
    @Nonnull
    private final String name;

    public Trigger(
            @Nonnull final Class<? extends UntypedActor> scope,
            @Nonnull final String localName) {
        assert scope != null;
        assert localName != null;
        this.name = scope.getName() + '#' + localName;
        assert usedNames.put(localName, this) == null;
    }

    /**
     * @return Trigger name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nonnull final Object obj) {
        if (obj instanceof Trigger) {
            return ((Trigger) obj).name.equals(name);
        }
        return false;
    }
}
