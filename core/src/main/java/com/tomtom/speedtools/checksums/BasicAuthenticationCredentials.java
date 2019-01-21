/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.checksums;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;

public final class BasicAuthenticationCredentials {

    // No default ctor for utility class.
    private BasicAuthenticationCredentials() {
        assert false;
    }

    /**
     * Return a base-64 encoded string.
     *
     * @param source Source string.
     * @return Base-64 representation.
     */
    @Nonnull
    public static String encodeBase64(@Nonnull final String source) {
        assert source != null;
        final String expected = DatatypeConverter.printBase64Binary(source.getBytes());
        return "Basic " + expected;
    }

    /**
     * Return a base-64 encoded string with salt ":none" appended.
     *
     * @param source Source string.
     * @return Base-64 representation.
     */
    @Nonnull
    public static String encodeBase64WithNone(@Nonnull final String source) {
        return encodeBase64(source + ":none");
    }
}
