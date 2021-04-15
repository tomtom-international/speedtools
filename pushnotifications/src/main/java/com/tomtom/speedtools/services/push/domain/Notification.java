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

package com.tomtom.speedtools.services.push.domain;

import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Notification contains information that is intended for a Party.
 */
public final class Notification {

    @Nonnull
    final private Uid<Notification> id;
    @Nonnull
    private final String type;
    @Nullable
    private final Duration timeToLive;
    @Nullable
    final private String message;
    @Nullable
    final private JsonRenderable payload;
    @Nullable
    final private Integer badge;

    public Notification(
            @Nonnull final Uid<Notification> id,
            @Nonnull final String type,
            @Nullable final Duration timeToLive,
            @Nullable final String message,
            @Nullable final JsonRenderable payload,
            @Nullable final Integer badge) {
        assert id != null;
        assert type != null;
        assert (badge == null) || (badge >= 0);

        this.id = id;
        this.type = type;
        this.timeToLive = timeToLive;
        this.message = message;
        this.payload = payload;
        this.badge = badge;
    }

    @Nonnull
    public Uid<Notification> getId() {
        return id;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nullable
    public Duration getTimeToLive() {
        return timeToLive;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public JsonRenderable getPayload() {
        return payload;
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }

    @Nullable
    public Integer getBadge() {
        return badge;
    }
}
