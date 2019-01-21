/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.maven;

import com.tomtom.speedtools.guice.HasProperties;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class allows an application to read the maven POM version. The property
 * file in the code base should contain a line like this:
 *
 * <pre>
 *     Maven.pomVersion = ${pom.version}
 * </pre>
 *
 * This will cause Maven to properly expand the POM version in the property.
 */
public class MavenProperties implements HasProperties {

    @Nonnull
    private final String pomVersion;

    @Inject
    public MavenProperties(
            @Named("Maven.pomVersion") @Nonnull final String pomVersion) {
        assert pomVersion != null;
        this.pomVersion = pomVersion.trim();
    }

    @Nonnull
    public String getPomVersion() {
        return pomVersion;
    }
}
