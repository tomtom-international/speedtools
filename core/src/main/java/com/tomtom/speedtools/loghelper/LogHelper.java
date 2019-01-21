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

package com.tomtom.speedtools.loghelper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tomtom.speedtools.domain.Uid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to store (key, value) pairs, so human readable names can be used in log
 * messages.
 *
 * For example, if an application creates a User with database ID N, it may wish to call
 * {@link #logId(String, String)} for N and userName, so subsequent log messages can use
 * {@link #logId(String)} for N only and show both the ID and the name.
 *
 * <pre>
 *     // Create user. ID and user name both known at this point.
 *     LOG.info("User created: {}", logId(id, name));
 *     ...
 *     // Finds user ID only. User name not known here, but inserted by log helper.
 *     LOG.info("Found user: {}", logId(id));
 * </pre>
 *
 * Note that the LogHelper class has a limited cache of (key, value) pairs, to limit
 * memory usage. The cache auto-expires unused items as well.
 */
public final class LogHelper {
    private static final Logger LOG = LoggerFactory.getLogger(LogHelper.class);

    /**
     * Maximum size of cache (number of entries).
     */
    private static final int INITIAL_CAPACITY = 50000;
    private static final int MAX_CAPACITY = 100000;
    private static final int EXPIRE_AFTER_HOURS = 12;

    /**
     * The cache.
     */
    private static final Cache<String, String> map = CacheBuilder.newBuilder().
            initialCapacity(INITIAL_CAPACITY).
            maximumSize(MAX_CAPACITY).
            expireAfterAccess(EXPIRE_AFTER_HOURS, TimeUnit.HOURS).build();

    // Enable or disabled the cache.
    @SuppressWarnings("StaticNonFinalField")
    private static boolean enabled = false;

    // Enable/disabled the map.
    static {
        enable(LOG.isDebugEnabled());
    }

    private LogHelper() {
        // Prevent usage.
    }

    /**
     * Enable or disable the cache. Normally, you would initialize the cache once with
     * LogHelper.enable(LOG.isDebugEnabled());
     *
     * @param enable True to enable.
     */
    public static void enable(final boolean enable) {
        enabled = enable;
        if (enable) {
            // Do nothing. Enabled by default.
        } else {
            // Don't set to null (not thread safe).
            map.invalidateAll();
        }
    }

    /**
     * Return whether the LogHelper is enabled or not.
     *
     * @return True if enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Store a value from the key/value store and return the key with the value appended.
     *
     * @param key   Unique key.
     * @param value Value. If null, the key/value pair is effectively removed from the map.
     * @return Key with value appended (if not null).
     */
    @Nullable
    public static String logId(@Nullable final Uid<?> key, @Nullable final String value) {
        return (key == null) ? null : logId(key.toString(), value);
    }

    /**
     * Get a value from the map.
     *
     * @param key Unique key.
     * @return Key with value appended (if not null).
     */
    @Nullable
    public static String logId(@Nullable final Uid<?> key) {
        return (key == null) ? null : logId(key.toString());
    }

    /**
     * Store a value from the key/value store and return the key with the value appended.
     *
     * @param key   Unique key.
     * @param value Value. If null, the key/value pair is effectively removed from the map.
     * @return Key with value appended (if not null).
     */
    @Nullable
    public static String logId(@Nullable final String key, @Nullable final String value) {

        // Short cut.
        if (!enabled) {
            return key;
        }

        if (key == null) {
            return null;
        }

        if (value == null) {
            map.invalidate(key);
        } else {
            map.put(key, value);
        }
        return keyValue(key, value);
    }

    /**
     * Get a value from the map.
     *
     * @param key Unique key.
     * @return Key with value appended (if not null).
     */
    @Nullable
    public static String logId(@Nullable final String key) {

        // Short cut.
        if (!enabled) {
            return key;
        }

        if (key == null) {
            return null;
        }

        final String value = map.getIfPresent(key);
        return keyValue(key, value);
    }

    /**
     * Get only the name from the map.
     *
     * @param key Unique key.
     * @return Name if found, or key, or empty.
     */
    @Nonnull
    public static String logIdName(@Nullable final Uid<?> key) {
        if (key == null) {
            return "";
        }
        return logIdName(key.toString());
    }

    /**
     * Get a name from the map.
     *
     * @param key Unique key.
     * @return Name, or key, or empty.
     */
    @Nonnull
    public static String logIdName(@Nullable final String key) {

        // Short cut.
        if (!enabled) {
            return (key == null) ? "" : key;
        }

        if (key == null) {
            return "";
        }

        final String value = map.getIfPresent(key);
        if (value == null) {
            return key;
        }
        return value;
    }

    @Nonnull
    private static String keyValue(@Nonnull final String key, @Nullable final String value) {
        assert key != null;
        if (value == null) {
            return key;
        }
        return '[' + value + ']' + key;
    }
}
