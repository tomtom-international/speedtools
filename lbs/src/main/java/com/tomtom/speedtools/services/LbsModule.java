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

package com.tomtom.speedtools.services;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.tomtom.speedtools.services.lbs.LbsProperties;
import com.tomtom.speedtools.services.lbs.geocode.GeoCodeEngine;
import com.tomtom.speedtools.services.lbs.geocode.implementation.TomTomLbsGeoCodeEngine;
import com.tomtom.speedtools.services.lbs.route.RouteEngine;
import com.tomtom.speedtools.services.lbs.route.implementation.TomTomLbsRouteEngine;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

/**
 * This is a Guice module to configure the modules for {@link com.tomtom.speedtools.services}.
 */
public class LbsModule implements Module {

    @Override
    public void configure(@Nonnull final Binder binder) {
        assert binder != null;

        // Binder services.
        binder.bind(GeoCodeEngine.class).to(TomTomLbsGeoCodeEngine.class).in(Singleton.class);
        binder.bind(RouteEngine.class).to(TomTomLbsRouteEngine.class).in(Singleton.class);
        binder.bind(LbsProperties.class).in(Singleton.class);
    }
}
