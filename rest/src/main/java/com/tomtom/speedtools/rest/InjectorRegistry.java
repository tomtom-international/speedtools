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

package com.tomtom.speedtools.rest;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Class that tracks the current Guice injector, for accessing guice from container managed classes (e.g. servlets).
 * There cannot be more than one injector active at any time. Instantiating this class will fail if an injector was
 * already set. The current injector can be reset by using clear().
 *
 * The injector is injected into the constructor by Guice.
 */
public final class InjectorRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(InjectorRegistry.class);

    @Nullable
    private static Injector injector = null;

    /**
     * Clear the injector, thus allowing a new injector to be stored.
     */
    public static void clear() {
        injector = null;
    }

    /**
     * Retrieve the current global (static) injector.
     *
     * @return The injector.
     */
    @Nullable
    public static Injector getInjector() {
        return injector;
    }

    /**
     * Create a new instance of this registry, thereby setting the global (static) injector. If an injector was already
     * set, an IllegalStateException will be thrown.
     *
     * @param injector The new injector (may not be null).
     */
    @SuppressWarnings("NullableProblems")
    @Inject
    private InjectorRegistry(@Nonnull final Injector injector) {
        assert injector != null;

        if (InjectorRegistry.injector != null) {
            LOG.error("InjectorRegistry: Injector has already been set.");
            throw new IllegalStateException("Injector already set.");
        }

        InjectorRegistry.injector = injector;
    }
}
