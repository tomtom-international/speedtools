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

package com.tomtom.speedtools.urls;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;


/**
 * A URL specific parameter string builder.
 */
public class UrlParameterBuilder {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(UrlParameterBuilder.class);

    /**
     * Parameter encoding type.
     */
    @Nonnull
    public static final String ENCODING = "UTF-8";

    @Nonnull
    protected final Collection<Pair> parameters = new ArrayList<>();

    /**
     * Clear all key/value pairs.
     */
    public void clear() {
        parameters.clear();
    }

    /**
     * Add a key/value pair to the set of parameters. Invoking this method with a previously added key (and value) will
     * result in both entries to be retained (useful for e.g. constructing lists).
     *
     * @param key   Key to add. Cannot be empty.
     * @param value key to add. Can be empty.
     */
    public void addParameter(@Nonnull final String key, @Nonnull final Object value) {
        assert key != null;
        assert !key.isEmpty();
        assert value != null;

        parameters.add(new Pair(key, value.toString()));
    }

    /**
     * URL encoded parameters.
     *
     * @return URL (UTF-8) encoded parameters.
     */
    @Nonnull
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        String delim = "";
        for (final Pair p : parameters) {
            result.append(delim).append(encode(p.key)).append('=').append(encode(p.value));
            delim = "&";
        }
        return result.toString();
    }

    /**
     * Utility function to encode a parameter.
     *
     * @param par Parameter to URL encode.
     * @return Encoded parameter.
     */
    @Nonnull
    public static String encode(@Nonnull final String par) {
        String enc;
        try {
            enc = URLEncoder.encode(par, ENCODING);
        } catch (final UnsupportedEncodingException ignore) {
            LOG.error("encode: Cannot URLEncoder.encode [{}] in {}", par, ENCODING);
            enc = "";
        }
        assert enc != null;
        return enc;
    }

    /**
     * Helper class for URL parameter string builder.
     */
    private static class Pair {
        @Nonnull
        public final String key;

        @Nonnull
        public final String value;

        private Pair(@Nonnull final String key, @Nonnull final String value) {
            super();
            assert key != null;
            assert !key.isEmpty();
            assert value != null;
            this.key = key;
            this.value = value;
        }
    }
}

