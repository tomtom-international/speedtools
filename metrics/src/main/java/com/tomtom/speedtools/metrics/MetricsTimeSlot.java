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

package com.tomtom.speedtools.metrics;

import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.objects.Objects;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Time-bucket that represents aggregated data for an amount of time for a specific metric. Storing metrics in
 * time-buckets creates a trade-off between maintaining historic data for any period of time while still using a fixed,
 * limited amount of memory.
 */
@SuppressWarnings("PackageVisibleField")
public final class MetricsTimeSlot implements JsonRenderable {
    @Nonnull
    private final DateTime startTime;
    private final float sum;
    private final float sumSquares;
    private final float max;
    private final float min;
    private final int count;

    public MetricsTimeSlot(@Nonnull final DateTime startTime, final float sum, final float sumSquares, final float max, final float min, final int count) {
        assert startTime != null;
        this.startTime = startTime;
        this.sum = sum;
        this.sumSquares = sumSquares;
        this.max = max;
        this.min = min;
        this.count = count;
    }

    @Nonnull
    public DateTime getStartTime() {
        return startTime;
    }

    public float getSum() {
        return sum;
    }

    public float getSumSquares() {
        return sumSquares;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    public int getCount() {
        return count;
    }

    @Nonnull
    public MetricsTimeSlot addValue(final float value) {
        return new MetricsTimeSlot(startTime, this.sum + value, this.sumSquares + (value * value),
                Math.max(max, value), Math.min(min, value), count + 1);
    }

    public boolean canEqual(@Nonnull final Object obj) {
        assert obj != null;
        return obj instanceof MetricsTimeSlot;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        boolean eq;
        if (this == obj) {
            eq = true;
        } else if ((obj != null) && (obj instanceof MetricsTimeSlot)) {
            final MetricsTimeSlot that = (MetricsTimeSlot) obj;
            eq = that.canEqual(this);
            // Top-level entity, don't check for super.equals(that).
            eq = eq && startTime.equals(that.startTime);
            eq = eq && (Float.compare(sum, that.sum) == 0);
            eq = eq && (count == that.count);
        } else {
            eq = false;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startTime, sum, count);
    }

    @Override
    @Nonnull
    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }
}
