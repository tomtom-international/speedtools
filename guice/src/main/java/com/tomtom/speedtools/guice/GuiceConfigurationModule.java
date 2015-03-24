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

package com.tomtom.speedtools.guice;

import com.google.inject.Binder;
import com.google.inject.Module;

import javax.annotation.Nonnull;

/**
 * Guice module that reads configuration from certain urls. Supported urls are file://, http://, classpath:, jndi:.
 */
public class GuiceConfigurationModule implements Module {
    @Nonnull
    private final String primaryPropertySourceUrl;
    @Nonnull
    private final String[] secondaryPropertySourceUrls;

    /**
     * Create a Guice configuration module with property URLs for property files. Secondary property files may only
     * contain properties found in the primary property file.
     *
     * @param primaryPropertySourceUrl    Primary property URL.
     * @param secondaryPropertySourceUrls Zero or more secondary property URLs.
     */
    public GuiceConfigurationModule(
            @Nonnull final String primaryPropertySourceUrl,
            @Nonnull final String... secondaryPropertySourceUrls) {
        super();
        assert primaryPropertySourceUrl != null;
        assert secondaryPropertySourceUrls != null;
        this.primaryPropertySourceUrl = primaryPropertySourceUrl;
        this.secondaryPropertySourceUrls = secondaryPropertySourceUrls;
    }

    @Override
    public void configure(@Nonnull final Binder binder) {
        assert binder != null;
        binder.requireExplicitBindings();
        GuiceUtils.bindProperties(binder, primaryPropertySourceUrl, secondaryPropertySourceUrls);
    }
}
