/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Utility methods in this class should be used whenever a context object needs to be retrieved from RestEasy.
 *
 * RestEasy stores context data in thread locals, hence context objects are only available to the thread invoking the
 * JAX-RS resource method. When injecting context objects via annotations, a proxy is injected that only resolves when
 * being accessed on the same thread. When requesting context objects via the RestEasy API, then RestEasy will return
 * {@code null} when not accessed on the same thread.
 *
 * The utilities in this class will throw whenever a requested context object cannot be retrieved because the utility
 * method is not called on the same thread as the one that invoked the JAX-RS resource method. This ensures that
 * programming mistakes are caught as early as possible.
 */
public class ContextHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ContextHelper.class);

    private ContextHelper() {
        // Do not instantiate this class.
    }

    /**
     * Returns contextual objects from RestEasy using {@link ResteasyProviderFactory#getContextData(Class)}.
     * This method will only work if invoked on the thread that RestEasy uses to call the resource method, because it
     * accesses the contextual objects via thread locals.
     *
     * Note that this method will throw an exception in case it is not accessed on the thread that is used to call the
     * resource method.
     *
     * @param clazz The class to get from the context.
     * @param <T>   The type of the class to get from the context.
     * @return The requested contextual object.
     * @throws RuntimeException In case the requested contextual object could not be retrieved (usually caused by not
     *                          being on the resource thread).
     */
    @Nonnull
    public static <T> T getContextData(@Nonnull final Class<T> clazz) {
        assert clazz != null;

        final T object = ResteasyProviderFactory.getContextData(clazz);

        if (object == null) {
            LOG.error("getContextData: contextual object of type {} could not be retrieved from RestEasy. This " +
                    "method must be called on the same thread that invokes the JAX-RS resource method.", clazz.getName());

            throw new IllegalStateException("Contextual object of type " + clazz.getName() + " could not be " +
                    "retrieved from RestEasy. This method must be called on the same thread that invokes the JAX-RS " +
                    "resource method.");
        }

        return object;
    }
}
